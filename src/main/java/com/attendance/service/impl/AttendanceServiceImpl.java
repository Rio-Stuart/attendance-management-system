package com.attendance.service.impl;

import com.attendance.config.DatabaseConfig;
import com.attendance.exception.InvalidOperationException;
import com.attendance.exception.ResourceNotFoundException;
import com.attendance.exception.ValidationException;
import com.attendance.model.AttendanceAuditLog;
import com.attendance.model.AttendanceRecord;
import com.attendance.model.ClassSession;
import com.attendance.model.StudentSectionEnrollment;
import com.attendance.model.enums.AttendanceStatus;
import com.attendance.model.enums.SessionStatus;
import com.attendance.repository.AttendanceAuditRepository;
import com.attendance.repository.AttendanceRepository;
import com.attendance.repository.ClassSessionRepository;
import com.attendance.repository.StudentEnrollmentRepository;
import com.attendance.service.AttendanceService;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AttendanceServiceImpl implements AttendanceService {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    private final AttendanceRepository attendanceRepository;
    private final ClassSessionRepository classSessionRepository;
    private final StudentEnrollmentRepository enrollmentRepository;
    private final AttendanceAuditRepository auditRepository;

    public AttendanceServiceImpl(AttendanceRepository attendanceRepository,
                                 ClassSessionRepository classSessionRepository,
                                 StudentEnrollmentRepository enrollmentRepository,
                                 AttendanceAuditRepository auditRepository) {
        this.attendanceRepository = attendanceRepository;
        this.classSessionRepository = classSessionRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.auditRepository = auditRepository;
    }

    @Override
    public void markAttendance(long classSessionId, long studentId, long enrollmentId,
                               AttendanceStatus status, long markedBy) {
        // 1. Get session
        ClassSession session = classSessionRepository.findById(classSessionId)
                .orElseThrow(() -> new ResourceNotFoundException("ClassSession not found: " + classSessionId));

        // 2. Get enrollment
        StudentSectionEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found: " + enrollmentId));

        // 3. Validate enrollment section matches session section
        if (enrollment.getSectionId() != session.getSectionId()) {
            throw new ValidationException("Student's enrollment section (" + enrollment.getSectionId()
                    + ") does not match session section (" + session.getSectionId() + ")");
        }

        // 4. Validate enrollment was active on session date
        LocalDate sessionDate = session.getSessionDate();
        if (enrollment.getStartDate().isAfter(sessionDate)) {
            throw new ValidationException("Student enrollment started after the session date");
        }
        if (enrollment.getEndDate() != null && enrollment.getEndDate().isBefore(sessionDate)) {
            throw new ValidationException("Student enrollment ended before the session date");
        }

        // 5. Prevent duplicate
        if (attendanceRepository.existsBySessionAndEnrollment(classSessionId, enrollmentId)) {
            throw new InvalidOperationException("Attendance already recorded for this student and session");
        }

        // 6. Transition to DRAFT if SCHEDULED
        if (session.getStatus() == SessionStatus.SCHEDULED) {
            classSessionRepository.updateStatus(classSessionId, SessionStatus.DRAFT, null, null);
        } else if (session.getStatus() == SessionStatus.CANCELLED) {
            throw new InvalidOperationException("Cannot mark attendance for a CANCELLED session");
        }

        // 7. Save record
        AttendanceRecord record = new AttendanceRecord();
        record.setClassSessionId(classSessionId);
        record.setStudentSectionEnrollmentId(enrollmentId);
        record.setStudentId(studentId);
        record.setSectionId(session.getSectionId());
        record.setAttendanceStatus(status);
        record.setMarkedBy(markedBy);
        record.setMarkedAt(LocalDateTime.now(IST));
        record.setUpdatedAt(LocalDateTime.now(IST));

        attendanceRepository.save(record);
    }

    @Override
    public void markBulkAttendance(long classSessionId, Map<Long, AttendanceStatus> enrollmentStatusMap,
                                   long markedBy) {
        for (Map.Entry<Long, AttendanceStatus> entry : enrollmentStatusMap.entrySet()) {
            long enrollmentId = entry.getKey();
            StudentSectionEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found: " + enrollmentId));
            markAttendance(classSessionId, enrollment.getStudentId(), enrollmentId, entry.getValue(), markedBy);
        }
    }

    @Override
    public void saveDraft(long classSessionId, Map<Long, AttendanceStatus> enrollmentStatusMap,
                          long markedBy) {
        ClassSession session = classSessionRepository.findById(classSessionId)
                .orElseThrow(() -> new ResourceNotFoundException("ClassSession not found: " + classSessionId));

        if (session.getStatus() == SessionStatus.CANCELLED) {
            throw new InvalidOperationException("Cannot save draft for a CANCELLED session");
        }
        if (session.getStatus() == SessionStatus.FINALIZED) {
            throw new InvalidOperationException("Cannot save draft for a FINALIZED session");
        }

        // Save each record (skip duplicates by updating existing)
        for (Map.Entry<Long, AttendanceStatus> entry : enrollmentStatusMap.entrySet()) {
            long enrollmentId = entry.getKey();
            if (attendanceRepository.existsBySessionAndEnrollment(classSessionId, enrollmentId)) {
                // Update existing
                attendanceRepository.findBySessionAndEnrollment(classSessionId, enrollmentId)
                        .ifPresent(existing -> attendanceRepository.updateStatus(
                                existing.getId(), entry.getValue(), markedBy));
            } else {
                StudentSectionEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                        .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found: " + enrollmentId));
                markAttendance(classSessionId, enrollment.getStudentId(), enrollmentId, entry.getValue(), markedBy);
            }
        }

        // Transition to DRAFT
        if (session.getStatus() == SessionStatus.SCHEDULED) {
            classSessionRepository.updateStatus(classSessionId, SessionStatus.DRAFT, null, null);
        }
    }

    @Override
    public void finalizeAttendance(long classSessionId, long finalizedBy) {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            conn.setAutoCommit(false);

            // 1. Get session
            ClassSession session = classSessionRepository.findById(classSessionId)
                    .orElseThrow(() -> new ResourceNotFoundException("ClassSession not found: " + classSessionId));

            if (session.getStatus() == SessionStatus.FINALIZED) {
                throw new InvalidOperationException("Session is already FINALIZED");
            }
            if (session.getStatus() == SessionStatus.CANCELLED) {
                throw new InvalidOperationException("Cannot finalize a CANCELLED session");
            }

            // 2. Get eligible roster
            List<StudentSectionEnrollment> eligibleEnrollments =
                    enrollmentRepository.findActiveEnrollmentsBySectionIdOnDate(
                            session.getSectionId(), session.getSessionDate());

            if (eligibleEnrollments.isEmpty()) {
                throw new InvalidOperationException("No eligible students found for this session");
            }

            // 3. Get existing attendance records
            List<AttendanceRecord> existingRecords = attendanceRepository.findByClassSessionId(classSessionId);

            // 4. Verify EVERY eligible student has exactly one record
            Set<Long> eligibleEnrollmentIds = eligibleEnrollments.stream()
                    .map(StudentSectionEnrollment::getId)
                    .collect(Collectors.toSet());

            Set<Long> recordedEnrollmentIds = existingRecords.stream()
                    .map(AttendanceRecord::getStudentSectionEnrollmentId)
                    .collect(Collectors.toSet());

            // Check for missing students
            Set<Long> missingEnrollments = eligibleEnrollmentIds.stream()
                    .filter(id -> !recordedEnrollmentIds.contains(id))
                    .collect(Collectors.toSet());

            if (!missingEnrollments.isEmpty()) {
                throw new InvalidOperationException(
                        "Cannot finalize: " + missingEnrollments.size() +
                        " eligible students do not have attendance records");
            }

            // 5. Verify NO ineligible student has a record
            Set<Long> ineligibleRecords = recordedEnrollmentIds.stream()
                    .filter(id -> !eligibleEnrollmentIds.contains(id))
                    .collect(Collectors.toSet());

            if (!ineligibleRecords.isEmpty()) {
                throw new InvalidOperationException(
                        "Cannot finalize: " + ineligibleRecords.size() +
                        " attendance records belong to ineligible students");
            }

            // 6. Update session status
            LocalDateTime now = LocalDateTime.now(IST);
            classSessionRepository.updateStatus(classSessionId, SessionStatus.FINALIZED, finalizedBy, now);

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { /* ignore */ }
            }
            throw new InvalidOperationException("Database error during finalization: " + e.getMessage());
        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { /* ignore */ }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) { /* ignore */ }
            }
        }
    }

    @Override
    public void editFinalizedAttendance(long attendanceRecordId, AttendanceStatus newStatus,
                                        long changedBy, String reason) {
        // 1. Get record
        AttendanceRecord record = attendanceRepository.findById(attendanceRecordId)
                .orElseThrow(() -> new ResourceNotFoundException("AttendanceRecord not found: " + attendanceRecordId));

        // 2. Get session and verify it's FINALIZED
        ClassSession session = classSessionRepository.findById(record.getClassSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("ClassSession not found"));

        if (session.getStatus() != SessionStatus.FINALIZED) {
            throw new InvalidOperationException("Can only edit attendance for FINALIZED sessions");
        }

        // 3. Create audit log
        AttendanceAuditLog auditLog = new AttendanceAuditLog();
        auditLog.setAttendanceRecordId(attendanceRecordId);
        auditLog.setPreviousStatus(record.getAttendanceStatus().name());
        auditLog.setNewStatus(newStatus.name());
        auditLog.setChangedBy(changedBy);
        auditLog.setChangedAt(LocalDateTime.now(IST));
        auditLog.setReason(reason);
        auditRepository.save(auditLog);

        // 4. Update record
        attendanceRepository.updateStatus(attendanceRecordId, newStatus, changedBy);
    }

    @Override
    public List<AttendanceRecord> getSessionAttendance(long classSessionId) {
        return attendanceRepository.findByClassSessionId(classSessionId);
    }
}
