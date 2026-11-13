package com.attendance.repository.impl;

import com.attendance.config.DatabaseConfig;
import com.attendance.exception.DataAccessException;
import com.attendance.exception.DuplicateEntityException;
import com.attendance.exception.EntityNotFoundException;
import com.attendance.model.ClassSession;
import com.attendance.model.enums.SessionStatus;
import com.attendance.repository.ClassSessionRepository;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcClassSessionRepository implements ClassSessionRepository {

    @Override
    public ClassSession save(ClassSession session) {
        String sql = "INSERT INTO class_sessions (section_subject_id, section_id, timetable_entry_id, " +
                "session_date, start_time, end_time, status, created_by, finalized_by, finalized_at, " +
                "cancellation_reason) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, session.getSectionSubjectId());
            ps.setLong(2, session.getSectionId());
            if (session.getTimetableEntryId() != null) {
                ps.setLong(3, session.getTimetableEntryId());
            } else {
                ps.setNull(3, Types.BIGINT);
            }
            ps.setDate(4, Date.valueOf(session.getSessionDate()));
            ps.setTime(5, Time.valueOf(session.getStartTime()));
            ps.setTime(6, Time.valueOf(session.getEndTime()));
            ps.setString(7, session.getStatus().name());
            if (session.getCreatedBy() != null) {
                ps.setLong(8, session.getCreatedBy());
            } else {
                ps.setNull(8, Types.BIGINT);
            }
            if (session.getFinalizedBy() != null) {
                ps.setLong(9, session.getFinalizedBy());
            } else {
                ps.setNull(9, Types.BIGINT);
            }
            if (session.getFinalizedAt() != null) {
                ps.setTimestamp(10, Timestamp.valueOf(session.getFinalizedAt()));
            } else {
                ps.setNull(10, Types.TIMESTAMP);
            }
            ps.setString(11, session.getCancellationReason());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new DataAccessException("Failed to save class session, no rows affected");
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    session.setId(keys.getLong(1));
                }
            }
            return session;
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new DuplicateEntityException("Class session already exists for the given section_subject_id, date, and start_time", e);
        } catch (SQLException e) {
            throw new DataAccessException("Error saving class session", e);
        }
    }

    @Override
    public Optional<ClassSession> findById(long id) {
        String sql = "SELECT * FROM class_sessions WHERE id = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DataAccessException("Error finding class session by id: " + id, e);
        }
    }

    @Override
    public List<ClassSession> findBySectionSubjectId(long sectionSubjectId) {
        String sql = "SELECT * FROM class_sessions WHERE section_subject_id = ? ORDER BY session_date, start_time";
        return queryList(sql, sectionSubjectId);
    }

    @Override
    public List<ClassSession> findBySectionId(long sectionId) {
        String sql = "SELECT * FROM class_sessions WHERE section_id = ? ORDER BY session_date, start_time";
        return queryList(sql, sectionId);
    }

    @Override
    public List<ClassSession> findBySectionIdAndStatus(long sectionId, SessionStatus status) {
        String sql = "SELECT * FROM class_sessions WHERE section_id = ? AND status = ? ORDER BY session_date, start_time";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, sectionId);
            ps.setString(2, status.name());
            return extractList(ps);
        } catch (SQLException e) {
            throw new DataAccessException("Error finding class sessions by section and status", e);
        }
    }

    @Override
    public List<ClassSession> findBySectionSubjectIdAndStatus(long sectionSubjectId, SessionStatus status) {
        String sql = "SELECT * FROM class_sessions WHERE section_subject_id = ? AND status = ? ORDER BY session_date, start_time";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, sectionSubjectId);
            ps.setString(2, status.name());
            return extractList(ps);
        } catch (SQLException e) {
            throw new DataAccessException("Error finding class sessions by section_subject and status", e);
        }
    }

    @Override
    public List<ClassSession> findBySectionSubjectIdAndDateRange(long sectionSubjectId, LocalDate start, LocalDate end) {
        String sql = "SELECT * FROM class_sessions WHERE section_subject_id = ? AND session_date >= ? AND session_date <= ? ORDER BY session_date, start_time";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, sectionSubjectId);
            ps.setDate(2, Date.valueOf(start));
            ps.setDate(3, Date.valueOf(end));
            return extractList(ps);
        } catch (SQLException e) {
            throw new DataAccessException("Error finding class sessions by date range", e);
        }
    }

    @Override
    public List<ClassSession> findScheduledSessionsForPrediction(long sectionSubjectId, LocalDate from, LocalDate to) {
        String sql = "SELECT * FROM class_sessions WHERE section_subject_id = ? AND status = 'SCHEDULED' " +
                "AND session_date >= ? AND session_date <= ? ORDER BY session_date, start_time";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, sectionSubjectId);
            ps.setDate(2, Date.valueOf(from));
            ps.setDate(3, Date.valueOf(to));
            return extractList(ps);
        } catch (SQLException e) {
            throw new DataAccessException("Error finding scheduled sessions for prediction", e);
        }
    }

    @Override
    public Optional<ClassSession> findByUnique(long sectionSubjectId, LocalDate date, LocalTime startTime) {
        String sql = "SELECT * FROM class_sessions WHERE section_subject_id = ? AND session_date = ? AND start_time = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, sectionSubjectId);
            ps.setDate(2, Date.valueOf(date));
            ps.setTime(3, Time.valueOf(startTime));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DataAccessException("Error finding class session by unique key", e);
        }
    }

    @Override
    public ClassSession updateStatus(long id, SessionStatus status, Long finalizedBy, LocalDateTime finalizedAt) {
        String sql = "UPDATE class_sessions SET status = ?, finalized_by = ?, finalized_at = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());
            if (finalizedBy != null) {
                ps.setLong(2, finalizedBy);
            } else {
                ps.setNull(2, Types.BIGINT);
            }
            if (finalizedAt != null) {
                ps.setTimestamp(3, Timestamp.valueOf(finalizedAt));
            } else {
                ps.setNull(3, Types.TIMESTAMP);
            }
            ps.setLong(4, id);

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new EntityNotFoundException("ClassSession", id);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error updating class session status", e);
        }
        return findById(id).orElseThrow(() -> new EntityNotFoundException("ClassSession", id));
    }

    @Override
    public int countFinalizedSessions(long sectionSubjectId) {
        String sql = "SELECT COUNT(*) FROM class_sessions WHERE section_subject_id = ? AND status = 'FINALIZED'";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, sectionSubjectId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return 0;
        } catch (SQLException e) {
            throw new DataAccessException("Error counting finalized sessions", e);
        }
    }

    @Override
    public boolean existsByUnique(long sectionSubjectId, LocalDate date, LocalTime startTime) {
        String sql = "SELECT COUNT(*) FROM class_sessions WHERE section_subject_id = ? AND session_date = ? AND start_time = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, sectionSubjectId);
            ps.setDate(2, Date.valueOf(date));
            ps.setTime(3, Time.valueOf(startTime));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            return false;
        } catch (SQLException e) {
            throw new DataAccessException("Error checking class session existence", e);
        }
    }

    @Override
    public List<ClassSession> findAll() {
        String sql = "SELECT * FROM class_sessions ORDER BY session_date, start_time";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            return extractList(ps);
        } catch (SQLException e) {
            throw new DataAccessException("Error finding all class sessions", e);
        }
    }

    // ---- Helper methods ----

    private List<ClassSession> queryList(String sql, long paramValue) {
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, paramValue);
            return extractList(ps);
        } catch (SQLException e) {
            throw new DataAccessException("Error querying class sessions", e);
        }
    }

    private List<ClassSession> extractList(PreparedStatement ps) throws SQLException {
        List<ClassSession> results = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                results.add(mapRow(rs));
            }
        }
        return results;
    }

    private ClassSession mapRow(ResultSet rs) throws SQLException {
        ClassSession session = new ClassSession();
        session.setId(rs.getLong("id"));
        session.setSectionSubjectId(rs.getLong("section_subject_id"));
        session.setSectionId(rs.getLong("section_id"));

        long timetableEntryId = rs.getLong("timetable_entry_id");
        session.setTimetableEntryId(rs.wasNull() ? null : timetableEntryId);

        session.setSessionDate(rs.getDate("session_date").toLocalDate());
        session.setStartTime(rs.getTime("start_time").toLocalTime());
        session.setEndTime(rs.getTime("end_time").toLocalTime());
        session.setStatus(SessionStatus.valueOf(rs.getString("status")));

        long createdBy = rs.getLong("created_by");
        session.setCreatedBy(rs.wasNull() ? null : createdBy);

        long finalizedBy = rs.getLong("finalized_by");
        session.setFinalizedBy(rs.wasNull() ? null : finalizedBy);

        Timestamp createdAt = rs.getTimestamp("created_at");
        session.setCreatedAt(createdAt != null ? createdAt.toLocalDateTime() : null);

        Timestamp finalizedAt = rs.getTimestamp("finalized_at");
        session.setFinalizedAt(finalizedAt != null ? finalizedAt.toLocalDateTime() : null);

        session.setCancellationReason(rs.getString("cancellation_reason"));

        return session;
    }
}
