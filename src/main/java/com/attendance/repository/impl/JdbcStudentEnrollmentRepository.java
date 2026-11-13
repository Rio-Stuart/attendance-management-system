package com.attendance.repository.impl;

import com.attendance.config.DatabaseConfig;
import com.attendance.exception.DataAccessException;
import com.attendance.exception.EntityNotFoundException;
import com.attendance.model.StudentSectionEnrollment;
import com.attendance.model.enums.EnrollmentStatus;
import com.attendance.repository.StudentEnrollmentRepository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcStudentEnrollmentRepository implements StudentEnrollmentRepository {

    @Override
    public StudentSectionEnrollment save(StudentSectionEnrollment enrollment) {
        String sql = "INSERT INTO student_section_enrollments (student_id, section_id, academic_year, " +
                "start_date, end_date, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, enrollment.getStudentId());
            ps.setLong(2, enrollment.getSectionId());
            ps.setString(3, enrollment.getAcademicYear());
            ps.setDate(4, Date.valueOf(enrollment.getStartDate()));
            if (enrollment.getEndDate() != null) {
                ps.setDate(5, Date.valueOf(enrollment.getEndDate()));
            } else {
                ps.setNull(5, Types.DATE);
            }
            ps.setString(6, enrollment.getStatus().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) enrollment.setId(keys.getLong(1));
            }
            return enrollment;
        } catch (SQLException e) {
            throw new DataAccessException("Error saving enrollment", e);
        }
    }

    @Override
    public Optional<StudentSectionEnrollment> findById(long id) {
        String sql = "SELECT * FROM student_section_enrollments WHERE id = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DataAccessException("Error finding enrollment", e);
        }
    }

    @Override
    public List<StudentSectionEnrollment> findByStudentId(long studentId) {
        String sql = "SELECT * FROM student_section_enrollments WHERE student_id = ? ORDER BY start_date DESC";
        return queryByLong(sql, studentId);
    }

    @Override
    public Optional<StudentSectionEnrollment> findActiveByStudentId(long studentId) {
        String sql = "SELECT * FROM student_section_enrollments WHERE student_id = ? AND status = 'ACTIVE' ORDER BY start_date DESC";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DataAccessException("Error finding active enrollment", e);
        }
    }

    @Override
    public List<StudentSectionEnrollment> findBySectionId(long sectionId) {
        String sql = "SELECT * FROM student_section_enrollments WHERE section_id = ?";
        return queryByLong(sql, sectionId);
    }

    @Override
    public List<StudentSectionEnrollment> findActiveEnrollmentsBySectionIdOnDate(long sectionId, LocalDate date) {
        String sql = "SELECT * FROM student_section_enrollments WHERE section_id = ? " +
                "AND start_date <= ? AND (end_date IS NULL OR end_date >= ?) AND status = 'ACTIVE'";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, sectionId);
            ps.setDate(2, Date.valueOf(date));
            ps.setDate(3, Date.valueOf(date));
            return extractList(ps);
        } catch (SQLException e) {
            throw new DataAccessException("Error finding active enrollments by section and date", e);
        }
    }

    @Override
    public StudentSectionEnrollment deactivate(long id, LocalDate endDate) {
        String sql = "UPDATE student_section_enrollments SET status = 'INACTIVE', end_date = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(endDate));
            ps.setLong(2, id);
            int affected = ps.executeUpdate();
            if (affected == 0) throw new EntityNotFoundException("StudentSectionEnrollment", id);
        } catch (SQLException e) {
            throw new DataAccessException("Error deactivating enrollment", e);
        }
        return findById(id).orElseThrow(() -> new EntityNotFoundException("StudentSectionEnrollment", id));
    }

    @Override
    public boolean isStudentEnrolledInSectionOnDate(long studentId, long sectionId, LocalDate date) {
        String sql = "SELECT COUNT(*) FROM student_section_enrollments WHERE student_id = ? " +
                "AND section_id = ? AND start_date <= ? AND (end_date IS NULL OR end_date >= ?) AND status = 'ACTIVE'";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, studentId);
            ps.setLong(2, sectionId);
            ps.setDate(3, Date.valueOf(date));
            ps.setDate(4, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            throw new DataAccessException("Error checking enrollment", e);
        }
    }

    private List<StudentSectionEnrollment> queryByLong(String sql, long param) {
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, param);
            return extractList(ps);
        } catch (SQLException e) {
            throw new DataAccessException("Error querying enrollments", e);
        }
    }

    private List<StudentSectionEnrollment> extractList(PreparedStatement ps) throws SQLException {
        List<StudentSectionEnrollment> results = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) results.add(mapRow(rs));
        }
        return results;
    }

    private StudentSectionEnrollment mapRow(ResultSet rs) throws SQLException {
        StudentSectionEnrollment e = new StudentSectionEnrollment();
        e.setId(rs.getLong("id"));
        e.setStudentId(rs.getLong("student_id"));
        e.setSectionId(rs.getLong("section_id"));
        e.setAcademicYear(rs.getString("academic_year"));
        e.setStartDate(rs.getDate("start_date").toLocalDate());
        Date endDate = rs.getDate("end_date");
        e.setEndDate(endDate != null ? endDate.toLocalDate() : null);
        e.setStatus(EnrollmentStatus.valueOf(rs.getString("status")));
        Timestamp createdAt = rs.getTimestamp("created_at");
        e.setCreatedAt(createdAt != null ? createdAt.toLocalDateTime() : null);
        return e;
    }
}
