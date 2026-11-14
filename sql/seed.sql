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
('25BCS801', '2026-2027'),
('25BCS802', '2026-2027');

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
(3, 1, '2026-2027', '2026-08-01', 'ACTIVE'),   -- Aarav  → 25BCS801  (sse_id=1)
(4, 1, '2026-2027', '2026-08-01', 'ACTIVE'),   -- Priya  → 25BCS801  (sse_id=2)
(5, 1, '2026-2027', '2026-08-01', 'ACTIVE'),   -- Rohan  → 25BCS801  (sse_id=3)
(6, 2, '2026-2027', '2026-08-01', 'ACTIVE'),   -- Sneha  → 25BCS802  (sse_id=4)
(7, 2, '2026-2027', '2026-08-01', 'ACTIVE');   -- Vikram → 25BCS802  (sse_id=5)

-- ---- Weekly timetable entries ----
INSERT INTO weekly_timetable (section_subject_id, day_of_week, start_time, end_time, valid_from, valid_to) VALUES
(1, 'MONDAY',    '09:00', '10:00', '2026-08-01', '2027-05-31'),  -- 25BCS801 DBMS Mon
(1, 'WEDNESDAY', '09:00', '10:00', '2026-08-01', '2027-05-31'),  -- 25BCS801 DBMS Wed
(2, 'TUESDAY',   '10:00', '11:00', '2026-08-01', '2027-05-31'),  -- 25BCS801 OOPS Tue
(2, 'THURSDAY',  '10:00', '11:00', '2026-08-01', '2027-05-31'),  -- 25BCS801 OOPS Thu
(3, 'FRIDAY',    '11:00', '12:00', '2026-08-01', '2027-05-31'),  -- 25BCS801 OS Fri
(4, 'MONDAY',    '11:00', '12:00', '2026-08-01', '2027-05-31'),  -- 25BCS802 DBMS Mon
(5, 'WEDNESDAY', '14:00', '15:00', '2026-08-01', '2027-05-31'),  -- 25BCS802 OOPS Wed
(6, 'FRIDAY',    '14:00', '15:00', '2026-08-01', '2027-05-31');  -- 25BCS802 OS Fri

-- ---- University holidays ----
INSERT INTO university_holidays (holiday_date, description) VALUES
('2026-08-15', 'Independence Day'),
('2026-10-02', 'Gandhi Jayanti'),
('2026-10-24', 'Dussehra'),
('2026-11-01', 'Diwali'),
('2026-12-25', 'Christmas'),
('2027-01-26', 'Republic Day');

-- ---- Class sessions (various statuses) ----
-- DBMS (ss1) Finalized Sessions
INSERT INTO class_sessions (id, section_subject_id, section_id, timetable_entry_id, session_date, start_time, end_time, status, created_by, finalized_by, finalized_at) VALUES
(1, 1, 1, 1, '2026-08-03', '09:00', '10:00', 'FINALIZED', 1, 1, '2026-08-03 10:05:00'),
(2, 1, 1, 2, '2026-08-05', '09:00', '10:00', 'FINALIZED', 1, 1, '2026-08-05 10:05:00'),
(3, 1, 1, 1, '2026-08-10', '09:00', '10:00', 'FINALIZED', 1, 1, '2026-08-10 10:05:00'),
(4, 1, 1, 2, '2026-08-12', '09:00', '10:00', 'FINALIZED', 1, 1, '2026-08-12 10:05:00'),
(5, 1, 1, 1, '2026-08-17', '09:00', '10:00', 'FINALIZED', 1, 1, '2026-08-17 10:05:00'),
(6, 1, 1, 2, '2026-08-19', '09:00', '10:00', 'FINALIZED', 1, 1, '2026-08-19 10:05:00'),
-- OOPS (ss2) Finalized Sessions
(7, 2, 1, 3, '2026-08-04', '10:00', '11:00', 'FINALIZED', 1, 1, '2026-08-04 11:05:00'),
(8, 2, 1, 4, '2026-08-06', '10:00', '11:00', 'FINALIZED', 1, 1, '2026-08-06 11:05:00'),
(9, 2, 1, 3, '2026-08-11', '10:00', '11:00', 'FINALIZED', 1, 1, '2026-08-11 11:05:00'),
(10, 2, 1, 4, '2026-08-13', '10:00', '11:00', 'FINALIZED', 1, 1, '2026-08-13 11:05:00'),
(11, 2, 1, 3, '2026-08-18', '10:00', '11:00', 'FINALIZED', 1, 1, '2026-08-18 11:05:00'),
-- OS (ss3) Finalized Sessions
(12, 3, 1, 5, '2026-08-07', '11:00', '12:00', 'FINALIZED', 1, 1, '2026-08-07 12:05:00'),
(13, 3, 1, 5, '2026-08-14', '11:00', '12:00', 'FINALIZED', 1, 1, '2026-08-14 12:05:00'),
(14, 3, 1, 5, '2026-08-21', '11:00', '12:00', 'FINALIZED', 1, 1, '2026-08-21 12:05:00'),
(15, 3, 1, 5, '2026-08-28', '11:00', '12:00', 'FINALIZED', 1, 1, '2026-08-28 12:05:00');

-- ---- Attendance records (for FINALIZED sessions only) ----
INSERT INTO attendance_records (class_session_id, student_section_enrollment_id, student_id, section_id, attendance_status, marked_by) VALUES
-- DBMS (CS1 - CS6)
(1, 1, 3, 1, 'PRESENT', 1), (1, 2, 4, 1, 'PRESENT', 1), (1, 3, 5, 1, 'ABSENT', 1),
(2, 1, 3, 1, 'PRESENT', 1), (2, 2, 4, 1, 'ABSENT', 1), (2, 3, 5, 1, 'PRESENT', 1),
(3, 1, 3, 1, 'PRESENT', 1), (3, 2, 4, 1, 'PRESENT', 1), (3, 3, 5, 1, 'PRESENT', 1),
(4, 1, 3, 1, 'ABSENT', 1), (4, 2, 4, 1, 'PRESENT', 1), (4, 3, 5, 1, 'PRESENT', 1),
(5, 1, 3, 1, 'PRESENT', 1), (5, 2, 4, 1, 'PRESENT', 1), (5, 3, 5, 1, 'ABSENT', 1),
(6, 1, 3, 1, 'PRESENT', 1), (6, 2, 4, 1, 'ABSENT', 1), (6, 3, 5, 1, 'PRESENT', 1),
-- OOPS (CS7 - CS11)
(7, 1, 3, 1, 'PRESENT', 1), (7, 2, 4, 1, 'PRESENT', 1), (7, 3, 5, 1, 'PRESENT', 1),
(8, 1, 3, 1, 'ABSENT', 1), (8, 2, 4, 1, 'PRESENT', 1), (8, 3, 5, 1, 'ABSENT', 1),
(9, 1, 3, 1, 'PRESENT', 1), (9, 2, 4, 1, 'ABSENT', 1), (9, 3, 5, 1, 'PRESENT', 1),
(10, 1, 3, 1, 'ABSENT', 1), (10, 2, 4, 1, 'PRESENT', 1), (10, 3, 5, 1, 'PRESENT', 1),
(11, 1, 3, 1, 'PRESENT', 1), (11, 2, 4, 1, 'PRESENT', 1), (11, 3, 5, 1, 'ABSENT', 1),
-- OS (CS12 - CS15)
(12, 1, 3, 1, 'PRESENT', 1), (12, 2, 4, 1, 'PRESENT', 1), (12, 3, 5, 1, 'PRESENT', 1),
(13, 1, 3, 1, 'PRESENT', 1), (13, 2, 4, 1, 'PRESENT', 1), (13, 3, 5, 1, 'ABSENT', 1),
(14, 1, 3, 1, 'PRESENT', 1), (14, 2, 4, 1, 'ABSENT', 1), (14, 3, 5, 1, 'PRESENT', 1),
(15, 1, 3, 1, 'ABSENT', 1), (15, 2, 4, 1, 'PRESENT', 1), (15, 3, 5, 1, 'PRESENT', 1);

-- ---- Audit log sample (Rohan's status corrected in session 1) ----
INSERT INTO attendance_audit_log (attendance_record_id, previous_status, new_status, changed_by, reason) VALUES
(3, 'PRESENT', 'ABSENT', 1, 'Correction: student left early before session ended');
