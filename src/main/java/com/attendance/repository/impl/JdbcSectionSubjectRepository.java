package com.attendance.repository.impl;

import com.attendance.config.DatabaseConfig;
import com.attendance.exception.DataAccessException;
import com.attendance.model.SectionSubject;
import com.attendance.repository.SectionSubjectRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcSectionSubjectRepository implements SectionSubjectRepository {

    @Override
    public SectionSubject save(SectionSubject ss) {
        String sql = "INSERT INTO section_subjects (section_id, subject_id) VALUES (?, ?)";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, ss.getSectionId());
            ps.setLong(2, ss.getSubjectId());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) ss.setId(keys.getLong(1));
            }
            return ss;
        } catch (SQLException e) {
            throw new DataAccessException("Error saving section-subject", e);
        }
    }

    @Override
    public Optional<SectionSubject> findById(long id) {
        String sql = "SELECT * FROM section_subjects WHERE id = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DataAccessException("Error finding section-subject", e);
        }
    }

    @Override
    public List<SectionSubject> findBySectionId(long sectionId) {
        return queryByLong("SELECT * FROM section_subjects WHERE section_id = ?", sectionId);
    }

    @Override
    public List<SectionSubject> findBySubjectId(long subjectId) {
        return queryByLong("SELECT * FROM section_subjects WHERE subject_id = ?", subjectId);
    }

    @Override
    public Optional<SectionSubject> findBySectionAndSubject(long sectionId, long subjectId) {
        String sql = "SELECT * FROM section_subjects WHERE section_id = ? AND subject_id = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, sectionId);
            ps.setLong(2, subjectId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DataAccessException("Error finding section-subject", e);
        }
    }

    @Override
    public List<SectionSubject> findAll() {
        String sql = "SELECT * FROM section_subjects";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            return extractList(ps);
        } catch (SQLException e) {
            throw new DataAccessException("Error finding all section-subjects", e);
        }
    }

    @Override
    public void delete(long id) {
        String sql = "DELETE FROM section_subjects WHERE id = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting section-subject", e);
        }
    }

    private List<SectionSubject> queryByLong(String sql, long param) {
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, param);
            return extractList(ps);
        } catch (SQLException e) {
            throw new DataAccessException("Error querying section-subjects", e);
        }
    }

    private List<SectionSubject> extractList(PreparedStatement ps) throws SQLException {
        List<SectionSubject> results = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) results.add(mapRow(rs));
        }
        return results;
    }

    private SectionSubject mapRow(ResultSet rs) throws SQLException {
        SectionSubject ss = new SectionSubject();
        ss.setId(rs.getLong("id"));
        ss.setSectionId(rs.getLong("section_id"));
        ss.setSubjectId(rs.getLong("subject_id"));
        return ss;
    }
}
