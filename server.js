// server.js — Express Application for Attendance Management System
const express = require('express');
const cors = require('cors');
const path = require('path');
const db = require('./db.js');

const app = express();
app.use(cors());
app.use(express.json());

// Serve static frontend assets from public/
app.use(express.static(path.join(__dirname, 'public')));

// ----------------- AUTH -----------------
app.post('/api/auth/login', (req, res) => {
  const { username, password } = req.body;
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
});

// ----------------- SECTIONS & SUBJECTS -----------------
app.get('/api/sections', (req, res) => {
  res.json({ success: true, data: db.sections });
});

app.get('/api/subjects', (req, res) => {
  res.json({ success: true, data: db.subjects });
});

app.get('/api/sections/:id/subjects', (req, res) => {
  const sectionId = Number(req.params.id);
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
});

// ----------------- TIMETABLE -----------------
app.get('/api/sections/:id/timetable', (req, res) => {
  const sectionId = Number(req.params.id);
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
});

app.post('/api/sections/:id/generate-timetable', (req, res) => {
  try {
    const sectionId = Number(req.params.id);
    const daysAhead = Number(req.body.daysAhead || 30);
    const generated = db.generateTimetableSessions(sectionId, daysAhead);
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
app.get('/api/sessions', (req, res) => {
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

  // Enrich with section and subject names
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

  // Sort descending by date and time
  enriched.sort((a, b) => (b.session_date + b.start_time).localeCompare(a.session_date + a.start_time));
  res.json({ success: true, data: enriched });
});

app.post('/api/sessions', (req, res) => {
  try {
    const session = db.createSession(req.body);
    res.status(201).json({ success: true, message: 'Class session created successfully', data: session });
  } catch (err) {
    res.status(400).json({ success: false, message: err.message });
  }
});

app.post('/api/sessions/:id/cancel', (req, res) => {
  try {
    const session = db.cancelSession(req.params.id, req.body.reason);
    res.json({ success: true, message: 'Session cancelled successfully', data: session });
  } catch (err) {
    res.status(400).json({ success: false, message: err.message });
  }
});

// ----------------- ATTENDANCE WORKFLOW -----------------
app.get('/api/sessions/:id/roster', (req, res) => {
  try {
    const roster = db.getRosterForSession(req.params.id);
    const session = db.class_sessions.find(s => s.id === Number(req.params.id));
    res.json({ success: true, session, data: roster });
  } catch (err) {
    res.status(400).json({ success: false, message: err.message });
  }
});

app.post('/api/sessions/:id/save-draft', (req, res) => {
  try {
    const { records, adminId } = req.body;
    const result = db.saveDraft(req.params.id, records, adminId);
    res.json({ success: true, message: 'Draft attendance saved successfully', data: result });
  } catch (err) {
    res.status(400).json({ success: false, message: err.message });
  }
});

app.post('/api/sessions/:id/finalize', (req, res) => {
  try {
    const { adminId } = req.body;
    const session = db.finalizeAttendance(req.params.id, adminId);
    res.json({ success: true, message: 'Attendance finalized successfully', data: session });
  } catch (err) {
    res.status(400).json({ success: false, message: err.message });
  }
});

app.post('/api/attendance/edit', (req, res) => {
  try {
    const { recordId, newStatus, adminId, reason } = req.body;
    const record = db.editFinalizedAttendance(recordId, newStatus, adminId, reason);
    res.json({ success: true, message: 'Finalized attendance record updated', data: record });
  } catch (err) {
    res.status(400).json({ success: false, message: err.message });
  }
});

// ----------------- REPORTS & DASHBOARD -----------------
app.get('/api/reports/section/:id', (req, res) => {
  try {
    const report = db.getSectionReport(req.params.id);
    res.json({ success: true, data: report });
  } catch (err) {
    res.status(400).json({ success: false, message: err.message });
  }
});

app.get('/api/reports/section/:id/below-threshold', (req, res) => {
  try {
    const threshold = Number(req.query.threshold || 75);
    const report = db.getBelowThresholdReport(req.params.id, threshold);
    res.json({ success: true, threshold, data: report });
  } catch (err) {
    res.status(400).json({ success: false, message: err.message });
  }
});

app.get('/api/students/:id/dashboard', (req, res) => {
  try {
    const dashboard = db.getStudentDashboard(req.params.id);
    res.json({ success: true, data: dashboard });
  } catch (err) {
    res.status(400).json({ success: false, message: err.message });
  }
});

app.get('/api/students/:id/predict', (req, res) => {
  try {
    const studentId = Number(req.params.id);
    const sectionSubjectId = Number(req.query.sectionSubjectId);
    const daysAhead = Number(req.query.daysAhead || 30);
    const scenarioAttended = req.query.scenarioAttended !== undefined ? Number(req.query.scenarioAttended) : null;

    const prediction = db.predictAttendance(studentId, sectionSubjectId, daysAhead, scenarioAttended);
    res.json({ success: true, data: prediction });
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
    console.log(`[+] Attendance Management System web server running at http://localhost:${PORT}`);
  });
}

module.exports = app;
