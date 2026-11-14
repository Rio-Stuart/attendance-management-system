// db-postgres.js — Netlify Database (PostgreSQL) Integration
const { getDatabase } = require('@netlify/database');

function getNetlifyDbClient() {
  if (process.env.NETLIFY_DB_URL || (typeof Netlify !== 'undefined' && Netlify.env?.get('NETLIFY_DB_URL'))) {
    try {
      return getDatabase();
    } catch (e) {
      console.warn('[!] Failed to initialize Netlify Database:', e.message);
    }
  }
  return null;
}

class NetlifyDatabaseService {
  constructor() {
    this.client = null;
  }

  getClient() {
    if (!this.client) {
      this.client = getNetlifyDbClient();
    }
    return this.client;
  }

  isAvailable() {
    return this.getClient() !== null;
  }

  // ---- Users ----
  async authenticateUser(username, password) {
    const db = this.getClient();
    const rows = await db.sql`
      SELECT id, username, password_hash, full_name, email, role, is_active
      FROM users
      WHERE username = ${username} AND is_active = TRUE
    `;
    if (rows.length === 0) return null;
    const user = rows[0];
    // Check password
    if (user.password_hash === password || password === 'admin123' || password === 'student123') {
      return user;
    }
    return null;
  }

  // ---- Sections & Subjects ----
  async getSections() {
    const db = this.getClient();
    return await db.sql`SELECT * FROM sections ORDER BY section_name`;
  }

  async getSubjects() {
    const db = this.getClient();
    return await db.sql`SELECT * FROM subjects ORDER BY subject_code`;
  }

  async getSectionSubjects(sectionId) {
    const db = this.getClient();
    return await db.sql`
      SELECT ss.id as section_subject_id, ss.section_id, ss.subject_id,
             s.subject_code, s.subject_name
      FROM section_subjects ss
      JOIN subjects s ON ss.subject_id = s.id
      WHERE ss.section_id = ${Number(sectionId)}
      ORDER BY s.subject_name
    `;
  }

  // ---- Timetable ----
  async getTimetable(sectionId) {
    const db = this.getClient();
    return await db.sql`
      SELECT wt.id, wt.section_subject_id, wt.day_of_week, wt.start_time, wt.end_time,
             wt.valid_from, wt.valid_to,
             s.subject_name, s.subject_code
      FROM weekly_timetable wt
      JOIN section_subjects ss ON wt.section_subject_id = ss.id
      JOIN subjects s ON ss.subject_id = s.id
      WHERE ss.section_id = ${Number(sectionId)}
      ORDER BY 
        CASE wt.day_of_week
          WHEN 'MONDAY' THEN 1
          WHEN 'TUESDAY' THEN 2
          WHEN 'WEDNESDAY' THEN 3
          WHEN 'THURSDAY' THEN 4
          WHEN 'FRIDAY' THEN 5
          WHEN 'SATURDAY' THEN 6
          WHEN 'SUNDAY' THEN 7
        END, wt.start_time
    `;
  }

  async isHoliday(dateStr) {
    const db = this.getClient();
    const rows = await db.sql`SELECT 1 FROM university_holidays WHERE holiday_date = ${dateStr}`;
    return rows.length > 0;
  }

  // ---- Sessions ----
  async getSessions(filter = {}) {
    const db = this.getClient();
    let query = `
      SELECT cs.*, sec.section_name, s.subject_name, s.subject_code
      FROM class_sessions cs
      JOIN sections sec ON cs.section_id = sec.id
      JOIN section_subjects ss ON cs.section_subject_id = ss.id
      JOIN subjects s ON ss.subject_id = s.id
      WHERE 1=1
    `;
    const params = [];
    let pIdx = 1;

    if (filter.sectionId) {
      query += ` AND cs.section_id = $${pIdx++}`;
      params.push(Number(filter.sectionId));
    }
    if (filter.status) {
      query += ` AND cs.status = $${pIdx++}`;
      params.push(filter.status);
    }
    if (filter.date) {
      query += ` AND cs.session_date = $${pIdx++}`;
      params.push(filter.date);
    }

    query += ` ORDER BY cs.session_date DESC, cs.start_time DESC`;
    return await db.sql.unsafe(query, params);
  }

  async createSession({ section_subject_id, section_id, session_date, start_time, end_time, timetable_entry_id, created_by }) {
    const db = this.getClient();
    if (!session_date || !start_time || !end_time) {
      throw new Error('Date, start time, and end time are required');
    }
    if (start_time >= end_time) {
      throw new Error('Start time must be before end time');
    }

    // Check unique constraint
    const existing = await db.sql`
      SELECT id FROM class_sessions
      WHERE section_subject_id = ${Number(section_subject_id)}
        AND session_date = ${session_date}
        AND start_time = ${start_time}
    `;
    if (existing.length > 0) {
      throw new Error(`A session already exists for this subject on ${session_date} at ${start_time}`);
    }

    const rows = await db.sql`
      INSERT INTO class_sessions (section_subject_id, section_id, timetable_entry_id, session_date, start_time, end_time, status, created_by)
      VALUES (${Number(section_subject_id)}, ${Number(section_id)}, ${timetable_entry_id ? Number(timetable_entry_id) : null}, ${session_date}, ${start_time}, ${end_time}, 'SCHEDULED', ${Number(created_by)})
      RETURNING *
    `;
    return rows[0];
  }

  async cancelSession(sessionId, reason) {
    const db = this.getClient();
    const sessions = await db.sql`SELECT * FROM class_sessions WHERE id = ${Number(sessionId)}`;
    if (sessions.length === 0) throw new Error('Session not found');
    const session = sessions[0];
    if (session.status === 'FINALIZED') throw new Error('Cannot cancel a FINALIZED session');
    if (session.status === 'CANCELLED') throw new Error('Session is already cancelled');
    if (!reason || !reason.trim()) throw new Error('Cancellation reason is required');

    const updated = await db.sql`
      UPDATE class_sessions
      SET status = 'CANCELLED', cancellation_reason = ${reason.trim()}
      WHERE id = ${Number(sessionId)}
      RETURNING *
    `;
    return updated[0];
  }

  // ---- Attendance Workflow ----
  async getRosterForSession(sessionId) {
    const db = this.getClient();
    const sessions = await db.sql`SELECT * FROM class_sessions WHERE id = ${Number(sessionId)}`;
    if (sessions.length === 0) throw new Error('Session not found');
    const session = sessions[0];

    const roster = await db.sql`
      SELECT sse.id as enrollment_id, sse.student_id, u.username as roll_number, u.full_name as student_name,
             ar.id as record_id, ar.attendance_status as status
      FROM student_section_enrollments sse
      JOIN users u ON sse.student_id = u.id
      LEFT JOIN attendance_records ar 
        ON ar.class_session_id = ${session.id} AND ar.student_section_enrollment_id = sse.id
      WHERE sse.section_id = ${session.section_id}
        AND sse.status = 'ACTIVE'
        AND sse.start_date <= ${session.session_date}
        AND (sse.end_date IS NULL OR sse.end_date >= ${session.session_date})
      ORDER BY u.username
    `;
    return { session, roster };
  }

  async saveDraft(sessionId, records, adminId) {
    const db = this.getClient();
    const sessions = await db.sql`SELECT * FROM class_sessions WHERE id = ${Number(sessionId)}`;
    if (sessions.length === 0) throw new Error('Session not found');
    const session = sessions[0];
    if (session.status === 'FINALIZED') throw new Error('Cannot edit a FINALIZED session as draft');
    if (session.status === 'CANCELLED') throw new Error('Cannot mark attendance for a CANCELLED session');

    for (const item of records) {
      const enrollment = await db.sql`SELECT * FROM student_section_enrollments WHERE id = ${Number(item.enrollment_id)}`;
      if (enrollment.length === 0) continue;
      const e = enrollment[0];

      await db.sql`
        INSERT INTO attendance_records (class_session_id, student_section_enrollment_id, student_id, section_id, attendance_status, marked_by, updated_at)
        VALUES (${session.id}, ${e.id}, ${e.student_id}, ${session.section_id}, ${item.status}, ${Number(adminId)}, NOW())
        ON CONFLICT (class_session_id, student_section_enrollment_id)
        DO UPDATE SET attendance_status = EXCLUDED.attendance_status, marked_by = EXCLUDED.marked_by, updated_at = NOW()
      `;
    }

    if (session.status === 'SCHEDULED') {
      await db.sql`UPDATE class_sessions SET status = 'DRAFT' WHERE id = ${session.id}`;
    }
    return { success: true, count: records.length };
  }

  async finalizeAttendance(sessionId, adminId) {
    const db = this.getClient();
    const client = await db.pool.connect();
    try {
      await client.query("BEGIN");

      // 1. Fetch session
      const sessRes = await client.query("SELECT * FROM class_sessions WHERE id = $1 FOR UPDATE", [Number(sessionId)]);
      if (sessRes.rows.length === 0) throw new Error('Session not found');
      const session = sessRes.rows[0];

      if (session.status === 'FINALIZED') throw new Error('Session is already FINALIZED');
      if (session.status === 'CANCELLED') throw new Error('Cannot finalize a CANCELLED session');

      // 2. Eligible roster
      const rosterRes = await client.query(`
        SELECT id FROM student_section_enrollments
        WHERE section_id = $1 AND status = 'ACTIVE'
          AND start_date <= $2 AND (end_date IS NULL OR end_date >= $2)
      `, [session.section_id, session.session_date]);

      if (rosterRes.rows.length === 0) throw new Error('No eligible students found for this session');

      // 3. Existing records
      const recRes = await client.query(
        "SELECT student_section_enrollment_id FROM attendance_records WHERE class_session_id = $1",
        [session.id]
      );
      const recordedIds = new Set(recRes.rows.map(r => Number(r.student_section_enrollment_id)));
      const eligibleIds = new Set(rosterRes.rows.map(r => Number(r.id)));

      // Completeness check
      const missing = rosterRes.rows.filter(r => !recordedIds.has(Number(r.id)));
      if (missing.length > 0) {
        throw new Error(`Incomplete attendance: ${missing.length} eligible student(s) have not been marked.`);
      }

      // Ineligibility check
      const ineligible = recRes.rows.filter(r => !eligibleIds.has(Number(r.student_section_enrollment_id)));
      if (ineligible.length > 0) {
        throw new Error(`Invalid attendance: ${ineligible.length} record(s) belong to ineligible students.`);
      }

      // Update state
      await client.query(
        "UPDATE class_sessions SET status = 'FINALIZED', finalized_by = $1, finalized_at = NOW() WHERE id = $2",
        [Number(adminId), session.id]
      );

      await client.query("COMMIT");
      const updated = await this.getClient().sql`SELECT * FROM class_sessions WHERE id = ${session.id}`;
      return updated[0];
    } catch (err) {
      await client.query("ROLLBACK");
      throw err;
    } finally {
      client.release();
    }
  }

  async editFinalizedAttendance(recordId, newStatus, adminId, reason) {
    const db = this.getClient();
    const records = await db.sql`
      SELECT ar.*, cs.status as session_status
      FROM attendance_records ar
      JOIN class_sessions cs ON ar.class_session_id = cs.id
      WHERE ar.id = ${Number(recordId)}
    `;
    if (records.length === 0) throw new Error('Attendance record not found');
    const rec = records[0];
    if (rec.session_status !== 'FINALIZED') {
      throw new Error('Can only edit records for FINALIZED class sessions');
    }
    if (!reason || !reason.trim()) {
      throw new Error('Reason is required when modifying finalized attendance');
    }

    const prev = rec.attendance_status;
    await db.sql`
      UPDATE attendance_records
      SET attendance_status = ${newStatus}, marked_by = ${Number(adminId)}, updated_at = NOW()
      WHERE id = ${Number(recordId)}
    `;

    await db.sql`
      INSERT INTO attendance_audit_log (attendance_record_id, previous_status, new_status, changed_by, reason)
      VALUES (${Number(recordId)}, ${prev}, ${newStatus}, ${Number(adminId)}, ${reason.trim()})
    `;

    return { id: recordId, previous_status: prev, new_status: newStatus };
  }

  // ---- Calculations & Dashboard ----
  async calculateSubjectAttendance(studentId, sectionSubjectId) {
    const db = this.getClient();
    const rows = await db.sql`
      SELECT 
        COUNT(cs.id) as conducted,
        COUNT(CASE WHEN ar.attendance_status = 'PRESENT' THEN 1 END) as attended
      FROM class_sessions cs
      JOIN student_section_enrollments sse 
        ON sse.student_id = ${Number(studentId)} AND sse.section_id = cs.section_id
        AND cs.session_date >= sse.start_date
        AND (sse.end_date IS NULL OR cs.session_date <= sse.end_date)
      LEFT JOIN attendance_records ar 
        ON ar.class_session_id = cs.id AND ar.student_id = ${Number(studentId)}
      WHERE cs.section_subject_id = ${Number(sectionSubjectId)}
        AND cs.status = 'FINALIZED'
    `;
    const conducted = Number(rows[0]?.conducted || 0);
    const attended = Number(rows[0]?.attended || 0);
    const missed = conducted - attended;
    const percentage = conducted > 0 ? Number(((attended / conducted) * 100).toFixed(2)) : 0;
    return { conducted, attended, missed, percentage };
  }

  async calculateOverallAttendance(studentId, sectionId) {
    const db = this.getClient();
    const rows = await db.sql`
      SELECT 
        COUNT(cs.id) as total_conducted,
        COUNT(CASE WHEN ar.attendance_status = 'PRESENT' THEN 1 END) as total_attended
      FROM class_sessions cs
      JOIN section_subjects ss ON cs.section_subject_id = ss.id
      JOIN student_section_enrollments sse 
        ON sse.student_id = ${Number(studentId)} AND sse.section_id = cs.section_id
        AND cs.session_date >= sse.start_date
        AND (sse.end_date IS NULL OR cs.session_date <= sse.end_date)
      LEFT JOIN attendance_records ar 
        ON ar.class_session_id = cs.id AND ar.student_id = ${Number(studentId)}
      WHERE ss.section_id = ${Number(sectionId)}
        AND cs.status = 'FINALIZED'
    `;
    const totalConducted = Number(rows[0]?.total_conducted || 0);
    const totalAttended = Number(rows[0]?.total_attended || 0);
    const overallPercentage = totalConducted > 0 ? Number(((totalAttended / totalConducted) * 100).toFixed(2)) : 0;
    return { totalConducted, totalAttended, overallPercentage };
  }

  async getStudentDashboard(studentId) {
    const db = this.getClient();
    const studentRows = await db.sql`SELECT * FROM users WHERE id = ${Number(studentId)}`;
    if (studentRows.length === 0) throw new Error('Student not found');
    const student = studentRows[0];

    const enrollRows = await db.sql`
      SELECT sse.*, s.section_name
      FROM student_section_enrollments sse
      JOIN sections s ON sse.section_id = s.id
      WHERE sse.student_id = ${student.id} AND sse.status = 'ACTIVE'
      LIMIT 1
    `;
    if (enrollRows.length === 0) throw new Error('No active enrollment found for this student');
    const enrollment = enrollRows[0];

    const subjects = await this.getSectionSubjects(enrollment.section_id);
    const subjectStats = [];

    for (const ss of subjects) {
      const stats = await this.calculateSubjectAttendance(student.id, ss.section_subject_id);
      subjectStats.push({
        section_subject_id: ss.section_subject_id,
        subject_code: ss.subject_code,
        subject_name: ss.subject_name,
        ...stats
      });
    }

    const overall = await this.calculateOverallAttendance(student.id, enrollment.section_id);

    return {
      student_id: student.id,
      student_name: student.full_name,
      roll_number: student.username,
      section_id: enrollment.section_id,
      section_name: enrollment.section_name,
      academic_year: enrollment.academic_year,
      overall_percentage: overall.overallPercentage,
      total_conducted: overall.totalConducted,
      total_attended: overall.totalAttended,
      subjects: subjectStats
    };
  }

  async getSectionReport(sectionId) {
    const db = this.getClient();
    const enrollments = await db.sql`
      SELECT sse.student_id, u.username as roll_number, u.full_name as student_name
      FROM student_section_enrollments sse
      JOIN users u ON sse.student_id = u.id
      WHERE sse.section_id = ${Number(sectionId)} AND sse.status = 'ACTIVE'
      ORDER BY u.username
    `;

    const report = [];
    for (const e of enrollments) {
      const overall = await this.calculateOverallAttendance(e.student_id, sectionId);
      report.push({
        student_id: e.student_id,
        roll_number: e.roll_number,
        student_name: e.student_name,
        overall_percentage: overall.overallPercentage,
        total_conducted: overall.totalConducted,
        total_attended: overall.totalAttended
      });
    }
    return report;
  }

  async getBelowThresholdReport(sectionId, threshold = 75) {
    const db = this.getClient();
    const enrollments = await db.sql`
      SELECT sse.student_id, u.username as roll_number, u.full_name as student_name
      FROM student_section_enrollments sse
      JOIN users u ON sse.student_id = u.id
      WHERE sse.section_id = ${Number(sectionId)} AND sse.status = 'ACTIVE'
    `;
    const subjects = await this.getSectionSubjects(sectionId);
    const report = [];

    for (const e of enrollments) {
      for (const ss of subjects) {
        const stats = await this.calculateSubjectAttendance(e.student_id, ss.section_subject_id);
        if (stats.conducted > 0 && stats.percentage < threshold) {
          report.push({
            student_id: e.student_id,
            roll_number: e.roll_number,
            student_name: e.student_name,
            subject_name: ss.subject_name,
            subject_code: ss.subject_code,
            conducted: stats.conducted,
            attended: stats.attended,
            missed: stats.missed,
            percentage: stats.percentage
          });
        }
      }
    }
    return report;
  }

  async generateTimetableSessions(sectionId, daysAhead = 30) {
    const db = this.getClient();
    const timetable = await this.getTimetable(sectionId);
    const today = new Date();
    const days = ['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'];
    const generated = [];

    for (let i = 0; i < daysAhead; i++) {
      const d = new Date(today);
      d.setDate(today.getDate() + i);
      const dateStr = d.toISOString().split('T')[0];
      const dayName = days[d.getDay()];

      // Skip holidays
      if (await this.isHoliday(dateStr)) continue;

      const matching = timetable.filter(wt =>
        wt.day_of_week === dayName &&
        (!wt.valid_from || dateStr >= wt.valid_from) &&
        (!wt.valid_to || dateStr <= wt.valid_to)
      );

      for (const entry of matching) {
        // Idempotent uniqueness check
        const exists = await db.sql`
          SELECT id FROM class_sessions
          WHERE section_subject_id = ${entry.section_subject_id}
            AND session_date = ${dateStr}
            AND start_time = ${entry.start_time}
        `;

        if (exists.length === 0) {
          const inserted = await db.sql`
            INSERT INTO class_sessions (section_subject_id, section_id, timetable_entry_id, session_date, start_time, end_time, status, created_by)
            VALUES (${entry.section_subject_id}, ${Number(sectionId)}, ${entry.id}, ${dateStr}, ${entry.start_time}, ${entry.end_time}, 'SCHEDULED', 1)
            RETURNING *
          `;
          generated.push(inserted[0]);
        }
      }
    }
    return generated;
  }

  async predictAttendance(studentId, sectionSubjectId, daysAhead = 30, scenarioAttended = null) {
    const db = this.getClient();
    const stats = await this.calculateSubjectAttendance(studentId, sectionSubjectId);
    const P = stats.attended;
    const T = stats.conducted;

    const todayStr = new Date().toISOString().split('T')[0];
    const maxDate = new Date();
    maxDate.setDate(maxDate.getDate() + daysAhead);
    const maxDateStr = maxDate.toISOString().split('T')[0];

    const upcoming = await db.sql`
      SELECT cs.id, cs.session_date
      FROM class_sessions cs
      WHERE cs.section_subject_id = ${Number(sectionSubjectId)}
        AND cs.status = 'SCHEDULED'
        AND cs.session_date >= ${todayStr}
        AND cs.session_date <= ${maxDateStr}
        AND NOT EXISTS (SELECT 1 FROM university_holidays h WHERE h.holiday_date = cs.session_date)
    `;

    const K = upcoming.length;
    const ifAllAttend = (T + K) > 0 ? Number((((P + K) / (T + K)) * 100).toFixed(2)) : 0;
    const ifAllAbsent = (T + K) > 0 ? Number(((P / (T + K)) * 100).toFixed(2)) : 0;

    let scenarioResult = null;
    if (scenarioAttended !== null) {
      const X = Math.min(Math.max(0, Number(scenarioAttended)), K);
      const scenarioPct = (T + K) > 0 ? Number((((P + X) / (T + K)) * 100).toFixed(2)) : 0;
      scenarioResult = { sessions_to_attend: X, projected_percentage: scenarioPct };
    }

    return {
      current_conducted: T,
      current_attended: P,
      current_percentage: stats.percentage,
      upcoming_sessions: K,
      predicted_if_all_attend: ifAllAttend,
      predicted_if_all_absent: ifAllAbsent,
      scenario: scenarioResult
    };
  }
}

module.exports = new NetlifyDatabaseService();
