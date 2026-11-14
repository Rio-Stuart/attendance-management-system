-- netlify/database/migrations/20261114110000_seed_more_sessions.sql
-- Additional Class Sessions for Timetable History

INSERT INTO class_sessions (id, section_subject_id, section_id, timetable_entry_id, session_date, start_time, end_time, status, created_by, finalized_by, finalized_at) VALUES
(101, 1, 1, 1, '2026-08-03', '09:00', '10:00', 'FINALIZED', 1, 1, '2026-08-03 10:05:00'),
(102, 1, 1, 2, '2026-08-05', '09:00', '10:00', 'FINALIZED', 1, 1, '2026-08-05 10:05:00'),
(103, 1, 1, 1, '2026-08-10', '09:00', '10:00', 'FINALIZED', 1, 1, '2026-08-10 10:05:00'),
(104, 1, 1, 2, '2026-08-12', '09:00', '10:00', 'FINALIZED', 1, 1, '2026-08-12 10:05:00'),
(105, 1, 1, 1, '2026-08-17', '09:00', '10:00', 'FINALIZED', 1, 1, '2026-08-17 10:05:00'),
(106, 1, 1, 2, '2026-08-19', '09:00', '10:00', 'FINALIZED', 1, 1, '2026-08-19 10:05:00'),
(107, 2, 1, 3, '2026-08-04', '10:00', '11:00', 'FINALIZED', 1, 1, '2026-08-04 11:05:00'),
(108, 2, 1, 4, '2026-08-06', '10:00', '11:00', 'FINALIZED', 1, 1, '2026-08-06 11:05:00'),
(109, 2, 1, 3, '2026-08-11', '10:00', '11:00', 'FINALIZED', 1, 1, '2026-08-11 11:05:00'),
(110, 2, 1, 4, '2026-08-13', '10:00', '11:00', 'FINALIZED', 1, 1, '2026-08-13 11:05:00'),
(111, 2, 1, 3, '2026-08-18', '10:00', '11:00', 'FINALIZED', 1, 1, '2026-08-18 11:05:00'),
(112, 3, 1, 5, '2026-08-07', '11:00', '12:00', 'FINALIZED', 1, 1, '2026-08-07 12:05:00'),
(113, 3, 1, 5, '2026-08-14', '11:00', '12:00', 'FINALIZED', 1, 1, '2026-08-14 12:05:00'),
(114, 3, 1, 5, '2026-08-21', '11:00', '12:00', 'FINALIZED', 1, 1, '2026-08-21 12:05:00'),
(115, 3, 1, 5, '2026-08-28', '11:00', '12:00', 'FINALIZED', 1, 1, '2026-08-28 12:05:00')
ON CONFLICT (section_subject_id, session_date, start_time) DO NOTHING;

INSERT INTO attendance_records (class_session_id, student_section_enrollment_id, student_id, section_id, attendance_status, marked_by) VALUES
(101, 1, 3, 1, 'PRESENT', 1), (101, 2, 4, 1, 'PRESENT', 1), (101, 3, 5, 1, 'ABSENT', 1),
(102, 1, 3, 1, 'PRESENT', 1), (102, 2, 4, 1, 'ABSENT', 1), (102, 3, 5, 1, 'PRESENT', 1),
(103, 1, 3, 1, 'PRESENT', 1), (103, 2, 4, 1, 'PRESENT', 1), (103, 3, 5, 1, 'PRESENT', 1),
(104, 1, 3, 1, 'ABSENT', 1), (104, 2, 4, 1, 'PRESENT', 1), (104, 3, 5, 1, 'PRESENT', 1),
(105, 1, 3, 1, 'PRESENT', 1), (105, 2, 4, 1, 'PRESENT', 1), (105, 3, 5, 1, 'ABSENT', 1),
(106, 1, 3, 1, 'PRESENT', 1), (106, 2, 4, 1, 'ABSENT', 1), (106, 3, 5, 1, 'PRESENT', 1),
(107, 1, 3, 1, 'PRESENT', 1), (107, 2, 4, 1, 'PRESENT', 1), (107, 3, 5, 1, 'PRESENT', 1),
(108, 1, 3, 1, 'ABSENT', 1), (108, 2, 4, 1, 'PRESENT', 1), (108, 3, 5, 1, 'ABSENT', 1),
(109, 1, 3, 1, 'PRESENT', 1), (109, 2, 4, 1, 'ABSENT', 1), (109, 3, 5, 1, 'PRESENT', 1),
(110, 1, 3, 1, 'ABSENT', 1), (110, 2, 4, 1, 'PRESENT', 1), (110, 3, 5, 1, 'PRESENT', 1),
(111, 1, 3, 1, 'PRESENT', 1), (111, 2, 4, 1, 'PRESENT', 1), (111, 3, 5, 1, 'ABSENT', 1),
(112, 1, 3, 1, 'PRESENT', 1), (112, 2, 4, 1, 'PRESENT', 1), (112, 3, 5, 1, 'PRESENT', 1),
(113, 1, 3, 1, 'PRESENT', 1), (113, 2, 4, 1, 'ABSENT', 1), (113, 3, 5, 1, 'PRESENT', 1),
(114, 1, 3, 1, 'PRESENT', 1), (114, 2, 4, 1, 'PRESENT', 1), (114, 3, 5, 1, 'ABSENT', 1),
(115, 1, 3, 1, 'PRESENT', 1), (115, 2, 4, 1, 'PRESENT', 1), (115, 3, 5, 1, 'PRESENT', 1)
ON CONFLICT (class_session_id, student_section_enrollment_id) DO NOTHING;
