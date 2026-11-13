package com.attendance.repository.impl;

import com.attendance.config.DatabaseConfig;
import com.attendance.exception.DataAccessException;
import com.attendance.exception.DuplicateEntityException;
import com.attendance.exception.EntityNotFoundException;
import com.attendance.model.AttendanceRecord;
import com.attendance.model.enums.AttendanceStatus;
import com.attendance.repository.AttendanceRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcAttendanceRepository implements AttendanceRepository {

    @Override
    public AttendanceRecord save(AttendanceRecord record) {
        String sql = "INSERT INTO attendance_records (class_session_id, student_section_enrollment_id, " +
                "student_id, section_id, attendance_status, marked_by) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, record.getClassSessionId());
            ps.setLong(2, record.getStudentSectionEnrollmentId());
            ps.setLong(3, record.getStudentId());
            ps.setLong(4, record.getSectionId());
            ps.setString(5, record.getAttendanceStatus().name());
            if (record.getMarkedBy() != null) {
                ps.setLong(6, record.getMarkedBy());
            } else {
                ps.setNull(6, Types.BIGINT);
            }

            int affected = ps.executeUpdate();
            if (affected == 0) throw new DataAccessException("Failed to save attendance record");

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) record.setId(keys.getLong(1));
            }
            return record;
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new DuplicateEntityException("Attendance record already exists for this session and enrollment", e);
        } catch (SQLException e) {
            throw new DataAccessException("Error saving attendance record", e);
        }
    }

    @Override
    public Optional<AttendanceRecord> findById(long id) {
        String sql = "SELECT * FROM attendance_records WHERE id = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DataAccessException("Error finding attendance record", e);
        }
    }

    @Override
    public List<AttendanceRecord> findByClassSessionId(long classSessionId) {
        String sql = "SELECT * FROM attendance_records WHERE class_session_id = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, classSessionId);
            return extractList(ps);
        } catch (SQLException e) {
            throw new DataAccessException("Error finding attendance records by session", e);
        }
    }

    @Override
    public Optional<AttendanceRecord> findBySessionAndEnrollment(long classSessionId, long enrollmentId) {
        String sql = "SELECT * FROM attendance_records WHERE class_session_id = ? AND student_section_enrollment_id = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, classSessionId);
            ps.setLong(2, enrollmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DataAccessException("Error finding attendance record", e);
        }
    }

    @Override
    public int countPresentByStudentAndSubject(long studentId, long sectionSubjectId) {
        String sql = "SELECT COUNT(*) FROM attendance_records ar " +
                "JOIN class_sessions cs ON ar.class_session_id = cs.id " +
                "JOIN student_section_enrollments sse ON ar.student_section_enrollment_id = sse.id " +
                "WHERE ar.student_id = ? AND cs.section_subject_id = ? AND cs.status = 'FINALIZED' " +
                "AND ar.attendance_status = 'PRESENT' " +
                "AND cs.session_date >= sse.start_date " +
                "AND (sse.end_date IS NULL OR cs.session_date <= sse.end_date)";
        return countQuery(sql, studentId, sectionSubjectId);
    }

    @Override
    public int countByStudentAndSubject(long studentId, long sectionSubjectId) {
        String sql = "SELECT COUNT(*) FROM attendance_records ar " +
                "JOIN class_sessions cs ON ar.class_session_id = cs.id " +
                "JOIN student_section_enrollments sse ON ar.student_section_enrollment_id = sse.id " +
                "WHERE ar.student_id = ? AND cs.section_subject_id = ? AND cs.status = 'FINALIZED' " +
                "AND cs.session_date >= sse.start_date " +
                "AND (sse.end_date IS NULL OR cs.session_date <= sse.end_date)";
        return countQuery(sql, studentId, sectionSubjectId);
    }

    private int countQuery(String sql, long studentId, long sectionSubjectId) {
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, studentId);
            ps.setLong(2, sectionSubjectId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new DataAccessException("Error counting attendance records", e);
        }
    }

    @Override
    public AttendanceRecord updateStatus(long id, AttendanceStatus status, long updatedBy) {
        String sql = "UPDATE attendance_records SET attendance_status = ?, marked_by = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setLong(2, updatedBy);
            ps.setLong(3, id);
            int affected = ps.executeUpdate();
            if (affected == 0) throw new EntityNotFoundException("AttendanceRecord", id);
        } catch (SQLException e) {
            throw new DataAccessException("Error updating attendance status", e);
        }
        return findById(id).orElseThrow(() -> new EntityNotFoundException("AttendanceRecord", id));
    }

    @Override
    public void saveAll(List<AttendanceRecord> records) {
        String sql = "INSERT INTO attendance_records (class_session_id, student_section_enrollment_id, " +
                "student_id, section_id, attendance_status, marked_by) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (AttendanceRecord r : records) {
                ps.setLong(1, r.getClassSessionId());
                ps.setLong(2, r.getStudentSectionEnrollmentId());
                ps.setLong(3, r.getStudentId());
                ps.setLong(4, r.getSectionId());
                ps.setString(5, r.getAttendanceStatus().name());
                if (r.getMarkedBy() != null) {
                    ps.setLong(6, r.getMarkedBy());
                } else {
                    ps.setNull(6, Types.BIGINT);
                }
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw new DataAccessException("Error batch saving attendance records", e);
        }
    }

    @Override
    public boolean existsBySessionAndEnrollment(long classSessionId, long enrollmentId) {
        String sql = "SELECT COUNT(*) FROM attendance_records WHERE class_session_id = ? AND student_section_enrollment_id = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, classSessionId);
            ps.setLong(2, enrollmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            throw new DataAccessException("Error checking attendance existence", e);
        }
    }

    @Override
    public List<AttendanceRecord> findByStudentId(long studentId) {
        String sql = "SELECT * FROM attendance_records WHERE student_id = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, studentId);
            return extractList(ps);
        } catch (SQLException e) {
            throw new DataAccessException("Error finding attendance by student", e);
        }
    }

    @Override
    public void deleteByClassSessionId(long classSessionId) {
        String sql = "DELETE FROM attendance_records WHERE class_session_id = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, classSessionId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting attendance records", e);
        }
    }

    private List<AttendanceRecord> extractList(PreparedStatement ps) throws SQLException {
        List<AttendanceRecord> results = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) results.add(mapRow(rs));
        }
        return results;
    }

    private AttendanceRecord mapRow(ResultSet rs) throws SQLException {
        AttendanceRecord r = new AttendanceRecord();
        r.setId(rs.getLong("id"));
        r.setClassSessionId(rs.getLong("class_session_id"));
        r.setStudentSectionEnrollmentId(rs.getLong("student_section_enrollment_id"));
        r.setStudentId(rs.getLong("student_id"));
        r.setSectionId(rs.getLong("section_id"));
        r.setAttendanceStatus(AttendanceStatus.valueOf(rs.getString("attendance_status")));
        long markedBy = rs.getLong("marked_by");
        r.setMarkedBy(rs.wasNull() ? null : markedBy);
        Timestamp markedAt = rs.getTimestamp("marked_at");
        r.setMarkedAt(markedAt != null ? markedAt.toLocalDateTime() : null);
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        r.setUpdatedAt(updatedAt != null ? updatedAt.toLocalDateTime() : null);
        return r;
    }
}
