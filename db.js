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

    // 8. Class Sessions & Full Subject Historical Records
    // --- DBMS (CS401, ss1) Finalized Sessions ---
    const cs1 = this.addClassSession({
      section_subject_id: ss1.id, section_id: s1.id, timetable_entry_id: 1,
      session_date: '2026-08-03', start_time: '09:00', end_time: '10:00',
      status: 'FINALIZED', created_by: 1, finalized_by: 1, finalized_at: '2026-08-03 10:05:00'
    });
    const cs2 = this.addClassSession({
      section_subject_id: ss1.id, section_id: s1.id, timetable_entry_id: 2,
      session_date: '2026-08-05', start_time: '09:00', end_time: '10:00',
      status: 'FINALIZED', created_by: 1, finalized_by: 1, finalized_at: '2026-08-05 10:05:00'
    });
    const cs3 = this.addClassSession({
      section_subject_id: ss1.id, section_id: s1.id, timetable_entry_id: 1,
      session_date: '2026-08-10', start_time: '09:00', end_time: '10:00',
      status: 'FINALIZED', created_by: 1, finalized_by: 1, finalized_at: '2026-08-10 10:05:00'
    });
    const cs4 = this.addClassSession({
      section_subject_id: ss1.id, section_id: s1.id, timetable_entry_id: 2,
      session_date: '2026-08-12', start_time: '09:00', end_time: '10:00',
      status: 'FINALIZED', created_by: 1, finalized_by: 1, finalized_at: '2026-08-12 10:05:00'
    });
    const cs5 = this.addClassSession({
      section_subject_id: ss1.id, section_id: s1.id, timetable_entry_id: 1,
      session_date: '2026-08-17', start_time: '09:00', end_time: '10:00',
      status: 'FINALIZED', created_by: 1, finalized_by: 1, finalized_at: '2026-08-17 10:05:00'
    });
    const cs6 = this.addClassSession({
      section_subject_id: ss1.id, section_id: s1.id, timetable_entry_id: 2,
      session_date: '2026-08-19', start_time: '09:00', end_time: '10:00',
      status: 'FINALIZED', created_by: 1, finalized_by: 1, finalized_at: '2026-08-19 10:05:00'
    });

    // --- OOPS (CS402, ss2) Finalized Sessions ---
    const cs7 = this.addClassSession({
      section_subject_id: ss2.id, section_id: s1.id, timetable_entry_id: 3,
      session_date: '2026-08-04', start_time: '10:00', end_time: '11:00',
      status: 'FINALIZED', created_by: 1, finalized_by: 1, finalized_at: '2026-08-04 11:05:00'
    });
    const cs8 = this.addClassSession({
      section_subject_id: ss2.id, section_id: s1.id, timetable_entry_id: 4,
      session_date: '2026-08-06', start_time: '10:00', end_time: '11:00',
      status: 'FINALIZED', created_by: 1, finalized_by: 1, finalized_at: '2026-08-06 11:05:00'
    });
    const cs9 = this.addClassSession({
      section_subject_id: ss2.id, section_id: s1.id, timetable_entry_id: 3,
      session_date: '2026-08-11', start_time: '10:00', end_time: '11:00',
      status: 'FINALIZED', created_by: 1, finalized_by: 1, finalized_at: '2026-08-11 11:05:00'
    });
    const cs10 = this.addClassSession({
      section_subject_id: ss2.id, section_id: s1.id, timetable_entry_id: 4,
      session_date: '2026-08-13', start_time: '10:00', end_time: '11:00',
      status: 'FINALIZED', created_by: 1, finalized_by: 1, finalized_at: '2026-08-13 11:05:00'
    });
    const cs11 = this.addClassSession({
      section_subject_id: ss2.id, section_id: s1.id, timetable_entry_id: 3,
      session_date: '2026-08-18', start_time: '10:00', end_time: '11:00',
      status: 'FINALIZED', created_by: 1, finalized_by: 1, finalized_at: '2026-08-18 11:05:00'
    });

    // --- OS (CS403, ss3) Finalized Sessions ---
    const cs12 = this.addClassSession({
      section_subject_id: ss3.id, section_id: s1.id, timetable_entry_id: 5,
      session_date: '2026-08-07', start_time: '11:00', end_time: '12:00',
      status: 'FINALIZED', created_by: 1, finalized_by: 1, finalized_at: '2026-08-07 12:05:00'
    });
    const cs13 = this.addClassSession({
      section_subject_id: ss3.id, section_id: s1.id, timetable_entry_id: 5,
      session_date: '2026-08-14', start_time: '11:00', end_time: '12:00',
      status: 'FINALIZED', created_by: 1, finalized_by: 1, finalized_at: '2026-08-14 12:05:00'
    });
    const cs14 = this.addClassSession({
      section_subject_id: ss3.id, section_id: s1.id, timetable_entry_id: 5,
      session_date: '2026-08-21', start_time: '11:00', end_time: '12:00',
      status: 'FINALIZED', created_by: 1, finalized_by: 1, finalized_at: '2026-08-21 12:05:00'
    });
    const cs15 = this.addClassSession({
      section_subject_id: ss3.id, section_id: s1.id, timetable_entry_id: 5,
      session_date: '2026-08-28', start_time: '11:00', end_time: '12:00',
      status: 'FINALIZED', created_by: 1, finalized_by: 1, finalized_at: '2026-08-28 12:05:00'
    });

    // Cancelled and Draft Sessions
    this.addClassSession({
      section_subject_id: ss3.id,
      section_id: s1.id,
      timetable_entry_id: 5,
      session_date: '2026-09-04',
      start_time: '11:00',
      end_time: '12:00',
      status: 'CANCELLED',
      created_by: 1,
      cancellation_reason: 'Faculty on academic conference'
    });

    // 9. Attendance records for DBMS (CS401)
    // CS1
    this.addAttendanceRecord({ class_session_id: cs1.id, student_section_enrollment_id: e1.id, student_id: 3, section_id: s1.id, attendance_status: 'PRESENT', marked_by: 1 });
    this.addAttendanceRecord({ class_session_id: cs1.id, student_section_enrollment_id: e2.id, student_id: 4, section_id: s1.id, attendance_status: 'PRESENT', marked_by: 1 });
    this.addAttendanceRecord({ class_session_id: cs1.id, student_section_enrollment_id: e3.id, student_id: 5, section_id: s1.id, attendance_status: 'ABSENT', marked_by: 1 });
    // CS2
    this.addAttendanceRecord({ class_session_id: cs2.id, student_section_enrollment_id: e1.id, student_id: 3, section_id: s1.id, attendance_status: 'PRESENT', marked_by: 1 });
    this.addAttendanceRecord({ class_session_id: cs2.id, student_section_enrollment_id: e2.id, student_id: 4, section_id: s1.id, attendance_status: 'ABSENT', marked_by: 1 });
    this.addAttendanceRecord({ class_session_id: cs2.id, student_section_enrollment_id: e3.id, student_id: 5, section_id: s1.id, attendance_status: 'PRESENT', marked_by: 1 });
    // CS3
    this.addAttendanceRecord({ class_session_id: cs3.id, student_section_enrollment_id: e1.id, student_id: 3, section_id: s1.id, attendance_status: 'PRESENT', marked_by: 1 });
    this.addAttendanceRecord({ class_session_id: cs3.id, student_section_enrollment_id: e2.id, student_id: 4, section_id: s1.id, attendance_status: 'PRESENT', marked_by: 1 });
    this.addAttendanceRecord({ class_session_id: cs3.id, student_section_enrollment_id: e3.id, student_id: 5, section_id: s1.id, attendance_status: 'PRESENT', marked_by: 1 });
    // CS4
    this.addAttendanceRecord({ class_session_id: cs4.id, student_section_enrollment_id: e1.id, student_id: 3, section_id: s1.id, attendance_status: 'ABSENT', marked_by: 1 });
    this.addAttendanceRecord({ class_session_id: cs4.id, student_section_enrollment_id: e2.id, student_id: 4, section_id: s1.id, attendance_status: 'PRESENT', marked_by: 1 });
    this.addAttendanceRecord({ class_session_id: cs4.id, student_section_enrollment_id: e3.id, student_id: 5, section_id: s1.id, attendance_status: 'PRESENT', marked_by: 1 });
    // CS5
    this.addAttendanceRecord({ class_session_id: cs5.id, student_section_enrollment_id: e1.id, student_id: 3, section_id: s1.id, attendance_status: 'PRESENT', marked_by: 1 });
    this.addAttendanceRecord({ class_session_id: cs5.id, student_section_enrollment_id: e2.id, student_id: 4, section_id: s1.id, attendance_status: 'PRESENT', marked_by: 1 });
    this.addAttendanceRecord({ class_session_id: cs5.id, student_section_enrollment_id: e3.id, student_id: 5, section_id: s1.id, attendance_status: 'ABSENT', marked_by: 1 });
    // CS6
    this.addAttendanceRecord({ class_session_id: cs6.id, student_section_enrollment_id: e1.id, student_id: 3, section_id: s1.id, attendance_status: 'PRESENT', marked_by: 1 });
    this.addAttendanceRecord({ class_session_id: cs6.id, student_section_enrollment_id: e2.id, student_id: 4, section_id: s1.id, attendance_status: 'ABSENT', marked_by: 1 });
    this.addAttendanceRecord({ class_session_id: cs6.id, student_section_enrollment_id: e3.id, student_id: 5, section_id: s1.id, attendance_status: 'PRESENT', marked_by: 1 });

    // Attendance records for OOPS (CS402)
    // CS7
    this.addAttendanceRecord({ class_session_id: cs7.id, student_section_enrollment_id: e1.id, student_id: 3, section_id: s1.id, attendance_status: 'PRESENT', marked_by: 1 });
    this.addAttendanceRecord({ class_session_id: cs7.id, student_section_enrollment_id: e2.id, student_id: 4, section_id: s1.id, attendance_status: 'PRESENT', marked_by: 1 });
    this.addAttendanceRecord({ class_session_id: cs7.id, student_section_enrollment_id: e3.id, student_id: 5, section_id: s1.id, attendance_status: 'PRESENT', marked_by: 1 });
    // CS8
    this.addAttendanceRecord({ class_session_id: cs8.id, student_section_enrollment_id: e1.id, student_id: 3, section_id: s1.id, attendance_status: 'ABSENT', marked_by: 1 });
    this.addAttendanceRecord({ class_session_id: cs8.id, student_section_enrollment_id: e2.id, student_id: 4, section_id: s1.id, attendance_status: 'PRESENT', marked_by: 1 });
    this.addAttendanceRecord({ class_session_id: cs8.id, student_section_enrollment_id: e3.id, student_id: 5, section_id: s1.id, attendance_status: 'ABSENT', marked_by: 1 });
    // CS9
    this.addAttendanceRecord({ class_session_id: cs9.id, student_section_enrollment_id: e1.id, student_id: 3, section_id: s1.id, attendance_status: 'PRESENT', marked_by: 1 });
    this.addAttendanceRecord({ class_session_id: cs9.id, student_section_enrollment_id: e2.id, student_id: 4, section_id: s1.id, attendance_status: 'ABSENT', marked_by: 1 });
    this.addAttendanceRecord({ class_session_id: cs9.id, student_section_enrollment_id: e3.id, student_id: 5, section_id: s1.id, attendance_status: 'PRESENT', marked_by: 1 });
    // CS10
    this.addAttendanceRecord({ class_session_id: cs10.id, student_section_enrollment_id: e1.id, student_id: 3, section_id: s1.id, attendance_status: 'ABSENT', marked_by: 1 });
    this.addAttendanceRecord({ class_session_id: cs10.id, student_section_enrollment_id: e2.id, student_id: 4, section_id: s1.id, attendance_status: 'PRESENT', marked_by: 1 });
    this.addAttendanceRecord({ class_session_id: cs10.id, student_section_enrollment_id: e3.id, student_id: 5, section_id: s1.id, attendance_status: 'PRESENT', marked_by: 1 });
    // CS11
    this.addAttendanceRecord({ class_session_id: cs11.id, student_section_enrollment_id: e1.id, student_id: 3, section_id: s1.id, attendance_status: 'PRESENT', marked_by: 1 });
    this.addAttendanceRecord({ class_session_id: cs11.id, student_section_enrollment_id: e2.id, student_id: 4, section_id: s1.id, attendance_status: 'PRESENT', marked_by: 1 });
    this.addAttendanceRecord({ class_session_id: cs11.id, student_section_enrollment_id: e3.id, student_id: 5, section_id: s1.id, attendance_status: 'ABSENT', marked_by: 1 });

    // Attendance records for OS (CS403)
    // CS12
    this.addAttendanceRecord({ class_session_id: cs12.id, student_section_enrollment_id: e1.id, student_id: 3, section_id: s1.id, attendance_status: 'PRESENT', marked_by: 1 });
    this.addAttendanceRecord({ class_session_id: cs12.id, student_section_enrollment_id: e2.id, student_id: 4, section_id: s1.id, attendance_status: 'PRESENT', marked_by: 1 });
    this.addAttendanceRecord({ class_session_id: cs12.id, student_section_enrollment_id: e3.id, student_id: 5, section_id: s1.id, attendance_status: 'PRESENT', marked_by: 1 });
    // CS13
    this.addAttendanceRecord({ class_session_id: cs13.id, student_section_enrollment_id: e1.id, student_id: 3, section_id: s1.id, attendance_status: 'PRESENT', marked_by: 1 });
    this.addAttendanceRecord({ class_session_id: cs13.id, student_section_enrollment_id: e2.id, student_id: 4, section_id: s1.id, attendance_status: 'PRESENT', marked_by: 1 });
    this.addAttendanceRecord({ class_session_id: cs13.id, student_section_enrollment_id: e3.id, student_id: 5, section_id: s1.id, attendance_status: 'ABSENT', marked_by: 1 });
    // CS14
    this.addAttendanceRecord({ class_session_id: cs14.id, student_section_enrollment_id: e1.id, student_id: 3, section_id: s1.id, attendance_status: 'PRESENT', marked_by: 1 });
    this.addAttendanceRecord({ class_session_id: cs14.id, student_section_enrollment_id: e2.id, student_id: 4, section_id: s1.id, attendance_status: 'ABSENT', marked_by: 1 });
    this.addAttendanceRecord({ class_session_id: cs14.id, student_section_enrollment_id: e3.id, student_id: 5, section_id: s1.id, attendance_status: 'PRESENT', marked_by: 1 });
    // CS15
    this.addAttendanceRecord({ class_session_id: cs15.id, student_section_enrollment_id: e1.id, student_id: 3, section_id: s1.id, attendance_status: 'ABSENT', marked_by: 1 });
    this.addAttendanceRecord({ class_session_id: cs15.id, student_section_enrollment_id: e2.id, student_id: 4, section_id: s1.id, attendance_status: 'PRESENT', marked_by: 1 });
    this.addAttendanceRecord({ class_session_id: cs15.id, student_section_enrollment_id: e3.id, student_id: 5, section_id: s1.id, attendance_status: 'PRESENT', marked_by: 1 });

    // Sample audit record
    this.addAuditLog({
      attendance_record_id: 3,
      previous_status: 'PRESENT',
      new_status: 'ABSENT',
      changed_by: 1,
      reason: 'Student left lecture early'
    });

    // 10. Automatically assign/generate upcoming scheduled classes according to weekly timetable
    this.generateTimetableSessions(s1.id, 60);
    this.generateTimetableSessions(s2.id, 60);
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

  // ---- Date-Wise Attendance Operations ----
  getDateSessionsSummary(sectionId, dateStr) {
    const secId = Number(sectionId);
    const dateObj = new Date(`${dateStr}T00:00:00Z`);
    const days = ['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'];
    const dayOfWeek = days[dateObj.getUTCDay()];

    const holiday = this.university_holidays.find(h => h.holiday_date === dateStr);

    // 1. Timetable entries for this day of week in this section
    const sectionSubjects = this.section_subjects.filter(ss => ss.section_id === secId);
    const timetableSlots = [];

    for (const ss of sectionSubjects) {
      const subj = this.subjects.find(s => s.id === ss.subject_id);
      const entries = this.weekly_timetable.filter(wt =>
        wt.section_subject_id === ss.id &&
        wt.day_of_week === dayOfWeek &&
        (!wt.valid_from || dateStr >= wt.valid_from) &&
        (!wt.valid_to || dateStr <= wt.valid_to)
      );

      for (const e of entries) {
        timetableSlots.push({
          timetable_entry_id: e.id,
          section_subject_id: ss.id,
          subject_name: subj ? subj.subject_name : 'Unknown',
          subject_code: subj ? subj.subject_code : '',
          start_time: e.start_time,
          end_time: e.end_time
        });
      }
    }

    // 2. Existing class sessions on this date for this section
    const existingSessions = this.class_sessions.filter(cs =>
      cs.section_id === secId && cs.session_date === dateStr
    );

    // 3. Merge timetable slots with existing sessions
    const periods = [];
    const matchedSessionIds = new Set();

    for (const slot of timetableSlots) {
      const existing = existingSessions.find(cs =>
        cs.section_subject_id === slot.section_subject_id &&
        cs.start_time === slot.start_time
      );

      if (existing) {
        matchedSessionIds.add(existing.id);
        const records = this.attendance_records.filter(r => r.class_session_id === existing.id);
        const presentCount = records.filter(r => r.attendance_status === 'PRESENT').length;
        const totalMarked = records.length;
        const faculty = this.users.find(u => u.id === (existing.finalized_by || existing.created_by));

        periods.push({
          type: 'TIMETABLE_SLOT',
          session_id: existing.id,
          timetable_entry_id: slot.timetable_entry_id,
          section_subject_id: slot.section_subject_id,
          subject_name: slot.subject_name,
          subject_code: slot.subject_code,
          start_time: slot.start_time,
          end_time: slot.end_time,
          status: existing.status,
          total_marked: totalMarked,
          present_count: presentCount,
          faculty_name: faculty ? faculty.full_name : 'Faculty Administrator',
          finalized_at: existing.finalized_at,
          can_start: false
        });
      } else {
        periods.push({
          type: 'TIMETABLE_SLOT',
          session_id: null,
          timetable_entry_id: slot.timetable_entry_id,
          section_subject_id: slot.section_subject_id,
          subject_name: slot.subject_name,
          subject_code: slot.subject_code,
          start_time: slot.start_time,
          end_time: slot.end_time,
          status: 'UNSCHEDULED',
          total_marked: 0,
          present_count: 0,
          faculty_name: null,
          finalized_at: null,
          can_start: !holiday
        });
      }
    }

    // 4. Any ad-hoc sessions on this date not in timetable
    for (const cs of existingSessions) {
      if (!matchedSessionIds.has(cs.id)) {
        const ss = this.section_subjects.find(s => s.id === cs.section_subject_id);
        const subj = ss ? this.subjects.find(s => s.id === ss.subject_id) : null;
        const records = this.attendance_records.filter(r => r.class_session_id === cs.id);
        const presentCount = records.filter(r => r.attendance_status === 'PRESENT').length;

        periods.push({
          type: 'ADHOC_SESSION',
          session_id: cs.id,
          timetable_entry_id: cs.timetable_entry_id,
          section_subject_id: cs.section_subject_id,
          subject_name: subj ? subj.subject_name : 'Unknown',
          subject_code: subj ? subj.subject_code : '',
          start_time: cs.start_time,
          end_time: cs.end_time,
          status: cs.status,
          total_marked: records.length,
          present_count: presentCount,
          faculty_name: 'Faculty Administrator',
          finalized_at: cs.finalized_at,
          can_start: false
        });
      }
    }

    periods.sort((a, b) => a.start_time.localeCompare(b.start_time));

    return {
      date: dateStr,
      day_of_week: dayOfWeek,
      is_holiday: !!holiday,
      holiday_description: holiday ? holiday.description : null,
      periods
    };
  }

  getOrCreateSessionForDate({ section_id, section_subject_id, session_date, start_time, end_time, timetable_entry_id, created_by }) {
    const existing = this.class_sessions.find(cs =>
      cs.section_subject_id === Number(section_subject_id) &&
      cs.session_date === session_date &&
      cs.start_time === start_time
    );

    if (existing) return existing;

    return this.createSession({
      section_id,
      section_subject_id,
      session_date,
      start_time,
      end_time,
      timetable_entry_id,
      created_by
    });
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

  getSessionFinalizeWindow(session) {
    const dateStr = session.session_date instanceof Date
      ? session.session_date.toISOString().split('T')[0]
      : String(session.session_date).split('T')[0];

    const startTime = String(session.start_time || '00:00').trim();
    const endTime = String(session.end_time || '23:59').trim();
    const normStart = startTime.length === 5 ? `${startTime}:00` : startTime;
    const normEnd = endTime.length === 5 ? `${endTime}:00` : endTime;

    const startDt = new Date(`${dateStr}T${normStart}`);
    let endDt = new Date(`${dateStr}T${normEnd}`);
    if (endDt < startDt) {
      endDt = new Date(endDt.getTime() + 24 * 60 * 60 * 1000);
    }
    const maxFinalizeDt = new Date(endDt.getTime() + 3 * 24 * 60 * 60 * 1000);
    const now = new Date();

    const isFuture = now < startDt;
    const isLive = now >= startDt && now <= endDt;
    const isWithinGrace = now > endDt && now <= maxFinalizeDt;
    const isExpired = now > maxFinalizeDt;
    const canFinalize = (isLive || isWithinGrace) && session.status !== 'FINALIZED' && session.status !== 'CANCELLED';

    return {
      startDt,
      endDt,
      maxFinalizeDt,
      isFuture,
      isLive,
      isWithinGrace,
      isExpired,
      canFinalize
    };
  }

  finalizeAttendance(sessionId, adminId, skipTimeCheck = false) {
    const session = this.class_sessions.find(cs => cs.id === Number(sessionId));
    if (!session) throw new Error('Session not found');
    if (session.status === 'FINALIZED') throw new Error('Session is already FINALIZED');
    if (session.status === 'CANCELLED') throw new Error('Cannot finalize a CANCELLED session');

    // Invariant: Future sessions cannot be finalised in advance.
    // Must be during the session period or up to 3 days after original session endtime only.
    if (!skipTimeCheck) {
      const window = this.getSessionFinalizeWindow(session);
      if (window.isFuture) {
        throw new Error(
          `Cannot finalize attendance for a future session. Finalization is only allowed manually during the session (${session.start_time} – ${session.end_time}) or up to 3 days after original session end time.`
        );
      }
      if (window.isExpired) {
        throw new Error(
          `Finalization window expired. Attendance can only be finalized during the session or up to 3 days from the original session end time.`
        );
      }
    }

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

  // ---- Subject Session History (for student drilldown) ----
  getSubjectSessions(studentId, sectionSubjectId) {
    const sid = Number(studentId);
    const ssid = Number(sectionSubjectId);

    const enrollments = this.student_section_enrollments.filter(e => e.student_id === sid);
    if (enrollments.length === 0) return [];

    const finalizedSessions = this.class_sessions.filter(cs =>
      cs.section_subject_id === ssid && cs.status === 'FINALIZED'
    );

    const daysOfWeek = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
    const results = [];

    for (const session of finalizedSessions) {
      const validEnrollment = enrollments.find(e => {
        if (e.section_id !== session.section_id) return false;
        const sDate = new Date(`${session.session_date}T00:00:00Z`);
        const start = new Date(`${e.start_date}T00:00:00Z`);
        if (sDate < start) return false;
        if (e.end_date && sDate > new Date(`${e.end_date}T00:00:00Z`)) return false;
        return true;
      });

      if (validEnrollment) {
        const record = this.attendance_records.find(r =>
          r.class_session_id === session.id && r.student_id === sid
        );
        const faculty = this.users.find(u => u.id === session.finalized_by);
        const sDate = new Date(`${session.session_date}T00:00:00Z`);
        const dayName = daysOfWeek[sDate.getUTCDay()];

        const audit = record ? this.attendance_audit_log.find(a => a.attendance_record_id === record.id) : null;

        results.push({
          session_id: session.id,
          session_date: session.session_date,
          day_of_week: dayName,
          start_time: session.start_time,
          end_time: session.end_time,
          attendance_status: record ? record.attendance_status : 'ABSENT',
          record_id: record ? record.id : null,
          marked_at: record ? record.marked_at : session.finalized_at,
          finalized_by: faculty ? faculty.full_name : 'Faculty Administrator',
          finalized_at: session.finalized_at,
          audit_reason: audit ? audit.reason : null
        });
      }
    }

    results.sort((a, b) => (b.session_date + b.start_time).localeCompare(a.session_date + a.start_time));
    return results;
  }

  // ---- Upcoming Sessions Detection ----
  getUpcomingSessions(studentId, sectionSubjectId, untilDate = null, daysAhead = 30) {
    const sid = Number(studentId);
    const activeEnrollment = this.student_section_enrollments.find(e =>
      e.student_id === sid && e.status === 'ACTIVE'
    );
    if (!activeEnrollment) throw new Error('Student has no active enrollment');

    const todayStr = new Date().toISOString().split('T')[0];
    let targetDateStr;

    if (untilDate) {
      targetDateStr = untilDate;
      const d1 = new Date(todayStr);
      const d2 = new Date(untilDate);
      const diffDays = Math.ceil((d2 - d1) / (1000 * 60 * 60 * 24)) + 1;
      if (diffDays > 0) {
        this.generateTimetableSessions(activeEnrollment.section_id, Math.max(diffDays, 30));
      }
    } else {
      const d = new Date();
      d.setDate(d.getDate() + Number(daysAhead));
      targetDateStr = d.toISOString().split('T')[0];
      this.generateTimetableSessions(activeEnrollment.section_id, Number(daysAhead));
    }

    const daysOfWeek = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];

    let upcoming = this.class_sessions.filter(cs =>
      cs.section_id === activeEnrollment.section_id &&
      cs.status === 'SCHEDULED' &&
      cs.session_date >= todayStr &&
      cs.session_date <= targetDateStr &&
      !this.isHoliday(cs.session_date)
    );

    if (sectionSubjectId && sectionSubjectId !== 'all') {
      upcoming = upcoming.filter(cs => cs.section_subject_id === Number(sectionSubjectId));
    }

    const enriched = upcoming.map(cs => {
      const ss = this.section_subjects.find(s => s.id === cs.section_subject_id);
      const subj = ss ? this.subjects.find(s => s.id === ss.subject_id) : null;
      const sDate = new Date(`${cs.session_date}T00:00:00Z`);
      const dayName = daysOfWeek[sDate.getUTCDay()];

      return {
        session_id: cs.id,
        section_subject_id: cs.section_subject_id,
        subject_name: subj ? subj.subject_name : 'Unknown',
        subject_code: subj ? subj.subject_code : '',
        session_date: cs.session_date,
        day_of_week: dayName,
        start_time: cs.start_time,
        end_time: cs.end_time,
        status: cs.status
      };
    });

    enriched.sort((a, b) => (a.session_date + a.start_time).localeCompare(b.session_date + b.start_time));
    return enriched;
  }

  // ---- Interactive Session-by-Session Predictor with Leave Support ----
  predictAttendanceInteractive(studentId, sectionSubjectId, sessionDecisions = {}) {
    const sid = Number(studentId);
    const ssid = sectionSubjectId && sectionSubjectId !== 'all' ? Number(sectionSubjectId) : null;

    let P_curr = 0;
    let T_curr = 0;
    let currentPercentage = 0;

    if (!ssid) {
      const activeEnrollment = this.student_section_enrollments.find(e =>
        e.student_id === sid && e.status === 'ACTIVE'
      );
      if (activeEnrollment) {
        const overall = this.calculateOverallAttendance(sid, activeEnrollment.section_id);
        P_curr = overall.totalAttended;
        T_curr = overall.totalConducted;
        currentPercentage = overall.overallPercentage;
      }
    } else {
      const stats = this.calculateSubjectAttendance(sid, ssid);
      P_curr = stats.attended;
      T_curr = stats.conducted;
      currentPercentage = stats.percentage;
    }

    let plannedPresent = 0;
    let plannedAbsent = 0;
    let plannedLeave = 0;

    for (const [sessionId, decision] of Object.entries(sessionDecisions)) {
      const upper = String(decision).toUpperCase();
      if (upper === 'PRESENT') plannedPresent++;
      else if (upper === 'ABSENT') plannedAbsent++;
      else if (upper === 'LEAVE') plannedLeave++;
    }

    // LEAVE excused: does not increment conducted denominator
    const projectedConducted = T_curr + plannedPresent + plannedAbsent;
    const projectedAttended = P_curr + plannedPresent;

    const projectedPercentage = projectedConducted > 0
      ? Number(((projectedAttended / projectedConducted) * 100).toFixed(2))
      : 0;

    const isEligible = projectedPercentage >= 75.0;

    // Margin missable: how many more classes can be missed without dropping below 75%
    let marginMissable = 0;
    if (isEligible) {
      marginMissable = Math.max(0, Math.floor((projectedAttended - 0.75 * projectedConducted) / 0.75));
    }

    // Classes needed: how many consecutive classes to attend to reach 75%
    let classesNeeded = 0;
    if (!isEligible) {
      classesNeeded = Math.max(0, Math.ceil(3 * projectedConducted - 4 * projectedAttended));
    }

    return {
      current_conducted: T_curr,
      current_attended: P_curr,
      current_percentage: currentPercentage,
      planned_present: plannedPresent,
      planned_absent: plannedAbsent,
      planned_leave: plannedLeave,
      total_planned_sessions: plannedPresent + plannedAbsent + plannedLeave,
      projected_conducted: projectedConducted,
      projected_attended: projectedAttended,
      projected_percentage: projectedPercentage,
      is_eligible: isEligible,
      margin_missable: marginMissable,
      classes_needed: classesNeeded
    };
  }
}

const db = new AttendanceDB();
module.exports = db;
