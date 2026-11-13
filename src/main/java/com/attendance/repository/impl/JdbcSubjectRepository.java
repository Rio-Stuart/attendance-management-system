package com.attendance.repository.impl;

import com.attendance.config.DatabaseConfig;
import com.attendance.exception.DataAccessException;
import com.attendance.model.Subject;
import com.attendance.repository.SubjectRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcSubjectRepository implements SubjectRepository {

    @Override
    public Subject save(Subject subject) {
        String sql = "INSERT INTO subjects (subject_code, subject_name) VALUES (?, ?)";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, subject.getSubjectCode());
            ps.setString(2, subject.getSubjectName());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) subject.setId(keys.getLong(1));
            }
            return subject;
        } catch (SQLException e) {
            throw new DataAccessException("Error saving subject", e);
        }
    }

    @Override
    public Optional<Subject> findById(long id) {
        String sql = "SELECT * FROM subjects WHERE id = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DataAccessException("Error finding subject", e);
        }
    }

    @Override
    public Optional<Subject> findBySubjectCode(String subjectCode) {
        String sql = "SELECT * FROM subjects WHERE subject_code = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, subjectCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DataAccessException("Error finding subject by code", e);
        }
    }

    public Optional<Subject> findByCode(String subjectCode) {
        return findBySubjectCode(subjectCode);
    }

    @Override
    public List<Subject> findAll() {
        String sql = "SELECT * FROM subjects ORDER BY subject_code";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            return extractList(ps);
        } catch (SQLException e) {
            throw new DataAccessException("Error finding all subjects", e);
        }
    }

    @Override
    public Subject update(Subject subject) {
        String sql = "UPDATE subjects SET subject_name = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, subject.getSubjectName());
            ps.setLong(2, subject.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error updating subject", e);
        }
        return findById(subject.getId()).orElse(subject);
    }

    @Override
    public void delete(long id) {
        String sql = "DELETE FROM subjects WHERE id = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting subject", e);
        }
    }

    private List<Subject> extractList(PreparedStatement ps) throws SQLException {
        List<Subject> results = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) results.add(mapRow(rs));
        }
        return results;
    }

    private Subject mapRow(ResultSet rs) throws SQLException {
        Subject s = new Subject();
        s.setId(rs.getLong("id"));
        s.setSubjectCode(rs.getString("subject_code"));
        s.setSubjectName(rs.getString("subject_name"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        s.setCreatedAt(createdAt != null ? createdAt.toLocalDateTime() : null);
        return s;
    }
}
