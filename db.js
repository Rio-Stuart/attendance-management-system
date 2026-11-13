// db.js — In-Memory Relational Database Engine for Attendance Management System
// Enforces mandatory schema, constraints, transactional finalization, timetable generation, and predictor formulas.

class AttendanceDB {
  constructor() {
    this.reset();
  }

  reset() {
    this.users = [];
    this.sections = [];
    this.subjects = [];
    this.section_subjects = [];
    this.student_section_enrollments = [];
    this.weekly_timetable = [];
    this.university_holidays = [];
    this.class_sessions = [];
    this.attendance_records = [];
    this.attendance_audit_log = [];

    this.counters = {
      users: 0,
      sections: 0,
      subjects: 0,
      section_subjects: 0,
      student_section_enrollments: 0,
      weekly_timetable: 0,
      university_holidays: 0,
      class_sessions: 0,
      attendance_records: 0,
      attendance_audit_log: 0
    };

    this.seed();
  }

  seed() {
    // 1. Users
    this.addUser({ username: 'admin1', password: 'admin123', full_name: 'Dr. Rajesh Kumar', email: 'rajesh.kumar@university.edu', role: 'ADMIN' });
    this.addUser({ username: 'admin2', password: 'admin123', full_name: 'Prof. Anita Sharma', email: 'anita.sharma@university.edu', role: 'ADMIN' });
    this.addUser({ username: 'student1', password: 'student123', full_name: 'Aarav Patel', email: 'aarav.patel@student.edu', role: 'STUDENT' });
    this.addUser({ username: 'student2', password: 'student123', full_name: 'Priya Singh', email: 'priya.singh@student.edu', role: 'STUDENT' });
    this.addUser({ username: 'student3', password: 'student123', full_name: 'Rohan Gupta', email: 'rohan.gupta@student.edu', role: 'STUDENT' });
    this.addUser({ username: 'student4', password: 'student123', full_name: 'Sneha Reddy', email: 'sneha.reddy@student.edu', role: 'STUDENT' });
    this.addUser({ username: 'student5', password: 'student123', full_name: 'Vikram Joshi', email: 'vikram.joshi@student.edu', role: 'STUDENT' });

    // 2. Sections
    const s1 = this.addSection({ section_name: '25BCS801', academic_year: '2026-2027' });
    const s2 = this.addSection({ section_name: '25BCS802', academic_year: '2026-2027' });

    // 3. Subjects
    const sub1 = this.addSubject({ subject_code: 'CS401', subject_name: 'Database Management Systems' });
    const sub2 = this.addSubject({ subject_code: 'CS402', subject_name: 'Object Oriented Programming' });
    const sub3 = this.addSubject({ subject_code: 'CS403', subject_name: 'Operating Systems' });

    // 4. Section Subjects
    const ss1 = this.addSectionSubject({ section_id: s1.id, subject_id: sub1.id }); // 1: 801 - DBMS
    const ss2 = this.addSectionSubject({ section_id: s1.id, subject_id: sub2.id }); // 2: 801 - OOPS
    const ss3 = this.addSectionSubject({ section_id: s1.id, subject_id: sub3.id }); // 3: 801 - OS
    const ss4 = this.addSectionSubject({ section_id: s2.id, subject_id: sub1.id }); // 4: 802 - DBMS
    const ss5 = this.addSectionSubject({ section_id: s2.id, subject_id: sub2.id }); // 5: 802 - OOPS
    const ss6 = this.addSectionSubject({ section_id: s2.id, subject_id: sub3.id }); // 6: 802 - OS

    // 5. Enrollments (students 3,4,5 in 801; students 6,7 in 802)
    const e1 = this.addEnrollment({ student_id: 3, section_id: s1.id, academic_year: '2026-2027', start_date: '2026-08-01', end_date: null, status: 'ACTIVE' });
    const e2 = this.addEnrollment({ student_id: 4, section_id: s1.id, academic_year: '2026-2027', start_date: '2026-08-01', end_date: null, status: 'ACTIVE' });
    const e3 = this.addEnrollment({ student_id: 5, section_id: s1.id, academic_year: '2026-2027', start_date: '2026-08-01', end_date: null, status: 'ACTIVE' });
    const e4 = this.addEnrollment({ student_id: 6, section_id: s2.id, academic_year: '2026-2027', start_date: '2026-08-01', end_date: null, status: 'ACTIVE' });
    const e5 = this.addEnrollment({ student_id: 7, section_id: s2.id, academic_year: '2026-2027', start_date: '2026-08-01', end_date: null, status: 'ACTIVE' });

    // 6. Weekly Timetable
    this.addTimetableEntry({ section_subject_id: ss1.id, day_of_week: 'MONDAY', start_time: '09:00', end_time: '10:00', valid_from: '2026-08-01', valid_to: '2027-05-31' });
    this.addTimetableEntry({ section_subject_id: ss1.id, day_of_week: 'WEDNESDAY', start_time: '09:00', end_time: '10:00', valid_from: '2026-08-01', valid_to: '2027-05-31' });
    this.addTimetableEntry({ section_subject_id: ss2.id, day_of_week: 'TUESDAY', start_time: '10:00', end_time: '11:00', valid_from: '2026-08-01', valid_to: '2027-05-31' });
    this.addTimetableEntry({ section_subject_id: ss2.id, day_of_week: 'THURSDAY', start_time: '10:00', end_time: '11:00', valid_from: '2026-08-01', valid_to: '2027-05-31' });
    this.addTimetableEntry({ section_subject_id: ss3.id, day_of_week: 'FRIDAY', start_time: '11:00', end_time: '12:00', valid_from: '2026-08-01', valid_to: '2027-05-31' });
    this.addTimetableEntry({ section_subject_id: ss4.id, day_of_week: 'MONDAY', start_time: '11:00', end_time: '12:00', valid_from: '2026-08-01', valid_to: '2027-05-31' });
    this.addTimetableEntry({ section_subject_id: ss5.id, day_of_week: 'WEDNESDAY', start_time: '14:00', end_time: '15:00', valid_from: '2026-08-01', valid_to: '2027-05-31' });
    this.addTimetableEntry({ section_subject_id: ss6.id, day_of_week: 'FRIDAY', start_time: '14:00', end_time: '15:00', valid_from: '2026-08-01', valid_to: '2027-05-31' });

    // 7. Holidays
    this.addHoliday({ holiday_date: '2026-08-15', description: 'Independence Day' });
    this.addHoliday({ holiday_date: '2026-10-02', description: 'Gandhi Jayanti' });
    this.addHoliday({ holiday_date: '2026-10-24', description: 'Dussehra' });
    this.addHoliday({ holiday_date: '2026-11-01', description: 'Diwali' });
    this.addHoliday({ holiday_date: '2026-12-25', description: 'Christmas' });
    this.addHoliday({ holiday_date: '2027-01-26', description: 'Republic Day' });

    // 8. Class Sessions
    // Session 1: FINALIZED (25BCS801 - DBMS - Mon 4 Aug)
    const cs1 = this.addClassSession({
      section_subject_id: ss1.id,
      section_id: s1.id,
      timetable_entry_id: 1,
      session_date: '2026-08-04',
      start_time: '09:00',
      end_time: '10:00',
      status: 'FINALIZED',
      created_by: 1,
      finalized_by: 1,
      finalized_at: '2026-08-04 10:05:00'
    });

    // Session 2: FINALIZED (25BCS801 - DBMS - Wed 6 Aug)
    const cs2 = this.addClassSession({
      section_subject_id: ss1.id,
      section_id: s1.id,
      timetable_entry_id: 2,
      session_date: '2026-08-06',
      start_time: '09:00',
      end_time: '10:00',
      status: 'FINALIZED',
      created_by: 1,
      finalized_by: 1,
      finalized_at: '2026-08-06 10:05:00'
    });

    // Session 3: SCHEDULED (25BCS801 - OOPS - Tue 5 Aug)
    this.addClassSession({
      section_subject_id: ss2.id,
      section_id: s1.id,
      timetable_entry_id: 3,
      session_date: '2026-11-17',
      start_time: '10:00',
      end_time: '11:00',
      status: 'SCHEDULED',
      created_by: 1
    });

    // Session 4: DRAFT (25BCS801 - OOPS - Thu 7 Aug)
    const cs4 = this.addClassSession({
      section_subject_id: ss2.id,
      section_id: s1.id,
      timetable_entry_id: 4,
      session_date: '2026-11-19',
      start_time: '10:00',
      end_time: '11:00',
      status: 'DRAFT',
      created_by: 1
    });

    // Session 5: CANCELLED (25BCS801 - OS - Fri 8 Aug)
    this.addClassSession({
      section_subject_id: ss3.id,
      section_id: s1.id,
      timetable_entry_id: 5,
      session_date: '2026-11-20',
      start_time: '11:00',
      end_time: '12:00',
      status: 'CANCELLED',
      created_by: 1,
      cancellation_reason: 'Faculty on academic conference'
    });

    // 9. Attendance records for finalized session 1
    this.addAttendanceRecord({ class_session_id: cs1.id, student_section_enrollment_id: e1.id, student_id: 3, section_id: s1.id, attendance_status: 'PRESENT', marked_by: 1 });
    this.addAttendanceRecord({ class_session_id: cs1.id, student_section_enrollment_id: e2.id, student_id: 4, section_id: s1.id, attendance_status: 'PRESENT', marked_by: 1 });
    this.addAttendanceRecord({ class_session_id: cs1.id, student_section_enrollment_id: e3.id, student_id: 5, section_id: s1.id, attendance_status: 'ABSENT', marked_by: 1 });

    // Attendance records for finalized session 2
    this.addAttendanceRecord({ class_session_id: cs2.id, student_section_enrollment_id: e1.id, student_id: 3, section_id: s1.id, attendance_status: 'PRESENT', marked_by: 1 });
    this.addAttendanceRecord({ class_session_id: cs2.id, student_section_enrollment_id: e2.id, student_id: 4, section_id: s1.id, attendance_status: 'ABSENT', marked_by: 1 });
    this.addAttendanceRecord({ class_session_id: cs2.id, student_section_enrollment_id: e3.id, student_id: 5, section_id: s1.id, attendance_status: 'PRESENT', marked_by: 1 });

    // Draft records for session 4 (partial)
    this.addAttendanceRecord({ class_session_id: cs4.id, student_section_enrollment_id: e1.id, student_id: 3, section_id: s1.id, attendance_status: 'PRESENT', marked_by: 1 });
    this.addAttendanceRecord({ class_session_id: cs4.id, student_section_enrollment_id: e2.id, student_id: 4, section_id: s1.id, attendance_status: 'PRESENT', marked_by: 1 });

    // Sample audit record
    this.addAuditLog({
      attendance_record_id: 3,
      previous_status: 'PRESENT',
      new_status: 'ABSENT',
      changed_by: 1,
      reason: 'Student left lecture early'
    });
  }

  // ---- Model Adders ----
  addUser(data) {
    const id = ++this.counters.users;
    const user = { id, ...data, is_active: data.is_active ?? true, created_at: new Date().toISOString() };
    this.users.push(user);
    return user;
  }

  addSection(data) {
    const id = ++this.counters.sections;
    const sec = { id, ...data, created_at: new Date().toISOString() };
    this.sections.push(sec);
    return sec;
  }

  addSubject(data) {
    const id = ++this.counters.subjects;
    const subj = { id, ...data, created_at: new Date().toISOString() };
    this.subjects.push(subj);
    return subj;
  }

  addSectionSubject(data) {
    const id = ++this.counters.section_subjects;
    const ss = { id, ...data };
    this.section_subjects.push(ss);
    return ss;
  }

  addEnrollment(data) {
    const id = ++this.counters.student_section_enrollments;
    const e = { id, ...data, created_at: new Date().toISOString() };
    this.student_section_enrollments.push(e);
    return e;
  }

  addTimetableEntry(data) {
    const id = ++this.counters.weekly_timetable;
    const entry = { id, ...data };
    this.weekly_timetable.push(entry);
    return entry;
  }

  addHoliday(data) {
    const id = ++this.counters.university_holidays;
    const h = { id, ...data };
    this.university_holidays.push(h);
    return h;
  }

  addClassSession(data) {
    const id = ++this.counters.class_sessions;
    const cs = {
      id,
      ...data,
      status: data.status || 'SCHEDULED',
      created_at: data.created_at || new Date().toISOString()
    };
    this.class_sessions.push(cs);
    return cs;
  }

  addAttendanceRecord(data) {
    const id = ++this.counters.attendance_records;
    const r = {
      id,
      ...data,
      marked_at: data.marked_at || new Date().toISOString(),
      updated_at: new Date().toISOString()
    };
    this.attendance_records.push(r);
    return r;
  }

  addAuditLog(data) {
    const id = ++this.counters.attendance_audit_log;
    const log = { id, ...data, changed_at: new Date().toISOString() };
    this.attendance_audit_log.push(log);
    return log;
  }

  // ---- Query & Verification Helpers ----

  isHoliday(dateStr) {
    return this.university_holidays.some(h => h.holiday_date === dateStr);
  }

  findActiveEnrollmentsOnDate(sectionId, dateStr) {
    const targetDate = new Date(dateStr);
    return this.student_section_enrollments.filter(e => {
      if (e.section_id !== Number(sectionId) || e.status !== 'ACTIVE') return false;
      const start = new Date(e.start_date);
      if (start > targetDate) return false;
      if (e.end_date) {
        const end = new Date(e.end_date);
        if (end < targetDate) return false;
      }
      return true;
    });
  }

  // ---- Session Operations ----

  createSession({ section_subject_id, section_id, session_date, start_time, end_time, timetable_entry_id, created_by }) {
    if (!session_date || !start_time || !end_time) {
      throw new Error('Date, start time, and end time are required');
    }
    if (start_time >= end_time) {
      throw new Error('Start time must be before end time');
    }

    // Unique constraint: (section_subject_id, session_date, start_time)
    const duplicate = this.class_sessions.find(cs =>
      cs.section_subject_id === Number(section_subject_id) &&
      cs.session_date === session_date &&
      cs.start_time === start_time
    );
    if (duplicate) {
      throw new Error(`A session already exists for this subject on ${session_date} at ${start_time}`);
    }

    return this.addClassSession({
      section_subject_id: Number(section_subject_id),
      section_id: Number(section_id),
      timetable_entry_id: timetable_entry_id ? Number(timetable_entry_id) : null,
      session_date,
      start_time,
      end_time,
      status: 'SCHEDULED',
      created_by: Number(created_by)
    });
  }

  cancelSession(sessionId, reason) {
    const session = this.class_sessions.find(cs => cs.id === Number(sessionId));
    if (!session) throw new Error('Session not found');
    if (session.status === 'FINALIZED') throw new Error('Cannot cancel a FINALIZED session');
    if (session.status === 'CANCELLED') throw new Error('Session is already cancelled');
    if (!reason || !reason.trim()) throw new Error('Cancellation reason is required');

    session.status = 'CANCELLED';
    session.cancellation_reason = reason.trim();
    return session;
  }

  // ---- Attendance Operations ----

  getRosterForSession(sessionId) {
    const session = this.class_sessions.find(cs => cs.id === Number(sessionId));
    if (!session) throw new Error('Session not found');

    const enrollments = this.findActiveEnrollmentsOnDate(session.section_id, session.session_date);
    const existingRecords = this.attendance_records.filter(r => r.class_session_id === session.id);

    return enrollments.map(e => {
      const student = this.users.find(u => u.id === e.student_id);
      const record = existingRecords.find(r => r.student_section_enrollment_id === e.id);
      return {
        enrollment_id: e.id,
        student_id: e.student_id,
        student_name: student ? student.full_name : 'Unknown',
        roll_number: student ? student.username : '',
        status: record ? record.attendance_status : null,
        record_id: record ? record.id : null
      };
    });
  }

  saveDraft(sessionId, records, adminId) {
    const session = this.class_sessions.find(cs => cs.id === Number(sessionId));
    if (!session) throw new Error('Session not found');
    if (session.status === 'FINALIZED') throw new Error('Cannot edit a FINALIZED session as draft');
    if (session.status === 'CANCELLED') throw new Error('Cannot mark attendance for a CANCELLED session');

    for (const item of records) {
      const existing = this.attendance_records.find(r =>
        r.class_session_id === session.id &&
        r.student_section_enrollment_id === Number(item.enrollment_id)
      );

      if (existing) {
        existing.attendance_status = item.status;
        existing.marked_by = Number(adminId);
        existing.updated_at = new Date().toISOString();
      } else {
        const enrollment = this.student_section_enrollments.find(e => e.id === Number(item.enrollment_id));
        if (!enrollment) continue;
        this.addAttendanceRecord({
          class_session_id: session.id,
          student_section_enrollment_id: enrollment.id,
          student_id: enrollment.student_id,
          section_id: session.section_id,
          attendance_status: item.status,
          marked_by: Number(adminId)
        });
      }
    }

    if (session.status === 'SCHEDULED') {
      session.status = 'DRAFT';
    }
    return { success: true, count: records.length };
  }

  finalizeAttendance(sessionId, adminId) {
    const session = this.class_sessions.find(cs => cs.id === Number(sessionId));
    if (!session) throw new Error('Session not found');
    if (session.status === 'FINALIZED') throw new Error('Session is already FINALIZED');
    if (session.status === 'CANCELLED') throw new Error('Cannot finalize a CANCELLED session');

    // 1. Get eligible active roster
    const eligibleEnrollments = this.findActiveEnrollmentsOnDate(session.section_id, session.session_date);
    if (eligibleEnrollments.length === 0) {
      throw new Error('No eligible students found for this session');
    }

    // 2. Fetch existing records
    const sessionRecords = this.attendance_records.filter(r => r.class_session_id === session.id);
    const recordedEnrollmentIds = new Set(sessionRecords.map(r => r.student_section_enrollment_id));
    const eligibleEnrollmentIds = new Set(eligibleEnrollments.map(e => e.id));

    // 3. Completeness check
    const missing = eligibleEnrollments.filter(e => !recordedEnrollmentIds.has(e.id));
    if (missing.length > 0) {
      throw new Error(`Incomplete attendance: ${missing.length} eligible student(s) have not been marked.`);
    }

    // 4. Ineligibility check
    const ineligible = sessionRecords.filter(r => !eligibleEnrollmentIds.has(r.student_section_enrollment_id));
    if (ineligible.length > 0) {
      throw new Error(`Invalid attendance: ${ineligible.length} record(s) belong to ineligible students.`);
    }

    // 5. Atomic state update
    session.status = 'FINALIZED';
    session.finalized_by = Number(adminId);
    session.finalized_at = new Date().toISOString();

    return session;
  }

  editFinalizedAttendance(recordId, newStatus, adminId, reason) {
    const record = this.attendance_records.find(r => r.id === Number(recordId));
    if (!record) throw new Error('Attendance record not found');

    const session = this.class_sessions.find(cs => cs.id === record.class_session_id);
    if (!session || session.status !== 'FINALIZED') {
      throw new Error('Can only edit records for FINALIZED class sessions');
    }
    if (!reason || !reason.trim()) {
      throw new Error('Reason is required when modifying finalized attendance');
    }

    const previousStatus = record.attendance_status;
    record.attendance_status = newStatus;
    record.updated_at = new Date().toISOString();

    this.addAuditLog({
      attendance_record_id: record.id,
      previous_status: previousStatus,
      new_status: newStatus,
      changed_by: Number(adminId),
      reason: reason.trim()
    });

    return record;
  }

  // ---- Calculations & Dashboard ----

  calculateSubjectAttendance(studentId, sectionSubjectId) {
    const sid = Number(studentId);
    const ssid = Number(sectionSubjectId);

    // Active or historical enrollment check
    const enrollments = this.student_section_enrollments.filter(e => e.student_id === sid);
    if (enrollments.length === 0) return { conducted: 0, attended: 0, missed: 0, percentage: 0 };

    // Find all FINALIZED sessions for this section_subject within student's enrollment period
    let conducted = 0;
    let attended = 0;

    const finalizedSessions = this.class_sessions.filter(cs =>
      cs.section_subject_id === ssid && cs.status === 'FINALIZED'
    );

    for (const session of finalizedSessions) {
      const validEnrollment = enrollments.find(e => {
        if (e.section_id !== session.section_id) return false;
        const sDate = new Date(session.session_date);
        const start = new Date(e.start_date);
        if (sDate < start) return false;
        if (e.end_date && sDate > new Date(e.end_date)) return false;
        return true;
      });

      if (validEnrollment) {
        conducted++;
        const record = this.attendance_records.find(r =>
          r.class_session_id === session.id &&
          r.student_id === sid &&
          r.attendance_status === 'PRESENT'
        );
        if (record) attended++;
      }
    }

    const missed = conducted - attended;
    const percentage = conducted > 0 ? Number(((attended / conducted) * 100).toFixed(2)) : 0;
    return { conducted, attended, missed, percentage };
  }

  calculateOverallAttendance(studentId, sectionId) {
    const sid = Number(studentId);
    const secId = Number(sectionId);

    const sectionSubjects = this.section_subjects.filter(ss => ss.section_id === secId);
    let totalConducted = 0;
    let totalAttended = 0;

    for (const ss of sectionSubjects) {
      const stats = this.calculateSubjectAttendance(sid, ss.id);
      totalConducted += stats.conducted;
      totalAttended += stats.attended;
    }

    const overallPercentage = totalConducted > 0
      ? Number(((totalAttended / totalConducted) * 100).toFixed(2))
      : 0;

    return { totalConducted, totalAttended, overallPercentage };
  }

  getStudentDashboard(studentId) {
    const student = this.users.find(u => u.id === Number(studentId));
    if (!student) throw new Error('Student not found');

    const activeEnrollment = this.student_section_enrollments.find(e =>
      e.student_id === student.id && e.status === 'ACTIVE'
    );
    if (!activeEnrollment) throw new Error('No active enrollment found for this student');

    const section = this.sections.find(s => s.id === activeEnrollment.section_id);
    const sectionSubjects = this.section_subjects.filter(ss => ss.section_id === activeEnrollment.section_id);

    const subjectStats = sectionSubjects.map(ss => {
      const subj = this.subjects.find(s => s.id === ss.subject_id);
      const stats = this.calculateSubjectAttendance(student.id, ss.id);
      return {
        section_subject_id: ss.id,
        subject_code: subj ? subj.subject_code : '',
        subject_name: subj ? subj.subject_name : 'Unknown',
        ...stats
      };
    });

    const overall = this.calculateOverallAttendance(student.id, activeEnrollment.section_id);

    return {
      student_id: student.id,
      student_name: student.full_name,
      roll_number: student.username,
      section_id: activeEnrollment.section_id,
      section_name: section ? section.section_name : 'Unknown',
      academic_year: activeEnrollment.academic_year,
      overall_percentage: overall.overallPercentage,
      total_conducted: overall.totalConducted,
      total_attended: overall.totalAttended,
      subjects: subjectStats
    };
  }

  getSectionReport(sectionId) {
    const secId = Number(sectionId);
    const enrollments = this.student_section_enrollments.filter(e => e.section_id === secId && e.status === 'ACTIVE');

    return enrollments.map(e => {
      const student = this.users.find(u => u.id === e.student_id);
      const overall = this.calculateOverallAttendance(e.student_id, secId);
      return {
        student_id: e.student_id,
        roll_number: student ? student.username : '',
        student_name: student ? student.full_name : 'Unknown',
        overall_percentage: overall.overallPercentage,
        total_conducted: overall.totalConducted,
        total_attended: overall.totalAttended
      };
    });
  }

  getBelowThresholdReport(sectionId, threshold = 75) {
    const secId = Number(sectionId);
    const enrollments = this.student_section_enrollments.filter(e => e.section_id === secId && e.status === 'ACTIVE');
    const sectionSubjects = this.section_subjects.filter(ss => ss.section_id === secId);
    const report = [];

    for (const e of enrollments) {
      const student = this.users.find(u => u.id === e.student_id);
      for (const ss of sectionSubjects) {
        const stats = this.calculateSubjectAttendance(e.student_id, ss.id);
        if (stats.conducted > 0 && stats.percentage < threshold) {
          const subj = this.subjects.find(s => s.id === ss.subject_id);
          report.push({
            student_id: e.student_id,
            roll_number: student ? student.username : '',
            student_name: student ? student.full_name : 'Unknown',
            subject_name: subj ? subj.subject_name : '',
            subject_code: subj ? subj.subject_code : '',
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

  // ---- Idempotent Timetable Generation ----

  generateTimetableSessions(sectionId, daysAhead = 30) {
    const secId = Number(sectionId);
    const sectionSubjects = this.section_subjects.filter(ss => ss.section_id === secId);
    const today = new Date();
    const days = ['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'];
    const generated = [];

    for (let i = 0; i < daysAhead; i++) {
      const d = new Date(today);
      d.setDate(today.getDate() + i);
      const dateStr = d.toISOString().split('T')[0];
      const dayName = days[d.getDay()];

      // Skip holidays
      if (this.isHoliday(dateStr)) continue;

      for (const ss of sectionSubjects) {
        const matchingEntries = this.weekly_timetable.filter(wt =>
          wt.section_subject_id === ss.id &&
          wt.day_of_week === dayName &&
          (!wt.valid_from || dateStr >= wt.valid_from) &&
          (!wt.valid_to || dateStr <= wt.valid_to)
        );

        for (const entry of matchingEntries) {
          // Idempotent uniqueness check on (section_subject_id, session_date, start_time)
          const exists = this.class_sessions.some(cs =>
            cs.section_subject_id === ss.id &&
            cs.session_date === dateStr &&
            cs.start_time === entry.start_time
          );

          if (!exists) {
            const newSession = this.addClassSession({
              section_subject_id: ss.id,
              section_id: secId,
              timetable_entry_id: entry.id,
              session_date: dateStr,
              start_time: entry.start_time,
              end_time: entry.end_time,
              status: 'SCHEDULED',
              created_by: 1
            });
            generated.push(newSession);
          }
        }
      }
    }
    return generated;
  }

  // ---- Attendance Predictor ----

  predictAttendance(studentId, sectionSubjectId, daysAhead = 30, scenarioAttended = null) {
    const sid = Number(studentId);
    const ssid = Number(sectionSubjectId);

    const stats = this.calculateSubjectAttendance(sid, ssid);
    const P = stats.attended;
    const T = stats.conducted;

    const todayStr = new Date().toISOString().split('T')[0];
    const maxDate = new Date();
    maxDate.setDate(maxDate.getDate() + daysAhead);
    const maxDateStr = maxDate.toISOString().split('T')[0];

    // Upcoming SCHEDULED sessions
    const upcoming = this.class_sessions.filter(cs =>
      cs.section_subject_id === ssid &&
      cs.status === 'SCHEDULED' &&
      cs.session_date >= todayStr &&
      cs.session_date <= maxDateStr &&
      !this.isHoliday(cs.session_date)
    );

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

const db = new AttendanceDB();
module.exports = db;
