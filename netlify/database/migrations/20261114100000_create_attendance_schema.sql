-- netlify/database/migrations/20261114100000_create_attendance_schema.sql
-- Complete PostgreSQL Schema for Netlify Database

-- 1. Users table (administrators and students)
CREATE TABLE IF NOT EXISTS users (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(50)  NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(100) NOT NULL,
    email           VARCHAR(100),
    role            VARCHAR(10)  NOT NULL CHECK (role IN ('ADMIN', 'STUDENT')),
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- 2. Sections
CREATE TABLE IF NOT EXISTS sections (
    id              BIGSERIAL PRIMARY KEY,
    section_name    VARCHAR(20)  NOT NULL UNIQUE,
    academic_year   VARCHAR(10)  NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- 3. Subjects
CREATE TABLE IF NOT EXISTS subjects (
    id              BIGSERIAL PRIMARY KEY,
    subject_code    VARCHAR(20)  NOT NULL UNIQUE,
    subject_name    VARCHAR(100) NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- 4. Section Subjects (many-to-many)
CREATE TABLE IF NOT EXISTS section_subjects (
    id              BIGSERIAL PRIMARY KEY,
    section_id      BIGINT NOT NULL REFERENCES sections(id) ON DELETE CASCADE,
    subject_id      BIGINT NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    CONSTRAINT uq_section_subject UNIQUE (section_id, subject_id)
);

-- 5. Student Section Enrollments (history tracking)
CREATE TABLE IF NOT EXISTS student_section_enrollments (
    id              BIGSERIAL PRIMARY KEY,
    student_id      BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    section_id      BIGINT       NOT NULL REFERENCES sections(id) ON DELETE CASCADE,
    academic_year   VARCHAR(10)  NOT NULL,
    start_date      DATE         NOT NULL,
    end_date        DATE,
    status          VARCHAR(10)  NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- 6. Weekly Timetable
CREATE TABLE IF NOT EXISTS weekly_timetable (
    id                 BIGSERIAL PRIMARY KEY,
    section_subject_id BIGINT      NOT NULL REFERENCES section_subjects(id) ON DELETE CASCADE,
    day_of_week        VARCHAR(10) NOT NULL CHECK (day_of_week IN ('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY')),
    start_time         TIME        NOT NULL,
    end_time           TIME        NOT NULL,
    valid_from         DATE,
    valid_to           DATE,
    CONSTRAINT chk_wt_time CHECK (start_time < end_time)
);

-- 7. University Holidays
CREATE TABLE IF NOT EXISTS university_holidays (
    id              BIGSERIAL PRIMARY KEY,
    holiday_date    DATE         NOT NULL UNIQUE,
    description     VARCHAR(200)
);

-- 8. Class Sessions
CREATE TABLE IF NOT EXISTS class_sessions (
    id                  BIGSERIAL PRIMARY KEY,
    section_subject_id  BIGINT      NOT NULL REFERENCES section_subjects(id) ON DELETE CASCADE,
    section_id          BIGINT      NOT NULL REFERENCES sections(id) ON DELETE CASCADE,
    timetable_entry_id  BIGINT      REFERENCES weekly_timetable(id) ON DELETE SET NULL,
    session_date        DATE        NOT NULL,
    start_time          TIME        NOT NULL,
    end_time            TIME        NOT NULL,
    status              VARCHAR(15) NOT NULL DEFAULT 'SCHEDULED'
                        CHECK (status IN ('SCHEDULED','DRAFT','FINALIZED','CANCELLED')),
    created_by          BIGINT      REFERENCES users(id) ON DELETE SET NULL,
    finalized_by        BIGINT      REFERENCES users(id) ON DELETE SET NULL,
    created_at          TIMESTAMP   NOT NULL DEFAULT NOW(),
    finalized_at        TIMESTAMP,
    cancellation_reason VARCHAR(500),
    CONSTRAINT chk_cs_time CHECK (start_time < end_time),
    CONSTRAINT uq_class_session UNIQUE (section_subject_id, session_date, start_time)
);

-- 9. Attendance Records
CREATE TABLE IF NOT EXISTS attendance_records (
    id                            BIGSERIAL PRIMARY KEY,
    class_session_id              BIGINT      NOT NULL REFERENCES class_sessions(id) ON DELETE CASCADE,
    student_section_enrollment_id BIGINT      NOT NULL REFERENCES student_section_enrollments(id) ON DELETE CASCADE,
    student_id                    BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    section_id                    BIGINT      NOT NULL REFERENCES sections(id) ON DELETE CASCADE,
    attendance_status             VARCHAR(10) NOT NULL CHECK (attendance_status IN ('PRESENT','ABSENT')),
    marked_by                     BIGINT      REFERENCES users(id) ON DELETE SET NULL,
    marked_at                     TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at                    TIMESTAMP   NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_attendance UNIQUE (class_session_id, student_section_enrollment_id)
);

-- 10. Attendance Audit Log
CREATE TABLE IF NOT EXISTS attendance_audit_log (
    id                    BIGSERIAL PRIMARY KEY,
    attendance_record_id  BIGINT      NOT NULL REFERENCES attendance_records(id) ON DELETE CASCADE,
    previous_status       VARCHAR(10),
    new_status            VARCHAR(10),
    changed_by            BIGINT      REFERENCES users(id) ON DELETE SET NULL,
    changed_at            TIMESTAMP   NOT NULL DEFAULT NOW(),
    reason                VARCHAR(500)
);

-- Indexes for high-performance querying
CREATE INDEX IF NOT EXISTS idx_cs_section_date   ON class_sessions(section_id, session_date);
CREATE INDEX IF NOT EXISTS idx_cs_status         ON class_sessions(status);
CREATE INDEX IF NOT EXISTS idx_cs_ss_id          ON class_sessions(section_subject_id);
CREATE INDEX IF NOT EXISTS idx_ar_student        ON attendance_records(student_id);
CREATE INDEX IF NOT EXISTS idx_ar_session        ON attendance_records(class_session_id);
CREATE INDEX IF NOT EXISTS idx_sse_student_status ON student_section_enrollments(student_id, status);
CREATE INDEX IF NOT EXISTS idx_sse_section       ON student_section_enrollments(section_id);
