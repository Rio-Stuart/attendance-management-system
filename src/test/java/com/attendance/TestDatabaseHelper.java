package com.attendance;

import com.attendance.config.DatabaseConfig;
import com.attendance.model.*;
import com.attendance.model.enums.*;
import com.attendance.repository.*;
import com.attendance.repository.impl.*;
import com.attendance.service.*;
import com.attendance.service.impl.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Sets up a fresh in-memory H2 database for each test run.
 */
public class TestDatabaseHelper {

    private static int dbCounter = 0;

    // Repositories
    public UserRepository userRepository;
    public SectionRepository sectionRepository;
    public SubjectRepository subjectRepository;
    public SectionSubjectRepository sectionSubjectRepository;
    public StudentEnrollmentRepository enrollmentRepository;
    public ClassSessionRepository classSessionRepository;
    public AttendanceRepository attendanceRepository;
    public TimetableRepository timetableRepository;
    public HolidayRepository holidayRepository;
    public AttendanceAuditRepository auditRepository;

    // Services
    public ClassSessionService classSessionService;
    public AttendanceService attendanceService;
    public AttendanceCalculationService calculationService;
    public TimetableGenerationService timetableService;
    public AttendancePredictionService predictionService;
    public AttendanceReportService reportService;

    public void setup() {
        dbCounter++;
        String url = "jdbc:h2:mem:testdb_" + dbCounter + "_" + System.nanoTime() + ";DB_CLOSE_DELAY=-1";
        DatabaseConfig.setUrl(url);

        // Run schema
        DatabaseConfig.getInstance().executeSql(getSchemaSQL());

        // Initialize repos
        userRepository = new JdbcUserRepository();
        sectionRepository = new JdbcSectionRepository();
        subjectRepository = new JdbcSubjectRepository();
        sectionSubjectRepository = new JdbcSectionSubjectRepository();
        enrollmentRepository = new JdbcStudentEnrollmentRepository();
        classSessionRepository = new JdbcClassSessionRepository();
        attendanceRepository = new JdbcAttendanceRepository();
        timetableRepository = new JdbcTimetableRepository();
        holidayRepository = new JdbcHolidayRepository();
        auditRepository = new JdbcAttendanceAuditRepository();

        // Initialize services
        classSessionService = new ClassSessionServiceImpl(classSessionRepository, sectionSubjectRepository);
        attendanceService = new AttendanceServiceImpl(attendanceRepository, classSessionRepository,
                enrollmentRepository, auditRepository);
        calculationService = new AttendanceCalculationServiceImpl(attendanceRepository,
                classSessionRepository, enrollmentRepository, sectionSubjectRepository);
        timetableService = new TimetableGenerationServiceImpl(timetableRepository,
                classSessionRepository, holidayRepository, sectionSubjectRepository);
        predictionService = new AttendancePredictionServiceImpl(calculationService,
                classSessionRepository, enrollmentRepository, holidayRepository);
        reportService = new AttendanceReportServiceImpl(calculationService, enrollmentRepository,
                sectionSubjectRepository, userRepository, sectionRepository, subjectRepository);
    }

    // ---- Convenience methods to insert test data ----

    public User createAdmin(String username, String name) {
        User u = new User();
        u.setUsername(username);
        u.setPasswordHash("admin123");
        u.setFullName(name);
        u.setRole(UserRole.ADMIN);
        u.setActive(true);
        return userRepository.save(u);
    }

    public User createStudent(String username, String name) {
        User u = new User();
        u.setUsername(username);
        u.setPasswordHash("student123");
        u.setFullName(name);
        u.setRole(UserRole.STUDENT);
        u.setActive(true);
        return userRepository.save(u);
    }

    public Section createSection(String name, String year) {
        Section s = new Section();
        s.setSectionName(name);
        s.setAcademicYear(year);
        return sectionRepository.save(s);
    }

    public Subject createSubject(String code, String name) {
        Subject s = new Subject();
        s.setSubjectCode(code);
        s.setSubjectName(name);
        return subjectRepository.save(s);
    }

    public SectionSubject createSectionSubject(long sectionId, long subjectId) {
        SectionSubject ss = new SectionSubject();
        ss.setSectionId(sectionId);
        ss.setSubjectId(subjectId);
        return sectionSubjectRepository.save(ss);
    }

    public StudentSectionEnrollment createEnrollment(long studentId, long sectionId,
                                                      LocalDate startDate, LocalDate endDate) {
        StudentSectionEnrollment e = new StudentSectionEnrollment();
        e.setStudentId(studentId);
        e.setSectionId(sectionId);
        e.setAcademicYear("2026-27");
        e.setStartDate(startDate);
        e.setEndDate(endDate);
        e.setStatus(EnrollmentStatus.ACTIVE);
        return enrollmentRepository.save(e);
    }

    public ClassSession createSession(long sectionSubjectId, long sectionId,
                                       LocalDate date, LocalTime start, LocalTime end,
                                       SessionStatus status, long createdBy) {
        ClassSession cs = new ClassSession();
        cs.setSectionSubjectId(sectionSubjectId);
        cs.setSectionId(sectionId);
        cs.setSessionDate(date);
        cs.setStartTime(start);
        cs.setEndTime(end);
        cs.setStatus(status);
        cs.setCreatedBy(createdBy);
        ClassSession saved = classSessionRepository.save(cs);
        // If not SCHEDULED, update status (save defaults to what's in the model)
        if (status != SessionStatus.SCHEDULED && status != saved.getStatus()) {
            saved = classSessionRepository.updateStatus(saved.getId(), status, null, null);
        }
        return saved;
    }

    public WeeklyTimetable createTimetableEntry(long sectionSubjectId, DayOfWeek day,
                                                  LocalTime start, LocalTime end,
                                                  LocalDate validFrom, LocalDate validTo) {
        WeeklyTimetable wt = new WeeklyTimetable();
        wt.setSectionSubjectId(sectionSubjectId);
        wt.setDayOfWeek(day);
        wt.setStartTime(start);
        wt.setEndTime(end);
        wt.setValidFrom(validFrom);
        wt.setValidTo(validTo);
        return timetableRepository.save(wt);
    }

    public UniversityHoliday createHoliday(LocalDate date, String description) {
        UniversityHoliday h = new UniversityHoliday();
        h.setHolidayDate(date);
        h.setDescription(description);
        return holidayRepository.save(h);
    }

    public void markAndFinalize(long sessionId, long sectionId, LocalDate sessionDate, long adminId) {
        java.util.List<StudentSectionEnrollment> roster =
                enrollmentRepository.findActiveEnrollmentsBySectionIdOnDate(sectionId, sessionDate);
        for (StudentSectionEnrollment e : roster) {
            attendanceService.markAttendance(sessionId, e.getStudentId(), e.getId(),
                    AttendanceStatus.PRESENT, adminId);
        }
        attendanceService.finalizeAttendance(sessionId, adminId);
    }

    // ---- Schema SQL (inline to avoid file dependency in tests) ----

    private String getSchemaSQL() {
        return """
            CREATE TABLE IF NOT EXISTS users (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                username VARCHAR(50) NOT NULL UNIQUE,
                password_hash VARCHAR(255) NOT NULL,
                full_name VARCHAR(100) NOT NULL,
                email VARCHAR(100),
                role VARCHAR(10) NOT NULL CHECK (role IN ('ADMIN','STUDENT')),
                is_active BOOLEAN NOT NULL DEFAULT TRUE,
                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
            );
            CREATE TABLE IF NOT EXISTS sections (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                section_name VARCHAR(20) NOT NULL UNIQUE,
                academic_year VARCHAR(10) NOT NULL,
                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
            );
            CREATE TABLE IF NOT EXISTS subjects (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                subject_code VARCHAR(20) NOT NULL UNIQUE,
                subject_name VARCHAR(100) NOT NULL,
                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
            );
            CREATE TABLE IF NOT EXISTS section_subjects (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                section_id BIGINT NOT NULL,
                subject_id BIGINT NOT NULL,
                CONSTRAINT fk_ss_section FOREIGN KEY (section_id) REFERENCES sections(id),
                CONSTRAINT fk_ss_subject FOREIGN KEY (subject_id) REFERENCES subjects(id),
                CONSTRAINT uq_section_subject UNIQUE (section_id, subject_id)
            );
            CREATE TABLE IF NOT EXISTS student_section_enrollments (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                student_id BIGINT NOT NULL,
                section_id BIGINT NOT NULL,
                academic_year VARCHAR(10) NOT NULL,
                start_date DATE NOT NULL,
                end_date DATE,
                status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','INACTIVE')),
                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                CONSTRAINT fk_sse_student FOREIGN KEY (student_id) REFERENCES users(id),
                CONSTRAINT fk_sse_section FOREIGN KEY (section_id) REFERENCES sections(id)
            );
            CREATE TABLE IF NOT EXISTS weekly_timetable (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                section_subject_id BIGINT NOT NULL,
                day_of_week VARCHAR(10) NOT NULL,
                start_time TIME NOT NULL,
                end_time TIME NOT NULL,
                valid_from DATE,
                valid_to DATE,
                CONSTRAINT fk_wt_ss FOREIGN KEY (section_subject_id) REFERENCES section_subjects(id),
                CONSTRAINT chk_wt_time CHECK (start_time < end_time)
            );
            CREATE TABLE IF NOT EXISTS university_holidays (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                holiday_date DATE NOT NULL UNIQUE,
                description VARCHAR(200)
            );
            CREATE TABLE IF NOT EXISTS class_sessions (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                section_subject_id BIGINT NOT NULL,
                section_id BIGINT NOT NULL,
                timetable_entry_id BIGINT,
                session_date DATE NOT NULL,
                start_time TIME NOT NULL,
                end_time TIME NOT NULL,
                status VARCHAR(15) NOT NULL DEFAULT 'SCHEDULED',
                created_by BIGINT,
                finalized_by BIGINT,
                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                finalized_at TIMESTAMP,
                cancellation_reason VARCHAR(500),
                CONSTRAINT fk_cs_ss FOREIGN KEY (section_subject_id) REFERENCES section_subjects(id),
                CONSTRAINT fk_cs_sec FOREIGN KEY (section_id) REFERENCES sections(id),
                CONSTRAINT chk_cs_time CHECK (start_time < end_time),
                CONSTRAINT uq_class_session UNIQUE (section_subject_id, session_date, start_time)
            );
            CREATE TABLE IF NOT EXISTS attendance_records (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                class_session_id BIGINT NOT NULL,
                student_section_enrollment_id BIGINT NOT NULL,
                student_id BIGINT NOT NULL,
                section_id BIGINT NOT NULL,
                attendance_status VARCHAR(10) NOT NULL CHECK (attendance_status IN ('PRESENT','ABSENT')),
                marked_by BIGINT,
                marked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                CONSTRAINT fk_ar_session FOREIGN KEY (class_session_id) REFERENCES class_sessions(id),
                CONSTRAINT fk_ar_enroll FOREIGN KEY (student_section_enrollment_id) REFERENCES student_section_enrollments(id),
                CONSTRAINT fk_ar_student FOREIGN KEY (student_id) REFERENCES users(id),
                CONSTRAINT uq_attendance UNIQUE (class_session_id, student_section_enrollment_id)
            );
            CREATE TABLE IF NOT EXISTS attendance_audit_log (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                attendance_record_id BIGINT NOT NULL,
                previous_status VARCHAR(10),
                new_status VARCHAR(10),
                changed_by BIGINT,
                changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                reason VARCHAR(500),
                CONSTRAINT fk_aal_record FOREIGN KEY (attendance_record_id) REFERENCES attendance_records(id)
            );
            """;
    }
}
