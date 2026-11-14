-- netlify/database/migrations/20261114100001_seed_initial_data.sql
-- Initial Seed Data for Netlify Database (PostgreSQL)

-- Users (2 admins + 5 students)
INSERT INTO users (username, password_hash, full_name, email, role) VALUES
('admin1', 'admin123', 'Dr. Rajesh Kumar',   'rajesh.kumar@university.edu',   'ADMIN'),
('admin2', 'admin123', 'Prof. Anita Sharma',  'anita.sharma@university.edu',   'ADMIN'),
('student1', 'student123', 'Aarav Patel',    'aarav.patel@student.edu',    'STUDENT'),
('student2', 'student123', 'Priya Singh',    'priya.singh@student.edu',    'STUDENT'),
('student3', 'student123', 'Rohan Gupta',    'rohan.gupta@student.edu',    'STUDENT'),
('student4', 'student123', 'Sneha Reddy',    'sneha.reddy@student.edu',    'STUDENT'),
('student5', 'student123', 'Vikram Joshi',   'vikram.joshi@student.edu',   'STUDENT')
ON CONFLICT (username) DO NOTHING;

-- Sections
INSERT INTO sections (section_name, academic_year) VALUES
('25BCS801', '2026-2027'),
('25BCS802', '2026-2027')
ON CONFLICT (section_name) DO NOTHING;

-- Subjects
INSERT INTO subjects (subject_code, subject_name) VALUES
('CS401', 'Database Management Systems'),
('CS402', 'Object Oriented Programming'),
('CS403', 'Operating Systems')
ON CONFLICT (subject_code) DO NOTHING;

-- Section ↔ Subject mappings
-- Section 1 (25BCS801): DBMS, OOPS, OS
INSERT INTO section_subjects (section_id, subject_id) VALUES
(1, 1),
(1, 2),
(1, 3),
-- Section 2 (25BCS802): DBMS, OOPS, OS
(2, 1),
(2, 2),
(2, 3)
ON CONFLICT (section_id, subject_id) DO NOTHING;

-- Student Enrollments
-- Students 3, 4, 5 in Section 1 (25BCS801); Students 6, 7 in Section 2 (25BCS802)
INSERT INTO student_section_enrollments (student_id, section_id, academic_year, start_date, status) VALUES
(3, 1, '2026-2027', '2026-08-01', 'ACTIVE'),
(4, 1, '2026-2027', '2026-08-01', 'ACTIVE'),
(5, 1, '2026-2027', '2026-08-01', 'ACTIVE'),
(6, 2, '2026-2027', '2026-08-01', 'ACTIVE'),
(7, 2, '2026-2027', '2026-08-01', 'ACTIVE');

-- Weekly Timetable
INSERT INTO weekly_timetable (section_subject_id, day_of_week, start_time, end_time, valid_from, valid_to) VALUES
(1, 'MONDAY',    '09:00', '10:00', '2026-08-01', '2027-05-31'),
(1, 'WEDNESDAY', '09:00', '10:00', '2026-08-01', '2027-05-31'),
(2, 'TUESDAY',   '10:00', '11:00', '2026-08-01', '2027-05-31'),
(2, 'THURSDAY',  '10:00', '11:00', '2026-08-01', '2027-05-31'),
(3, 'FRIDAY',    '11:00', '12:00', '2026-08-01', '2027-05-31'),
(4, 'MONDAY',    '11:00', '12:00', '2026-08-01', '2027-05-31'),
(5, 'WEDNESDAY', '14:00', '15:00', '2026-08-01', '2027-05-31'),
(6, 'FRIDAY',    '14:00', '15:00', '2026-08-01', '2027-05-31');

-- University Holidays
INSERT INTO university_holidays (holiday_date, description) VALUES
('2026-08-15', 'Independence Day'),
('2026-10-02', 'Gandhi Jayanti'),
('2026-10-24', 'Dussehra'),
('2026-11-01', 'Diwali'),
('2026-12-25', 'Christmas'),
('2027-01-26', 'Republic Day')
ON CONFLICT (holiday_date) DO NOTHING;

-- Sample Class Sessions
-- 1. FINALIZED session (25BCS801 DBMS Mon 4 Aug)
INSERT INTO class_sessions (section_subject_id, section_id, timetable_entry_id, session_date, start_time, end_time, status, created_by, finalized_by, finalized_at) VALUES
(1, 1, 1, '2026-08-04', '09:00', '10:00', 'FINALIZED', 1, 1, '2026-08-04 10:05:00'),
-- 2. FINALIZED session (25BCS801 DBMS Wed 6 Aug)
(1, 1, 2, '2026-08-06', '09:00', '10:00', 'FINALIZED', 1, 1, '2026-08-06 10:05:00'),
-- 3. SCHEDULED session (25BCS801 OOPS Tue 17 Nov)
(2, 1, 3, '2026-11-17', '10:00', '11:00', 'SCHEDULED', 1, NULL, NULL),
-- 4. DRAFT session (25BCS801 OOPS Thu 19 Nov)
(2, 1, 4, '2026-11-19', '10:00', '11:00', 'DRAFT', 1, NULL, NULL),
-- 5. CANCELLED session (25BCS801 OS Fri 20 Nov)
(3, 1, 5, '2026-11-20', '11:00', '12:00', 'CANCELLED', 1, NULL, NULL)
ON CONFLICT (section_subject_id, session_date, start_time) DO NOTHING;

-- Attendance Records for Finalized Session 1 (all 3 students)
INSERT INTO attendance_records (class_session_id, student_section_enrollment_id, student_id, section_id, attendance_status, marked_by) VALUES
(1, 1, 3, 1, 'PRESENT', 1),
(1, 2, 4, 1, 'PRESENT', 1),
(1, 3, 5, 1, 'ABSENT',  1),
-- Attendance Records for Finalized Session 2 (all 3 students)
(2, 1, 3, 1, 'PRESENT', 1),
(2, 2, 4, 1, 'ABSENT',  1),
(2, 3, 5, 1, 'PRESENT', 1)
ON CONFLICT (class_session_id, student_section_enrollment_id) DO NOTHING;

-- Audit log
INSERT INTO attendance_audit_log (attendance_record_id, previous_status, new_status, changed_by, reason) VALUES
(3, 'PRESENT', 'ABSENT', 1, 'Student left lecture early');
