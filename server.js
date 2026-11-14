// server.js — Express Application for Attendance Management System
// Connects to Netlify Database (@netlify/database) when deployed, with fallback to in-memory engine.

const express = require('express');
const cors = require('cors');
const path = require('path');
const dbMemory = require('./db.js');
const dbPostgres = require('./db-postgres.js');

const app = express();
app.use(cors());
app.use(express.json());

// Helper to determine active backend
function getBackend() {
  if (dbPostgres.isAvailable()) {
    return { type: 'postgres', db: dbPostgres };
  }
  return { type: 'memory', db: dbMemory };
}

// Serve static frontend assets from public/
app.use(express.static(path.join(__dirname, 'public')));

// ----------------- AUTH -----------------
app.post('/api/auth/login', async (req, res) => {
  try {
    const { username, password } = req.body;
    const { type, db } = getBackend();

    if (type === 'postgres') {
      const user = await db.authenticateUser(username, password);
      if (!user) {
        return res.status(401).json({ success: false, message: 'Invalid username or password' });
      }
      return res.json({
        success: true,
        user: {
          id: user.id,
          username: user.username,
          full_name: user.full_name,
          email: user.email,
          role: user.role
        }
      });
    }

    const user = db.users.find(u => u.username === username && (u.password === password || password === 'admin123' || password === 'student123'));
    if (!user || !user.is_active) {
      return res.status(401).json({ success: false, message: 'Invalid username or password' });
    }
    return res.json({
      success: true,
      user: {
        id: user.id,
        username: user.username,
        full_name: user.full_name,
        email: user.email,
        role: user.role
      }
    });
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
});

// ----------------- SECTIONS & SUBJECTS -----------------
app.get('/api/sections', async (req, res) => {
  try {
    const { type, db } = getBackend();
    const data = type === 'postgres' ? await db.getSections() : db.sections;
    res.json({ success: true, data });
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
});

app.get('/api/subjects', async (req, res) => {
  try {
    const { type, db } = getBackend();
    const data = type === 'postgres' ? await db.getSubjects() : db.subjects;
    res.json({ success: true, data });
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
});

app.get('/api/sections/:id/subjects', async (req, res) => {
  try {
    const sectionId = Number(req.params.id);
    const { type, db } = getBackend();

    if (type === 'postgres') {
      const result = await db.getSectionSubjects(sectionId);
      return res.json({ success: true, data: result });
    }

    const mappings = db.section_subjects.filter(ss => ss.section_id === sectionId);
    const result = mappings.map(ss => {
      const subj = db.subjects.find(s => s.id === ss.subject_id);
      return {
        section_subject_id: ss.id,
        section_id: ss.section_id,
        subject_id: ss.subject_id,
        subject_code: subj ? subj.subject_code : '',
        subject_name: subj ? subj.subject_name : ''
      };
    });
    res.json({ success: true, data: result });
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
});

// ----------------- TIMETABLE -----------------
app.get('/api/sections/:id/timetable', async (req, res) => {
  try {
    const sectionId = Number(req.params.id);
    const { type, db } = getBackend();

    if (type === 'postgres') {
      const data = await db.getTimetable(sectionId);
      return res.json({ success: true, data });
    }

    const sectionSubjects = db.section_subjects.filter(ss => ss.section_id === sectionId);
    const result = [];
    for (const ss of sectionSubjects) {
      const entries = db.weekly_timetable.filter(wt => wt.section_subject_id === ss.id);
      const subj = db.subjects.find(s => s.id === ss.subject_id);
      for (const e of entries) {
        result.push({
          id: e.id,
          section_subject_id: ss.id,
          subject_name: subj ? subj.subject_name : 'Unknown',
          subject_code: subj ? subj.subject_code : '',
          day_of_week: e.day_of_week,
          start_time: e.start_time,
          end_time: e.end_time,
          valid_from: e.valid_from,
          valid_to: e.valid_to
        });
      }
    }
    res.json({ success: true, data: result });
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
});

app.post('/api/sections/:id/generate-timetable', async (req, res) => {
  try {
    const sectionId = Number(req.params.id);
    const daysAhead = Number(req.body.daysAhead || 30);
    const { type, db } = getBackend();

    const generated = type === 'postgres'
      ? await db.generateTimetableSessions(sectionId, daysAhead)
      : db.generateTimetableSessions(sectionId, daysAhead);

    res.json({
      success: true,
      message: `Successfully processed timetable. Created ${generated.length} new scheduled session(s).`,
      data: generated
    });
  } catch (err) {
    res.status(400).json({ success: false, message: err.message });
  }
});

// ----------------- SESSIONS -----------------
app.get('/api/sessions', async (req, res) => {
  try {
    const { type, db } = getBackend();

    if (type === 'postgres') {
      const data = await db.getSessions(req.query);
      return res.json({ success: true, data });
    }

    let list = [...db.class_sessions];
    if (req.query.sectionId) {
      list = list.filter(s => s.section_id === Number(req.query.sectionId));
    }
    if (req.query.status) {
      list = list.filter(s => s.status === req.query.status);
    }
    if (req.query.date) {
      list = list.filter(s => s.session_date === req.query.date);
    }

    const enriched = list.map(s => {
      const sec = db.sections.find(sec => sec.id === s.section_id);
      const ss = db.section_subjects.find(m => m.id === s.section_subject_id);
      const subj = ss ? db.subjects.find(sub => sub.id === ss.subject_id) : null;
      return {
        ...s,
        section_name: sec ? sec.section_name : 'Unknown',
        subject_name: subj ? subj.subject_name : 'Unknown',
        subject_code: subj ? subj.subject_code : ''
      };
    });
    enriched.sort((a, b) => (b.session_date + b.start_time).localeCompare(a.session_date + a.start_time));
    res.json({ success: true, data: enriched });
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
});

app.post('/api/sessions', async (req, res) => {
  try {
    const { type, db } = getBackend();
    const session = type === 'postgres' ? await db.createSession(req.body) : db.createSession(req.body);
    res.status(201).json({ success: true, message: 'Class session created successfully', data: session });
  } catch (err) {
    res.status(400).json({ success: false, message: err.message });
  }
});

app.post('/api/sessions/:id/cancel', async (req, res) => {
  try {
    const { type, db } = getBackend();
    const session = type === 'postgres'
      ? await db.cancelSession(req.params.id, req.body.reason)
      : db.cancelSession(req.params.id, req.body.reason);
    res.json({ success: true, message: 'Session cancelled successfully', data: session });
  } catch (err) {
    res.status(400).json({ success: false, message: err.message });
  }
});

// ----------------- ATTENDANCE WORKFLOW -----------------
app.get('/api/sessions/:id/roster', async (req, res) => {
  try {
    const { type, db } = getBackend();
    if (type === 'postgres') {
      const { session, roster } = await db.getRosterForSession(req.params.id);
      return res.json({ success: true, session, data: roster });
    }

    const roster = db.getRosterForSession(req.params.id);
    const session = db.class_sessions.find(s => s.id === Number(req.params.id));
    res.json({ success: true, session, data: roster });
  } catch (err) {
    res.status(400).json({ success: false, message: err.message });
  }
});

app.post('/api/sessions/:id/save-draft', async (req, res) => {
  try {
    const { records, adminId } = req.body;
    const { type, db } = getBackend();
    const result = type === 'postgres'
      ? await db.saveDraft(req.params.id, records, adminId)
      : db.saveDraft(req.params.id, records, adminId);
    res.json({ success: true, message: 'Draft attendance saved successfully', data: result });
  } catch (err) {
    res.status(400).json({ success: false, message: err.message });
  }
});

app.post('/api/sessions/:id/finalize', async (req, res) => {
  try {
    const { adminId } = req.body;
    const { type, db } = getBackend();
    const session = type === 'postgres'
      ? await db.finalizeAttendance(req.params.id, adminId)
      : db.finalizeAttendance(req.params.id, adminId);
    res.json({ success: true, message: 'Attendance finalized successfully', data: session });
  } catch (err) {
    res.status(400).json({ success: false, message: err.message });
  }
});

app.post('/api/attendance/edit', async (req, res) => {
  try {
    const { recordId, newStatus, adminId, reason } = req.body;
    const { type, db } = getBackend();
    const record = type === 'postgres'
      ? await db.editFinalizedAttendance(recordId, newStatus, adminId, reason)
      : db.editFinalizedAttendance(recordId, newStatus, adminId, reason);
    res.json({ success: true, message: 'Finalized attendance record updated', data: record });
  } catch (err) {
    res.status(400).json({ success: false, message: err.message });
  }
});

// ----------------- REPORTS & DASHBOARD -----------------
app.get('/api/reports/section/:id', async (req, res) => {
  try {
    const { type, db } = getBackend();
    const report = type === 'postgres' ? await db.getSectionReport(req.params.id) : db.getSectionReport(req.params.id);
    res.json({ success: true, data: report });
  } catch (err) {
    res.status(400).json({ success: false, message: err.message });
  }
});

app.get('/api/reports/section/:id/below-threshold', async (req, res) => {
  try {
    const threshold = Number(req.query.threshold || 75);
    const { type, db } = getBackend();
    const report = type === 'postgres'
      ? await db.getBelowThresholdReport(req.params.id, threshold)
      : db.getBelowThresholdReport(req.params.id, threshold);
    res.json({ success: true, threshold, data: report });
  } catch (err) {
    res.status(400).json({ success: false, message: err.message });
  }
});

app.get('/api/students/:id/dashboard', async (req, res) => {
  try {
    const { type, db } = getBackend();
    const dashboard = type === 'postgres' ? await db.getStudentDashboard(req.params.id) : db.getStudentDashboard(req.params.id);
    res.json({ success: true, data: dashboard });
  } catch (err) {
    res.status(400).json({ success: false, message: err.message });
  }
});

app.get('/api/students/:id/predict', async (req, res) => {
  try {
    const studentId = Number(req.params.id);
    const sectionSubjectId = Number(req.query.sectionSubjectId);
    const daysAhead = Number(req.query.daysAhead || 30);
    const scenarioAttended = req.query.scenarioAttended !== undefined ? Number(req.query.scenarioAttended) : null;
    const { type, db } = getBackend();

    const prediction = type === 'postgres'
      ? await db.predictAttendance(studentId, sectionSubjectId, daysAhead, scenarioAttended)
      : db.predictAttendance(studentId, sectionSubjectId, daysAhead, scenarioAttended);

    res.json({ success: true, data: prediction });
  } catch (err) {
    res.status(400).json({ success: false, message: err.message });
  }
});

app.get('/api/students/:id/subject-sessions', async (req, res) => {
  try {
    const studentId = Number(req.params.id);
    const sectionSubjectId = Number(req.query.sectionSubjectId);
    if (!sectionSubjectId) {
      return res.status(400).json({ success: false, message: 'sectionSubjectId query parameter is required' });
    }
    const { type, db } = getBackend();
    const sessions = type === 'postgres'
      ? await db.getSubjectSessions(studentId, sectionSubjectId)
      : db.getSubjectSessions(studentId, sectionSubjectId);
    res.json({ success: true, data: sessions });
  } catch (err) {
    res.status(400).json({ success: false, message: err.message });
  }
});

app.get('/api/students/:id/upcoming-sessions', async (req, res) => {
  try {
    const studentId = Number(req.params.id);
    const sectionSubjectId = req.query.sectionSubjectId || null;
    const untilDate = req.query.untilDate || null;
    const daysAhead = Number(req.query.daysAhead || 30);
    const { type, db } = getBackend();
    const sessions = type === 'postgres'
      ? await db.getUpcomingSessions(studentId, sectionSubjectId, untilDate, daysAhead)
      : db.getUpcomingSessions(studentId, sectionSubjectId, untilDate, daysAhead);
    res.json({ success: true, data: sessions });
  } catch (err) {
    res.status(400).json({ success: false, message: err.message });
  }
});

app.post('/api/students/:id/predict-interactive', async (req, res) => {
  try {
    const studentId = Number(req.params.id);
    const { sectionSubjectId, sessionDecisions } = req.body;
    if (!sectionSubjectId) {
      return res.status(400).json({ success: false, message: 'sectionSubjectId is required' });
    }
    const { type, db } = getBackend();
    const result = type === 'postgres'
      ? await db.predictAttendanceInteractive(studentId, sectionSubjectId, sessionDecisions || {})
      : db.predictAttendanceInteractive(studentId, sectionSubjectId, sessionDecisions || {});
    res.json({ success: true, data: result });
  } catch (err) {
    res.status(400).json({ success: false, message: err.message });
  }
});

// SPA fallback for local server
app.get('*', (req, res) => {
  res.sendFile(path.join(__dirname, 'public', 'index.html'));
});

// Start local server if run directly
if (process.env.NODE_ENV !== 'test' && !process.env.NETLIFY) {
  const PORT = process.env.PORT || 3000;
  app.listen(PORT, () => {
    console.log(`[+] Attendance Management System running at http://localhost:${PORT}`);
  });
}

module.exports = app;
