package com.attendance;

import com.attendance.model.*;
import com.attendance.model.enums.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.attendance.TestFramework.*;

/**
 * All 20 mandatory test cases for the Attendance Management System.
 */
public class AttendanceSystemTest {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("  ATTENDANCE MANAGEMENT SYSTEM — TEST SUITE");
        System.out.println("=================================================\n");

        reset();

        test01_CreateClassSession();
        test02_PreventDuplicateClassSession();
        test03_LoadCorrectSectionRoster();
        test04_RecordPresentAttendance();
        test05_RecordAbsentAttendance();
        test06_PreventDuplicateAttendance();
        test07_PreventAttendanceForWrongSection();
        test08_PreventAttendanceForInactiveEnrollment();
        test09_SaveDraftAttendance();
        test10_RejectIncompleteFinalization();
        test11_SuccessfulFinalization();
        test12_ExcludeDraftFromCalculations();
        test13_ExcludeCancelledFromCalculations();
        test14_ExcludeFutureScheduledFromCalculations();
        test15_SubjectWisePercentage();
        test16_OverallWeightedAttendance();
        test17_PreserveAttendanceAfterSectionTransfer();
        test18_GenerateTimetableWithoutDuplicates();
        test19_ExcludeHolidaysFromPrediction();
        test20_FutureAttendanceProjection();

        printSummary();
        System.exit(getFailedCount() > 0 ? 1 : 0);
    }

    // ===== TEST 1: Creating a class session =====
    static void test01_CreateClassSession() {
        System.out.println("\n--- Test 1: Create Class Session ---");
        try {
            TestDatabaseHelper h = new TestDatabaseHelper();
            h.setup();
            User admin = h.createAdmin("admin1", "Admin One");
            Section sec = h.createSection("SEC-A", "2026-27");
            Subject subj = h.createSubject("CS101", "DBMS");
            SectionSubject ss = h.createSectionSubject(sec.getId(), subj.getId());

            ClassSession session = h.classSessionService.createSession(
                    ss.getId(), sec.getId(),
                    LocalDate.of(2026, 11, 16), LocalTime.of(10, 0), LocalTime.of(11, 0),
                    null, admin.getId());

            assertTrue(session.getId() > 0, "Session has generated ID");
            assertEquals(SessionStatus.SCHEDULED, session.getStatus(), "Session status is SCHEDULED");
            assertEquals(LocalDate.of(2026, 11, 16), session.getSessionDate(), "Session date correct");
        } catch (Exception e) {
            fail(e, "Test 1");
        }
    }

    // ===== TEST 2: Preventing duplicate class sessions =====
    static void test02_PreventDuplicateClassSession() {
        System.out.println("\n--- Test 2: Prevent Duplicate Class Session ---");
        try {
            TestDatabaseHelper h = new TestDatabaseHelper();
            h.setup();
            User admin = h.createAdmin("admin1", "Admin One");
            Section sec = h.createSection("SEC-A", "2026-27");
            Subject subj = h.createSubject("CS101", "DBMS");
            SectionSubject ss = h.createSectionSubject(sec.getId(), subj.getId());

            h.classSessionService.createSession(ss.getId(), sec.getId(),
                    LocalDate.of(2026, 11, 16), LocalTime.of(10, 0), LocalTime.of(11, 0),
                    null, admin.getId());

            assertAnyThrows(() -> h.classSessionService.createSession(ss.getId(), sec.getId(),
                    LocalDate.of(2026, 11, 16), LocalTime.of(10, 0), LocalTime.of(11, 0),
                    null, admin.getId()),
                    "Duplicate session is rejected");
        } catch (Exception e) {
            fail(e, "Test 2");
        }
    }

    // ===== TEST 3: Loading the correct section roster =====
    static void test03_LoadCorrectSectionRoster() {
        System.out.println("\n--- Test 3: Load Correct Section Roster ---");
        try {
            TestDatabaseHelper h = new TestDatabaseHelper();
            h.setup();
            Section secA = h.createSection("SEC-A", "2026-27");
            Section secB = h.createSection("SEC-B", "2026-27");
            User s1 = h.createStudent("S001", "Student 1");
            User s2 = h.createStudent("S002", "Student 2");
            User s3 = h.createStudent("S003", "Student 3");
            User s4 = h.createStudent("S004", "Student 4");

            LocalDate start = LocalDate.of(2026, 8, 1);
            h.createEnrollment(s1.getId(), secA.getId(), start, null);
            h.createEnrollment(s2.getId(), secA.getId(), start, null);
            h.createEnrollment(s3.getId(), secA.getId(), start, null);
            h.createEnrollment(s4.getId(), secB.getId(), start, null); // different section

            LocalDate date = LocalDate.of(2026, 11, 16);
            List<StudentSectionEnrollment> roster =
                    h.enrollmentRepository.findActiveEnrollmentsBySectionIdOnDate(secA.getId(), date);

            assertEquals(3, roster.size(), "Section A has 3 students");
        } catch (Exception e) {
            fail(e, "Test 3");
        }
    }

    // ===== TEST 4: Recording PRESENT attendance =====
    static void test04_RecordPresentAttendance() {
        System.out.println("\n--- Test 4: Record PRESENT Attendance ---");
        try {
            TestDatabaseHelper h = new TestDatabaseHelper();
            h.setup();
            User admin = h.createAdmin("admin1", "Admin");
            Section sec = h.createSection("SEC-A", "2026-27");
            Subject subj = h.createSubject("CS101", "DBMS");
            SectionSubject ss = h.createSectionSubject(sec.getId(), subj.getId());
            User student = h.createStudent("S001", "Student 1");
            StudentSectionEnrollment enrollment = h.createEnrollment(student.getId(), sec.getId(),
                    LocalDate.of(2026, 8, 1), null);

            ClassSession session = h.createSession(ss.getId(), sec.getId(),
                    LocalDate.of(2026, 11, 10), LocalTime.of(10, 0), LocalTime.of(11, 0),
                    SessionStatus.DRAFT, admin.getId());

            h.attendanceService.markAttendance(session.getId(), student.getId(),
                    enrollment.getId(), AttendanceStatus.PRESENT, admin.getId());

            var records = h.attendanceRepository.findByClassSessionId(session.getId());
            assertEquals(1, records.size(), "One attendance record exists");
            assertEquals(AttendanceStatus.PRESENT, records.get(0).getAttendanceStatus(), "Status is PRESENT");
        } catch (Exception e) {
            fail(e, "Test 4");
        }
    }

    // ===== TEST 5: Recording ABSENT attendance =====
    static void test05_RecordAbsentAttendance() {
        System.out.println("\n--- Test 5: Record ABSENT Attendance ---");
        try {
            TestDatabaseHelper h = new TestDatabaseHelper();
            h.setup();
            User admin = h.createAdmin("admin1", "Admin");
            Section sec = h.createSection("SEC-A", "2026-27");
            Subject subj = h.createSubject("CS101", "DBMS");
            SectionSubject ss = h.createSectionSubject(sec.getId(), subj.getId());
            User student = h.createStudent("S001", "Student 1");
            StudentSectionEnrollment enrollment = h.createEnrollment(student.getId(), sec.getId(),
                    LocalDate.of(2026, 8, 1), null);

            ClassSession session = h.createSession(ss.getId(), sec.getId(),
                    LocalDate.of(2026, 11, 10), LocalTime.of(10, 0), LocalTime.of(11, 0),
                    SessionStatus.DRAFT, admin.getId());

            h.attendanceService.markAttendance(session.getId(), student.getId(),
                    enrollment.getId(), AttendanceStatus.ABSENT, admin.getId());

            var records = h.attendanceRepository.findByClassSessionId(session.getId());
            assertEquals(AttendanceStatus.ABSENT, records.get(0).getAttendanceStatus(), "Status is ABSENT");
        } catch (Exception e) {
            fail(e, "Test 5");
        }
    }

    // ===== TEST 6: Preventing duplicate attendance records =====
    static void test06_PreventDuplicateAttendance() {
        System.out.println("\n--- Test 6: Prevent Duplicate Attendance ---");
        try {
            TestDatabaseHelper h = new TestDatabaseHelper();
            h.setup();
            User admin = h.createAdmin("admin1", "Admin");
            Section sec = h.createSection("SEC-A", "2026-27");
            Subject subj = h.createSubject("CS101", "DBMS");
            SectionSubject ss = h.createSectionSubject(sec.getId(), subj.getId());
            User student = h.createStudent("S001", "Student 1");
            StudentSectionEnrollment enrollment = h.createEnrollment(student.getId(), sec.getId(),
                    LocalDate.of(2026, 8, 1), null);

            ClassSession session = h.createSession(ss.getId(), sec.getId(),
                    LocalDate.of(2026, 11, 10), LocalTime.of(10, 0), LocalTime.of(11, 0),
                    SessionStatus.DRAFT, admin.getId());

            h.attendanceService.markAttendance(session.getId(), student.getId(),
                    enrollment.getId(), AttendanceStatus.PRESENT, admin.getId());

            assertAnyThrows(() -> h.attendanceService.markAttendance(session.getId(), student.getId(),
                    enrollment.getId(), AttendanceStatus.ABSENT, admin.getId()),
                    "Duplicate attendance is rejected");
        } catch (Exception e) {
            fail(e, "Test 6");
        }
    }

    // ===== TEST 7: Preventing attendance for wrong section =====
    static void test07_PreventAttendanceForWrongSection() {
        System.out.println("\n--- Test 7: Prevent Attendance For Wrong Section ---");
        try {
            TestDatabaseHelper h = new TestDatabaseHelper();
            h.setup();
            User admin = h.createAdmin("admin1", "Admin");
            Section secA = h.createSection("SEC-A", "2026-27");
            Section secB = h.createSection("SEC-B", "2026-27");
            Subject subj = h.createSubject("CS101", "DBMS");
            SectionSubject ssA = h.createSectionSubject(secA.getId(), subj.getId());
            User student = h.createStudent("S001", "Student 1");
            // Enrolled in section B
            StudentSectionEnrollment enrollment = h.createEnrollment(student.getId(), secB.getId(),
                    LocalDate.of(2026, 8, 1), null);

            // Session in section A
            ClassSession session = h.createSession(ssA.getId(), secA.getId(),
                    LocalDate.of(2026, 11, 10), LocalTime.of(10, 0), LocalTime.of(11, 0),
                    SessionStatus.DRAFT, admin.getId());

            assertAnyThrows(() -> h.attendanceService.markAttendance(session.getId(), student.getId(),
                    enrollment.getId(), AttendanceStatus.PRESENT, admin.getId()),
                    "Attendance for wrong section is rejected");
        } catch (Exception e) {
            fail(e, "Test 7");
        }
    }

    // ===== TEST 8: Preventing attendance for inactive enrollment =====
    static void test08_PreventAttendanceForInactiveEnrollment() {
        System.out.println("\n--- Test 8: Prevent Attendance For Inactive Enrollment ---");
        try {
            TestDatabaseHelper h = new TestDatabaseHelper();
            h.setup();
            User admin = h.createAdmin("admin1", "Admin");
            Section sec = h.createSection("SEC-A", "2026-27");
            Subject subj = h.createSubject("CS101", "DBMS");
            SectionSubject ss = h.createSectionSubject(sec.getId(), subj.getId());
            User student = h.createStudent("S001", "Student 1");

            // Enrollment ended on Oct 1
            StudentSectionEnrollment enrollment = h.createEnrollment(student.getId(), sec.getId(),
                    LocalDate.of(2026, 8, 1), LocalDate.of(2026, 10, 1));

            // Session on Nov 1 (after enrollment ended)
            ClassSession session = h.createSession(ss.getId(), sec.getId(),
                    LocalDate.of(2026, 11, 1), LocalTime.of(10, 0), LocalTime.of(11, 0),
                    SessionStatus.DRAFT, admin.getId());

            assertAnyThrows(() -> h.attendanceService.markAttendance(session.getId(), student.getId(),
                    enrollment.getId(), AttendanceStatus.PRESENT, admin.getId()),
                    "Attendance for expired enrollment is rejected");
        } catch (Exception e) {
            fail(e, "Test 8");
        }
    }

    // ===== TEST 9: Saving incomplete draft attendance =====
    static void test09_SaveDraftAttendance() {
        System.out.println("\n--- Test 9: Save Draft Attendance ---");
        try {
            TestDatabaseHelper h = new TestDatabaseHelper();
            h.setup();
            User admin = h.createAdmin("admin1", "Admin");
            Section sec = h.createSection("SEC-A", "2026-27");
            Subject subj = h.createSubject("CS101", "DBMS");
            SectionSubject ss = h.createSectionSubject(sec.getId(), subj.getId());
            User s1 = h.createStudent("S001", "Student 1");
            User s2 = h.createStudent("S002", "Student 2");
            User s3 = h.createStudent("S003", "Student 3");
            StudentSectionEnrollment e1 = h.createEnrollment(s1.getId(), sec.getId(), LocalDate.of(2026, 8, 1), null);
            StudentSectionEnrollment e2 = h.createEnrollment(s2.getId(), sec.getId(), LocalDate.of(2026, 8, 1), null);
            h.createEnrollment(s3.getId(), sec.getId(), LocalDate.of(2026, 8, 1), null);

            ClassSession session = h.createSession(ss.getId(), sec.getId(),
                    LocalDate.of(2026, 11, 10), LocalTime.of(10, 0), LocalTime.of(11, 0),
                    SessionStatus.SCHEDULED, admin.getId());

            // Save draft for only 2 of 3 students
            Map<Long, AttendanceStatus> partial = new HashMap<>();
            partial.put(e1.getId(), AttendanceStatus.PRESENT);
            partial.put(e2.getId(), AttendanceStatus.ABSENT);
            h.attendanceService.saveDraft(session.getId(), partial, admin.getId());

            // Verify session is DRAFT
            ClassSession updated = h.classSessionRepository.findById(session.getId()).orElse(null);
            assertEquals(SessionStatus.DRAFT, updated.getStatus(), "Session is in DRAFT state");

            // Verify 2 records exist
            var records = h.attendanceRepository.findByClassSessionId(session.getId());
            assertEquals(2, records.size(), "2 draft attendance records saved");
        } catch (Exception e) {
            fail(e, "Test 9");
        }
    }

    // ===== TEST 10: Rejecting incomplete finalization =====
    static void test10_RejectIncompleteFinalization() {
        System.out.println("\n--- Test 10: Reject Incomplete Finalization ---");
        try {
            TestDatabaseHelper h = new TestDatabaseHelper();
            h.setup();
            User admin = h.createAdmin("admin1", "Admin");
            Section sec = h.createSection("SEC-A", "2026-27");
            Subject subj = h.createSubject("CS101", "DBMS");
            SectionSubject ss = h.createSectionSubject(sec.getId(), subj.getId());
            User s1 = h.createStudent("S001", "Student 1");
            User s2 = h.createStudent("S002", "Student 2");
            User s3 = h.createStudent("S003", "Student 3");
            StudentSectionEnrollment e1 = h.createEnrollment(s1.getId(), sec.getId(), LocalDate.of(2026, 8, 1), null);
            StudentSectionEnrollment e2 = h.createEnrollment(s2.getId(), sec.getId(), LocalDate.of(2026, 8, 1), null);
            h.createEnrollment(s3.getId(), sec.getId(), LocalDate.of(2026, 8, 1), null);

            ClassSession session = h.createSession(ss.getId(), sec.getId(),
                    LocalDate.of(2026, 11, 10), LocalTime.of(10, 0), LocalTime.of(11, 0),
                    SessionStatus.DRAFT, admin.getId());

            // Mark only 2 of 3
            h.attendanceService.markAttendance(session.getId(), s1.getId(), e1.getId(), AttendanceStatus.PRESENT, admin.getId());
            h.attendanceService.markAttendance(session.getId(), s2.getId(), e2.getId(), AttendanceStatus.ABSENT, admin.getId());

            // Finalize should fail
            assertAnyThrows(() -> h.attendanceService.finalizeAttendance(session.getId(), admin.getId()),
                    "Incomplete finalization is rejected");

            // Session should NOT be FINALIZED
            ClassSession check = h.classSessionRepository.findById(session.getId()).orElse(null);
            assertTrue(check.getStatus() != SessionStatus.FINALIZED, "Session is not FINALIZED after failed attempt");
        } catch (Exception e) {
            fail(e, "Test 10");
        }
    }

    // ===== TEST 11: Successful finalization =====
    static void test11_SuccessfulFinalization() {
        System.out.println("\n--- Test 11: Successful Finalization ---");
        try {
            TestDatabaseHelper h = new TestDatabaseHelper();
            h.setup();
            User admin = h.createAdmin("admin1", "Admin");
            Section sec = h.createSection("SEC-A", "2026-27");
            Subject subj = h.createSubject("CS101", "DBMS");
            SectionSubject ss = h.createSectionSubject(sec.getId(), subj.getId());
            User s1 = h.createStudent("S001", "Student 1");
            User s2 = h.createStudent("S002", "Student 2");
            User s3 = h.createStudent("S003", "Student 3");
            StudentSectionEnrollment e1 = h.createEnrollment(s1.getId(), sec.getId(), LocalDate.of(2026, 8, 1), null);
            StudentSectionEnrollment e2 = h.createEnrollment(s2.getId(), sec.getId(), LocalDate.of(2026, 8, 1), null);
            StudentSectionEnrollment e3 = h.createEnrollment(s3.getId(), sec.getId(), LocalDate.of(2026, 8, 1), null);

            ClassSession session = h.createSession(ss.getId(), sec.getId(),
                    LocalDate.of(2026, 11, 10), LocalTime.of(10, 0), LocalTime.of(11, 0),
                    SessionStatus.DRAFT, admin.getId());

            // Mark all 3
            h.attendanceService.markAttendance(session.getId(), s1.getId(), e1.getId(), AttendanceStatus.PRESENT, admin.getId());
            h.attendanceService.markAttendance(session.getId(), s2.getId(), e2.getId(), AttendanceStatus.PRESENT, admin.getId());
            h.attendanceService.markAttendance(session.getId(), s3.getId(), e3.getId(), AttendanceStatus.ABSENT, admin.getId());

            // Finalize
            h.attendanceService.finalizeAttendance(session.getId(), admin.getId());

            ClassSession finalized = h.classSessionRepository.findById(session.getId()).orElse(null);
            assertEquals(SessionStatus.FINALIZED, finalized.getStatus(), "Session is FINALIZED");
            assertTrue(finalized.getFinalizedBy() != null, "FinalizedBy is set");
            assertTrue(finalized.getFinalizedAt() != null, "FinalizedAt is set");
        } catch (Exception e) {
            fail(e, "Test 11");
        }
    }

    // ===== TEST 12: Exclude DRAFT from calculations =====
    static void test12_ExcludeDraftFromCalculations() {
        System.out.println("\n--- Test 12: Exclude Draft From Calculations ---");
        try {
            TestDatabaseHelper h = new TestDatabaseHelper();
            h.setup();
            User admin = h.createAdmin("admin1", "Admin");
            Section sec = h.createSection("SEC-A", "2026-27");
            Subject subj = h.createSubject("CS101", "DBMS");
            SectionSubject ss = h.createSectionSubject(sec.getId(), subj.getId());
            User student = h.createStudent("S001", "Student 1");
            StudentSectionEnrollment enrollment = h.createEnrollment(student.getId(), sec.getId(),
                    LocalDate.of(2026, 8, 1), null);

            // Session 1: FINALIZED with PRESENT
            ClassSession session1 = h.createSession(ss.getId(), sec.getId(),
                    LocalDate.of(2026, 11, 4), LocalTime.of(10, 0), LocalTime.of(11, 0),
                    SessionStatus.DRAFT, admin.getId());
            h.attendanceService.markAttendance(session1.getId(), student.getId(), enrollment.getId(),
                    AttendanceStatus.PRESENT, admin.getId());
            h.attendanceService.finalizeAttendance(session1.getId(), admin.getId());

            // Session 2: DRAFT with PRESENT (should NOT count)
            ClassSession session2 = h.createSession(ss.getId(), sec.getId(),
                    LocalDate.of(2026, 11, 6), LocalTime.of(10, 0), LocalTime.of(11, 0),
                    SessionStatus.DRAFT, admin.getId());
            h.attendanceService.markAttendance(session2.getId(), student.getId(), enrollment.getId(),
                    AttendanceStatus.PRESENT, admin.getId());

            double pct = h.calculationService.calculateSubjectAttendance(student.getId(), ss.getId());
            assertEqualsDouble(100.0, pct, 0.01, "Only finalized session counts: 1/1 = 100%");
            assertEquals(1, h.calculationService.getClassesConducted(student.getId(), ss.getId()),
                    "Conducted = 1 (not 2)");
        } catch (Exception e) {
            fail(e, "Test 12");
        }
    }

    // ===== TEST 13: Exclude CANCELLED from calculations =====
    static void test13_ExcludeCancelledFromCalculations() {
        System.out.println("\n--- Test 13: Exclude Cancelled From Calculations ---");
        try {
            TestDatabaseHelper h = new TestDatabaseHelper();
            h.setup();
            User admin = h.createAdmin("admin1", "Admin");
            Section sec = h.createSection("SEC-A", "2026-27");
            Subject subj = h.createSubject("CS101", "DBMS");
            SectionSubject ss = h.createSectionSubject(sec.getId(), subj.getId());
            User student = h.createStudent("S001", "Student 1");
            StudentSectionEnrollment enrollment = h.createEnrollment(student.getId(), sec.getId(),
                    LocalDate.of(2026, 8, 1), null);

            // Session 1: FINALIZED with PRESENT
            ClassSession session1 = h.createSession(ss.getId(), sec.getId(),
                    LocalDate.of(2026, 11, 4), LocalTime.of(10, 0), LocalTime.of(11, 0),
                    SessionStatus.DRAFT, admin.getId());
            h.attendanceService.markAttendance(session1.getId(), student.getId(), enrollment.getId(),
                    AttendanceStatus.PRESENT, admin.getId());
            h.attendanceService.finalizeAttendance(session1.getId(), admin.getId());

            // Session 2: CANCELLED
            h.createSession(ss.getId(), sec.getId(),
                    LocalDate.of(2026, 11, 6), LocalTime.of(10, 0), LocalTime.of(11, 0),
                    SessionStatus.CANCELLED, admin.getId());

            double pct = h.calculationService.calculateSubjectAttendance(student.getId(), ss.getId());
            assertEqualsDouble(100.0, pct, 0.01, "Cancelled session excluded: 1/1 = 100%");
        } catch (Exception e) {
            fail(e, "Test 13");
        }
    }

    // ===== TEST 14: Exclude future SCHEDULED from actual attendance =====
    static void test14_ExcludeFutureScheduledFromCalculations() {
        System.out.println("\n--- Test 14: Exclude Future Scheduled From Calculations ---");
        try {
            TestDatabaseHelper h = new TestDatabaseHelper();
            h.setup();
            User admin = h.createAdmin("admin1", "Admin");
            Section sec = h.createSection("SEC-A", "2026-27");
            Subject subj = h.createSubject("CS101", "DBMS");
            SectionSubject ss = h.createSectionSubject(sec.getId(), subj.getId());
            User student = h.createStudent("S001", "Student 1");
            StudentSectionEnrollment enrollment = h.createEnrollment(student.getId(), sec.getId(),
                    LocalDate.of(2026, 8, 1), null);

            // FINALIZED session
            ClassSession session1 = h.createSession(ss.getId(), sec.getId(),
                    LocalDate.of(2026, 11, 4), LocalTime.of(10, 0), LocalTime.of(11, 0),
                    SessionStatus.DRAFT, admin.getId());
            h.attendanceService.markAttendance(session1.getId(), student.getId(), enrollment.getId(),
                    AttendanceStatus.PRESENT, admin.getId());
            h.attendanceService.finalizeAttendance(session1.getId(), admin.getId());

            // Future SCHEDULED session
            h.createSession(ss.getId(), sec.getId(),
                    LocalDate.of(2027, 1, 5), LocalTime.of(10, 0), LocalTime.of(11, 0),
                    SessionStatus.SCHEDULED, admin.getId());

            double pct = h.calculationService.calculateSubjectAttendance(student.getId(), ss.getId());
            assertEqualsDouble(100.0, pct, 0.01, "Future scheduled excluded: 1/1 = 100%");
        } catch (Exception e) {
            fail(e, "Test 14");
        }
    }

    // ===== TEST 15: Subject-wise percentage =====
    static void test15_SubjectWisePercentage() {
        System.out.println("\n--- Test 15: Subject-wise Percentage ---");
        try {
            TestDatabaseHelper h = new TestDatabaseHelper();
            h.setup();
            User admin = h.createAdmin("admin1", "Admin");
            Section sec = h.createSection("SEC-A", "2026-27");
            Subject subjA = h.createSubject("CS101", "DBMS");
            Subject subjB = h.createSubject("CS102", "OOPS");
            SectionSubject ssA = h.createSectionSubject(sec.getId(), subjA.getId());
            SectionSubject ssB = h.createSectionSubject(sec.getId(), subjB.getId());
            User student = h.createStudent("S001", "Student 1");
            StudentSectionEnrollment enrollment = h.createEnrollment(student.getId(), sec.getId(),
                    LocalDate.of(2026, 8, 1), null);

            // Subject A: 3 sessions — 2 PRESENT, 1 ABSENT → 66.67%
            for (int i = 0; i < 3; i++) {
                ClassSession s = h.createSession(ssA.getId(), sec.getId(),
                        LocalDate.of(2026, 11, 2 + i), LocalTime.of(10, 0), LocalTime.of(11, 0),
                        SessionStatus.DRAFT, admin.getId());
                h.attendanceService.markAttendance(s.getId(), student.getId(), enrollment.getId(),
                        i < 2 ? AttendanceStatus.PRESENT : AttendanceStatus.ABSENT, admin.getId());
                h.attendanceService.finalizeAttendance(s.getId(), admin.getId());
            }

            // Subject B: 2 sessions — both PRESENT → 100%
            for (int i = 0; i < 2; i++) {
                ClassSession s = h.createSession(ssB.getId(), sec.getId(),
                        LocalDate.of(2026, 11, 5 + i), LocalTime.of(11, 0), LocalTime.of(12, 0),
                        SessionStatus.DRAFT, admin.getId());
                h.attendanceService.markAttendance(s.getId(), student.getId(), enrollment.getId(),
                        AttendanceStatus.PRESENT, admin.getId());
                h.attendanceService.finalizeAttendance(s.getId(), admin.getId());
            }

            double pctA = h.calculationService.calculateSubjectAttendance(student.getId(), ssA.getId());
            double pctB = h.calculationService.calculateSubjectAttendance(student.getId(), ssB.getId());
            assertEqualsDouble(66.67, pctA, 0.5, "Subject A: 2/3 ≈ 66.67%");
            assertEqualsDouble(100.0, pctB, 0.01, "Subject B: 2/2 = 100%");
        } catch (Exception e) {
            fail(e, "Test 15");
        }
    }

    // ===== TEST 16: Overall weighted attendance (NOT average) =====
    static void test16_OverallWeightedAttendance() {
        System.out.println("\n--- Test 16: Overall Weighted Attendance ---");
        try {
            TestDatabaseHelper h = new TestDatabaseHelper();
            h.setup();
            User admin = h.createAdmin("admin1", "Admin");
            Section sec = h.createSection("SEC-A", "2026-27");
            Subject subjA = h.createSubject("CS101", "DBMS");
            Subject subjB = h.createSubject("CS102", "OOPS");
            SectionSubject ssA = h.createSectionSubject(sec.getId(), subjA.getId());
            SectionSubject ssB = h.createSectionSubject(sec.getId(), subjB.getId());
            User student = h.createStudent("S001", "Student 1");
            StudentSectionEnrollment enrollment = h.createEnrollment(student.getId(), sec.getId(),
                    LocalDate.of(2026, 8, 1), null);

            // Subject A: 3 sessions, 2 present (66.67%)
            for (int i = 0; i < 3; i++) {
                ClassSession s = h.createSession(ssA.getId(), sec.getId(),
                        LocalDate.of(2026, 11, 2 + i), LocalTime.of(10, 0), LocalTime.of(11, 0),
                        SessionStatus.DRAFT, admin.getId());
                h.attendanceService.markAttendance(s.getId(), student.getId(), enrollment.getId(),
                        i < 2 ? AttendanceStatus.PRESENT : AttendanceStatus.ABSENT, admin.getId());
                h.attendanceService.finalizeAttendance(s.getId(), admin.getId());
            }

            // Subject B: 2 sessions, 2 present (100%)
            for (int i = 0; i < 2; i++) {
                ClassSession s = h.createSession(ssB.getId(), sec.getId(),
                        LocalDate.of(2026, 11, 5 + i), LocalTime.of(11, 0), LocalTime.of(12, 0),
                        SessionStatus.DRAFT, admin.getId());
                h.attendanceService.markAttendance(s.getId(), student.getId(), enrollment.getId(),
                        AttendanceStatus.PRESENT, admin.getId());
                h.attendanceService.finalizeAttendance(s.getId(), admin.getId());
            }

            // Overall = (2+2)/(3+2) = 4/5 = 80%, NOT (66.67+100)/2 = 83.33%
            double overall = h.calculationService.calculateOverallAttendance(student.getId(), sec.getId());
            assertEqualsDouble(80.0, overall, 0.01, "Overall weighted = 4/5 = 80% (not 83.33%)");
        } catch (Exception e) {
            fail(e, "Test 16");
        }
    }

    // ===== TEST 17: Preserve attendance after section transfer =====
    static void test17_PreserveAttendanceAfterSectionTransfer() {
        System.out.println("\n--- Test 17: Preserve Attendance After Section Transfer ---");
        try {
            TestDatabaseHelper h = new TestDatabaseHelper();
            h.setup();
            User admin = h.createAdmin("admin1", "Admin");
            Section secA = h.createSection("SEC-A", "2026-27");
            Section secB = h.createSection("SEC-B", "2026-27");
            Subject subj = h.createSubject("CS101", "DBMS");
            SectionSubject ssA = h.createSectionSubject(secA.getId(), subj.getId());
            h.createSectionSubject(secB.getId(), subj.getId());
            User student = h.createStudent("S001", "Student 1");

            // Enroll in section A
            StudentSectionEnrollment enrollA = h.createEnrollment(student.getId(), secA.getId(),
                    LocalDate.of(2026, 8, 1), null);

            // Attend session in A
            ClassSession sessionA = h.createSession(ssA.getId(), secA.getId(),
                    LocalDate.of(2026, 10, 5), LocalTime.of(10, 0), LocalTime.of(11, 0),
                    SessionStatus.DRAFT, admin.getId());
            h.attendanceService.markAttendance(sessionA.getId(), student.getId(), enrollA.getId(),
                    AttendanceStatus.PRESENT, admin.getId());
            h.attendanceService.finalizeAttendance(sessionA.getId(), admin.getId());

            // Transfer: deactivate A, enroll in B
            h.enrollmentRepository.deactivate(enrollA.getId(), LocalDate.of(2026, 10, 15));
            h.createEnrollment(student.getId(), secB.getId(), LocalDate.of(2026, 10, 16), null);

            // Old attendance should still be retrievable
            var oldRecords = h.attendanceRepository.findByStudentId(student.getId());
            assertTrue(oldRecords.size() >= 1, "Old attendance records preserved");
            assertEquals(AttendanceStatus.PRESENT, oldRecords.get(0).getAttendanceStatus(),
                    "Historical attendance status preserved");
        } catch (Exception e) {
            fail(e, "Test 17");
        }
    }

    // ===== TEST 18: Generate timetable sessions without duplicates =====
    static void test18_GenerateTimetableWithoutDuplicates() {
        System.out.println("\n--- Test 18: Generate Timetable Without Duplicates ---");
        try {
            TestDatabaseHelper h = new TestDatabaseHelper();
            h.setup();
            Section sec = h.createSection("SEC-A", "2026-27");
            Subject subj = h.createSubject("CS101", "DBMS");
            SectionSubject ss = h.createSectionSubject(sec.getId(), subj.getId());

            // Monday 10-11 timetable entry
            h.createTimetableEntry(ss.getId(), DayOfWeek.MONDAY,
                    LocalTime.of(10, 0), LocalTime.of(11, 0),
                    LocalDate.of(2026, 8, 1), LocalDate.of(2027, 5, 31));

            // Generate for a 2-week range
            LocalDate from = LocalDate.of(2026, 12, 7); // Monday
            LocalDate to = LocalDate.of(2026, 12, 20);   // Sunday

            List<ClassSession> first = h.timetableService.generateSessionsForDateRange(sec.getId(), from, to);
            int firstCount = first.size();
            assertTrue(firstCount == 2, "First generation: 2 Mondays in range → 2 sessions");

            // Generate again — idempotent
            List<ClassSession> second = h.timetableService.generateSessionsForDateRange(sec.getId(), from, to);
            assertEquals(0, second.size(), "Second generation creates 0 new sessions (idempotent)");

            // Total sessions should still be 2
            var all = h.classSessionRepository.findBySectionSubjectId(ss.getId());
            assertEquals(2, all.size(), "Total sessions unchanged after second generation");
        } catch (Exception e) {
            fail(e, "Test 18");
        }
    }

    // ===== TEST 19: Exclude holidays from prediction =====
    static void test19_ExcludeHolidaysFromPrediction() {
        System.out.println("\n--- Test 19: Exclude Holidays From Prediction ---");
        try {
            TestDatabaseHelper h = new TestDatabaseHelper();
            h.setup();
            Section sec = h.createSection("SEC-A", "2026-27");
            Subject subj = h.createSubject("CS101", "DBMS");
            SectionSubject ss = h.createSectionSubject(sec.getId(), subj.getId());
            User student = h.createStudent("S001", "Student 1");
            h.createEnrollment(student.getId(), sec.getId(), LocalDate.of(2026, 8, 1), null);

            // Create Monday timetable
            h.createTimetableEntry(ss.getId(), DayOfWeek.MONDAY,
                    LocalTime.of(10, 0), LocalTime.of(11, 0),
                    LocalDate.of(2026, 8, 1), LocalDate.of(2027, 5, 31));

            // Holiday on a Monday
            LocalDate holidayMonday = LocalDate.of(2026, 12, 14); // a Monday
            h.createHoliday(holidayMonday, "Test Holiday");

            // Generate sessions for the week containing the holiday
            LocalDate from = LocalDate.of(2026, 12, 7);
            LocalDate to = LocalDate.of(2026, 12, 21);
            h.timetableService.generateSessionsForDateRange(sec.getId(), from, to);

            // Check that the holiday date has no session
            boolean exists = h.classSessionRepository.existsByUnique(
                    ss.getId(), holidayMonday, LocalTime.of(10, 0));
            assertFalse(exists, "No session generated on holiday date");
        } catch (Exception e) {
            fail(e, "Test 19");
        }
    }

    // ===== TEST 20: Future attendance projection =====
    static void test20_FutureAttendanceProjection() {
        System.out.println("\n--- Test 20: Future Attendance Projection ---");
        try {
            TestDatabaseHelper h = new TestDatabaseHelper();
            h.setup();
            User admin = h.createAdmin("admin1", "Admin");
            Section sec = h.createSection("SEC-A", "2026-27");
            Subject subj = h.createSubject("CS101", "DBMS");
            SectionSubject ss = h.createSectionSubject(sec.getId(), subj.getId());
            User student = h.createStudent("S001", "Student 1");
            StudentSectionEnrollment enrollment = h.createEnrollment(student.getId(), sec.getId(),
                    LocalDate.of(2026, 8, 1), null);

            // 2 FINALIZED sessions: 1 PRESENT, 1 ABSENT → P=1, T=2
            ClassSession s1 = h.createSession(ss.getId(), sec.getId(),
                    LocalDate.of(2026, 11, 2), LocalTime.of(10, 0), LocalTime.of(11, 0),
                    SessionStatus.DRAFT, admin.getId());
            h.attendanceService.markAttendance(s1.getId(), student.getId(), enrollment.getId(),
                    AttendanceStatus.PRESENT, admin.getId());
            h.attendanceService.finalizeAttendance(s1.getId(), admin.getId());

            ClassSession s2 = h.createSession(ss.getId(), sec.getId(),
                    LocalDate.of(2026, 11, 4), LocalTime.of(10, 0), LocalTime.of(11, 0),
                    SessionStatus.DRAFT, admin.getId());
            h.attendanceService.markAttendance(s2.getId(), student.getId(), enrollment.getId(),
                    AttendanceStatus.ABSENT, admin.getId());
            h.attendanceService.finalizeAttendance(s2.getId(), admin.getId());

            // 3 upcoming SCHEDULED sessions
            for (int i = 0; i < 3; i++) {
                h.createSession(ss.getId(), sec.getId(),
                        LocalDate.now().plusDays(i + 1), LocalTime.of(10, 0), LocalTime.of(11, 0),
                        SessionStatus.SCHEDULED, admin.getId());
            }

            Map<String, Object> prediction = h.predictionService.predictAttendance(
                    student.getId(), ss.getId(), 30);

            int T = (int) prediction.get("currentConducted");
            int P = (int) prediction.get("currentAttended");
            int K = (int) prediction.get("upcomingSessions");

            assertEquals(2, T, "Current conducted = 2");
            assertEquals(1, P, "Current attended = 1");
            assertEquals(3, K, "Upcoming sessions = 3");

            // Predicted if all attend: (1+3)/(2+3) = 4/5 = 80%
            double allAttend = (double) prediction.get("predictedIfAllAttend");
            assertEqualsDouble(80.0, allAttend, 0.01, "Predicted if all attend: (1+3)/(2+3) = 80%");

            // Predicted if all absent: 1/(2+3) = 20%
            double allAbsent = (double) prediction.get("predictedIfAllAbsent");
            assertEqualsDouble(20.0, allAbsent, 0.01, "Predicted if all absent: 1/(2+3) = 20%");
        } catch (Exception e) {
            fail(e, "Test 20");
        }
    }

    // ---- Helper ----
    private static void fail(Exception e, String context) {
        System.out.println("  ✗ FAIL: " + context + " — Unexpected exception: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        e.printStackTrace(System.out);
    }
}
