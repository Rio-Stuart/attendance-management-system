-- ============================================================
-- Attendance Management System — Database Schema (H2 Database)
-- ============================================================

-- Users table (admins and students)
CREATE TABLE IF NOT EXISTS users (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(50)  NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(100) NOT NULL,
    email           VARCHAR(100),
    role            VARCHAR(10)  NOT NULL CHECK (role IN ('ADMIN', 'STUDENT')),
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Academic sections
CREATE TABLE IF NOT EXISTS sections (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    section_name    VARCHAR(20)  NOT NULL UNIQUE,
    academic_year   VARCHAR(10)  NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Subjects
CREATE TABLE IF NOT EXISTS subjects (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    subject_code    VARCHAR(20)  NOT NULL UNIQUE,
    subject_name    VARCHAR(100) NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Many-to-many: which subjects are taught in which sections
CREATE TABLE IF NOT EXISTS section_subjects (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    section_id      BIGINT NOT NULL,
    subject_id      BIGINT NOT NULL,
    CONSTRAINT fk_ss_section  FOREIGN KEY (section_id) REFERENCES sections(id),
    CONSTRAINT fk_ss_subject  FOREIGN KEY (subject_id) REFERENCES subjects(id),
    CONSTRAINT uq_section_subject UNIQUE (section_id, subject_id)
);

-- Student enrollment history (preserves section transfers)
CREATE TABLE IF NOT EXISTS student_section_enrollments (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id      BIGINT       NOT NULL,
    section_id      BIGINT       NOT NULL,
    academic_year   VARCHAR(10)  NOT NULL,
    start_date      DATE         NOT NULL,
    end_date        DATE,
    status          VARCHAR(10)  NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sse_student FOREIGN KEY (student_id) REFERENCES users(id),
    CONSTRAINT fk_sse_section FOREIGN KEY (section_id) REFERENCES sections(id)
);

-- Weekly recurring timetable
CREATE TABLE IF NOT EXISTS weekly_timetable (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    section_subject_id BIGINT      NOT NULL,
    day_of_week       VARCHAR(10) NOT NULL CHECK (day_of_week IN ('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY')),
    start_time        TIME        NOT NULL,
    end_time          TIME        NOT NULL,
    valid_from        DATE,
    valid_to          DATE,
    CONSTRAINT fk_wt_section_subject FOREIGN KEY (section_subject_id) REFERENCES section_subjects(id),
    CONSTRAINT chk_wt_time CHECK (start_time < end_time)
);

-- University holidays
CREATE TABLE IF NOT EXISTS university_holidays (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    holiday_date    DATE         NOT NULL UNIQUE,
    description     VARCHAR(200)
);

-- Individual class session occurrences
CREATE TABLE IF NOT EXISTS class_sessions (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    section_subject_id  BIGINT      NOT NULL,
    section_id          BIGINT      NOT NULL,
    timetable_entry_id  BIGINT,
    session_date        DATE        NOT NULL,
    start_time          TIME        NOT NULL,
    end_time            TIME        NOT NULL,
    status              VARCHAR(15) NOT NULL DEFAULT 'SCHEDULED'
                        CHECK (status IN ('SCHEDULED','DRAFT','FINALIZED','CANCELLED')),
    created_by          BIGINT,
    finalized_by        BIGINT,
    created_at          TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    finalized_at        TIMESTAMP,
    cancellation_reason VARCHAR(500),
    CONSTRAINT fk_cs_section_subject FOREIGN KEY (section_subject_id) REFERENCES section_subjects(id),
    CONSTRAINT fk_cs_section         FOREIGN KEY (section_id)         REFERENCES sections(id),
    CONSTRAINT fk_cs_timetable       FOREIGN KEY (timetable_entry_id) REFERENCES weekly_timetable(id),
    CONSTRAINT fk_cs_created_by      FOREIGN KEY (created_by)         REFERENCES users(id),
    CONSTRAINT fk_cs_finalized_by    FOREIGN KEY (finalized_by)       REFERENCES users(id),
    CONSTRAINT chk_cs_time           CHECK (start_time < end_time),
    CONSTRAINT uq_class_session      UNIQUE (section_subject_id, session_date, start_time)
);

-- Individual student attendance per session
CREATE TABLE IF NOT EXISTS attendance_records (
    id                            BIGINT AUTO_INCREMENT PRIMARY KEY,
    class_session_id              BIGINT      NOT NULL,
    student_section_enrollment_id BIGINT      NOT NULL,
    student_id                    BIGINT      NOT NULL,
    section_id                    BIGINT      NOT NULL,
    attendance_status             VARCHAR(10) NOT NULL CHECK (attendance_status IN ('PRESENT','ABSENT')),
    marked_by                     BIGINT,
    marked_at                     TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ar_session     FOREIGN KEY (class_session_id)              REFERENCES class_sessions(id),
    CONSTRAINT fk_ar_enrollment  FOREIGN KEY (student_section_enrollment_id) REFERENCES student_section_enrollments(id),
    CONSTRAINT fk_ar_student     FOREIGN KEY (student_id)                    REFERENCES users(id),
    CONSTRAINT fk_ar_section     FOREIGN KEY (section_id)                    REFERENCES sections(id),
    CONSTRAINT fk_ar_marked_by   FOREIGN KEY (marked_by)                    REFERENCES users(id),
    CONSTRAINT uq_attendance     UNIQUE (class_session_id, student_section_enrollment_id)
);

-- Audit trail for attendance edits
CREATE TABLE IF NOT EXISTS attendance_audit_log (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    attendance_record_id  BIGINT      NOT NULL,
    previous_status       VARCHAR(10),
    new_status            VARCHAR(10),
    changed_by            BIGINT,
    changed_at            TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reason                VARCHAR(500),
    CONSTRAINT fk_aal_record    FOREIGN KEY (attendance_record_id) REFERENCES attendance_records(id),
    CONSTRAINT fk_aal_changed_by FOREIGN KEY (changed_by)          REFERENCES users(id)
);

-- ======================== INDEXES ========================
CREATE INDEX IF NOT EXISTS idx_cs_section_date   ON class_sessions(section_id, session_date);
CREATE INDEX IF NOT EXISTS idx_cs_status         ON class_sessions(status);
CREATE INDEX IF NOT EXISTS idx_cs_ss_id          ON class_sessions(section_subject_id);
CREATE INDEX IF NOT EXISTS idx_ar_student        ON attendance_records(student_id);
CREATE INDEX IF NOT EXISTS idx_ar_session        ON attendance_records(class_session_id);
CREATE INDEX IF NOT EXISTS idx_sse_student_status ON student_section_enrollments(student_id, status);
CREATE INDEX IF NOT EXISTS idx_sse_section       ON student_section_enrollments(section_id);
