package com.attendance.repository.impl;

import com.attendance.config.DatabaseConfig;
import com.attendance.exception.DataAccessException;
import com.attendance.model.AttendanceAuditLog;
import com.attendance.repository.AttendanceAuditRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcAttendanceAuditRepository implements AttendanceAuditRepository {

    @Override
    public AttendanceAuditLog save(AttendanceAuditLog log) {
        String sql = "INSERT INTO attendance_audit_log (attendance_record_id, previous_status, new_status, changed_by, reason) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, log.getAttendanceRecordId());
            ps.setString(2, log.getPreviousStatus());
            ps.setString(3, log.getNewStatus());
            if (log.getChangedBy() != null) {
                ps.setLong(4, log.getChangedBy());
            } else {
                ps.setNull(4, Types.BIGINT);
            }
            ps.setString(5, log.getReason());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) log.setId(keys.getLong(1));
            }
            return log;
        } catch (SQLException e) {
            throw new DataAccessException("Error saving audit log", e);
        }
    }

    @Override
    public List<AttendanceAuditLog> findByRecordId(long attendanceRecordId) {
        String sql = "SELECT * FROM attendance_audit_log WHERE attendance_record_id = ? ORDER BY changed_at DESC";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, attendanceRecordId);
            List<AttendanceAuditLog> results = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) results.add(mapRow(rs));
            }
            return results;
        } catch (SQLException e) {
            throw new DataAccessException("Error finding audit logs", e);
        }
    }

    private AttendanceAuditLog mapRow(ResultSet rs) throws SQLException {
        AttendanceAuditLog log = new AttendanceAuditLog();
        log.setId(rs.getLong("id"));
        log.setAttendanceRecordId(rs.getLong("attendance_record_id"));
        log.setPreviousStatus(rs.getString("previous_status"));
        log.setNewStatus(rs.getString("new_status"));
        long changedBy = rs.getLong("changed_by");
        log.setChangedBy(rs.wasNull() ? null : changedBy);
        Timestamp changedAt = rs.getTimestamp("changed_at");
        log.setChangedAt(changedAt != null ? changedAt.toLocalDateTime() : null);
        log.setReason(rs.getString("reason"));
        return log;
    }
}
