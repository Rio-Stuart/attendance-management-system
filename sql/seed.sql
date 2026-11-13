-- ============================================================
-- Seed Data for Attendance Management System
-- ============================================================

-- ---- Users (2 admins + 5 students) ----
INSERT INTO users (username, password_hash, full_name, email, role) VALUES
('admin1', 'admin123', 'Dr. Rajesh Kumar',   'rajesh.kumar@university.edu',   'ADMIN'),
('admin2', 'admin123', 'Prof. Anita Sharma',  'anita.sharma@university.edu',   'ADMIN'),
('student1', 'student123', 'Aarav Patel',    'aarav.patel@student.edu',    'STUDENT'),
('student2', 'student123', 'Priya Singh',    'priya.singh@student.edu',    'STUDENT'),
('student3', 'student123', 'Rohan Gupta',    'rohan.gupta@student.edu',    'STUDENT'),
('student4', 'student123', 'Sneha Reddy',    'sneha.reddy@student.edu',    'STUDENT'),
('student5', 'student123', 'Vikram Joshi',   'vikram.joshi@student.edu',   'STUDENT');

-- ---- Sections ----
INSERT INTO sections (section_name, academic_year) VALUES
('25BCS801', '2025-2026'),
('25BCS802', '2025-2026');

-- ---- Subjects ----
INSERT INTO subjects (subject_code, subject_name) VALUES
('CS401', 'Database Management Systems'),
('CS402', 'Object Oriented Programming'),
('CS403', 'Operating Systems');

-- ---- Section ↔ Subject mappings ----
-- Section 25BCS801 (id=1) gets all 3 subjects
INSERT INTO section_subjects (section_id, subject_id) VALUES
(1, 1),  -- 25BCS801 - DBMS      (ss_id=1)
(1, 2),  -- 25BCS801 - OOPS      (ss_id=2)
(1, 3),  -- 25BCS801 - OS        (ss_id=3)
-- Section 25BCS802 (id=2) gets all 3 subjects
(2, 1),  -- 25BCS802 - DBMS      (ss_id=4)
(2, 2),  -- 25BCS802 - OOPS      (ss_id=5)
(2, 3);  -- 25BCS802 - OS        (ss_id=6)

-- ---- Student enrollments ----
-- Students 1-3 in 25BCS801, Students 4-5 in 25BCS802
INSERT INTO student_section_enrollments (student_id, section_id, academic_year, start_date, status) VALUES
(3, 1, '2025-2026', '2025-08-01', 'ACTIVE'),   -- Aarav  → 25BCS801  (sse_id=1)
(4, 1, '2025-2026', '2025-08-01', 'ACTIVE'),   -- Priya  → 25BCS801  (sse_id=2)
(5, 1, '2025-2026', '2025-08-01', 'ACTIVE'),   -- Rohan  → 25BCS801  (sse_id=3)
(6, 2, '2025-2026', '2025-08-01', 'ACTIVE'),   -- Sneha  → 25BCS802  (sse_id=4)
(7, 2, '2025-2026', '2025-08-01', 'ACTIVE');   -- Vikram → 25BCS802  (sse_id=5)

-- ---- Weekly timetable entries ----
INSERT INTO weekly_timetable (section_subject_id, day_of_week, start_time, end_time, valid_from, valid_to) VALUES
(1, 'MONDAY',    '09:00', '10:00', '2025-08-01', '2026-05-31'),  -- 25BCS801 DBMS Mon
(1, 'WEDNESDAY', '09:00', '10:00', '2025-08-01', '2026-05-31'),  -- 25BCS801 DBMS Wed
(2, 'TUESDAY',   '10:00', '11:00', '2025-08-01', '2026-05-31'),  -- 25BCS801 OOPS Tue
(2, 'THURSDAY',  '10:00', '11:00', '2025-08-01', '2026-05-31'),  -- 25BCS801 OOPS Thu
(3, 'FRIDAY',    '11:00', '12:00', '2025-08-01', '2026-05-31'),  -- 25BCS801 OS Fri
(4, 'MONDAY',    '11:00', '12:00', '2025-08-01', '2026-05-31'),  -- 25BCS802 DBMS Mon
(5, 'WEDNESDAY', '14:00', '15:00', '2025-08-01', '2026-05-31'),  -- 25BCS802 OOPS Wed
(6, 'FRIDAY',    '14:00', '15:00', '2025-08-01', '2026-05-31');  -- 25BCS802 OS Fri

-- ---- University holidays ----
INSERT INTO university_holidays (holiday_date, description) VALUES
('2025-08-15', 'Independence Day'),
('2025-10-02', 'Gandhi Jayanti'),
('2025-10-24', 'Dussehra'),
('2025-11-01', 'Diwali'),
('2025-12-25', 'Christmas'),
('2026-01-26', 'Republic Day');

-- ---- Class sessions (various statuses) ----
-- FINALIZED sessions (25BCS801 DBMS — Mon 4 Aug, Wed 6 Aug)
INSERT INTO class_sessions (section_subject_id, section_id, timetable_entry_id, session_date, start_time, end_time, status, created_by, finalized_by, finalized_at) VALUES
(1, 1, 1, '2025-08-04', '09:00', '10:00', 'FINALIZED', 1, 1, '2025-08-04 10:05:00'),  -- cs_id=1
(1, 1, 2, '2025-08-06', '09:00', '10:00', 'FINALIZED', 1, 1, '2025-08-06 10:05:00');  -- cs_id=2

-- SCHEDULED session (25BCS801 OOPS — Tue 5 Aug)
INSERT INTO class_sessions (section_subject_id, section_id, timetable_entry_id, session_date, start_time, end_time, status, created_by) VALUES
(2, 1, 3, '2025-08-05', '10:00', '11:00', 'SCHEDULED', 1);  -- cs_id=3

-- DRAFT session (25BCS801 OOPS — Thu 7 Aug)
INSERT INTO class_sessions (section_subject_id, section_id, timetable_entry_id, session_date, start_time, end_time, status, created_by) VALUES
(2, 1, 4, '2025-08-07', '10:00', '11:00', 'DRAFT', 1);  -- cs_id=4

-- CANCELLED session (25BCS801 OS — Fri 8 Aug)
INSERT INTO class_sessions (section_subject_id, section_id, timetable_entry_id, session_date, start_time, end_time, status, created_by, cancellation_reason) VALUES
(3, 1, 5, '2025-08-08', '11:00', '12:00', 'CANCELLED', 1, 'Professor on leave');  -- cs_id=5

-- FINALIZED session for 25BCS802 DBMS — Mon 4 Aug
INSERT INTO class_sessions (section_subject_id, section_id, timetable_entry_id, session_date, start_time, end_time, status, created_by, finalized_by, finalized_at) VALUES
(4, 2, 6, '2025-08-04', '11:00', '12:00', 'FINALIZED', 2, 2, '2025-08-04 12:05:00');  -- cs_id=6

-- ---- Attendance records (for FINALIZED sessions only) ----
-- Session 1 (25BCS801 DBMS Mon 4 Aug) — 3 students
INSERT INTO attendance_records (class_session_id, student_section_enrollment_id, student_id, section_id, attendance_status, marked_by) VALUES
(1, 1, 3, 1, 'PRESENT', 1),  -- Aarav present
(1, 2, 4, 1, 'PRESENT', 1),  -- Priya present
(1, 3, 5, 1, 'ABSENT',  1);  -- Rohan absent

-- Session 2 (25BCS801 DBMS Wed 6 Aug) — 3 students
INSERT INTO attendance_records (class_session_id, student_section_enrollment_id, student_id, section_id, attendance_status, marked_by) VALUES
(2, 1, 3, 1, 'PRESENT', 1),  -- Aarav present
(2, 2, 4, 1, 'ABSENT',  1),  -- Priya absent
(2, 3, 5, 1, 'PRESENT', 1);  -- Rohan present

-- Session 6 (25BCS802 DBMS Mon 4 Aug) — 2 students
INSERT INTO attendance_records (class_session_id, student_section_enrollment_id, student_id, section_id, attendance_status, marked_by) VALUES
(6, 4, 6, 2, 'PRESENT', 2),  -- Sneha present
(6, 5, 7, 2, 'PRESENT', 2);  -- Vikram present

-- ---- Audit log sample (Rohan's status corrected in session 1) ----
INSERT INTO attendance_audit_log (attendance_record_id, previous_status, new_status, changed_by, reason) VALUES
(3, 'PRESENT', 'ABSENT', 1, 'Correction: student left early before session ended');
