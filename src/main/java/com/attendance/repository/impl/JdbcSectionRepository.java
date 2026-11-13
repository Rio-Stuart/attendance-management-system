package com.attendance.repository.impl;

import com.attendance.config.DatabaseConfig;
import com.attendance.exception.DataAccessException;
import com.attendance.exception.EntityNotFoundException;
import com.attendance.model.Section;
import com.attendance.repository.SectionRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcSectionRepository implements SectionRepository {

    @Override
    public Section save(Section section) {
        String sql = "INSERT INTO sections (section_name, academic_year) VALUES (?, ?)";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, section.getSectionName());
            ps.setString(2, section.getAcademicYear());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) section.setId(keys.getLong(1));
            }
            return section;
        } catch (SQLException e) {
            throw new DataAccessException("Error saving section", e);
        }
    }

    @Override
    public Optional<Section> findById(long id) {
        String sql = "SELECT * FROM sections WHERE id = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DataAccessException("Error finding section", e);
        }
    }

    @Override
    public Optional<Section> findBySectionName(String sectionName) {
        String sql = "SELECT * FROM sections WHERE section_name = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sectionName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DataAccessException("Error finding section by name", e);
        }
    }

    public Optional<Section> findByName(String sectionName) {
        return findBySectionName(sectionName);
    }

    @Override
    public Section update(Section section) {
        String sql = "UPDATE sections SET section_name = ?, academic_year = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, section.getSectionName());
            ps.setString(2, section.getAcademicYear());
            ps.setLong(3, section.getId());
            int affected = ps.executeUpdate();
            if (affected == 0) throw new EntityNotFoundException("Section", section.getId());
        } catch (SQLException e) {
            throw new DataAccessException("Error updating section", e);
        }
        return findById(section.getId()).orElse(section);
    }

    @Override
    public List<Section> findAll() {
        String sql = "SELECT * FROM sections ORDER BY section_name";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            return extractList(ps);
        } catch (SQLException e) {
            throw new DataAccessException("Error finding all sections", e);
        }
    }

    @Override
    public List<Section> findByAcademicYear(String academicYear) {
        String sql = "SELECT * FROM sections WHERE academic_year = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, academicYear);
            return extractList(ps);
        } catch (SQLException e) {
            throw new DataAccessException("Error finding sections by year", e);
        }
    }

    @Override
    public void delete(long id) {
        String sql = "DELETE FROM sections WHERE id = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting section", e);
        }
    }

    private List<Section> extractList(PreparedStatement ps) throws SQLException {
        List<Section> results = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) results.add(mapRow(rs));
        }
        return results;
    }

    private Section mapRow(ResultSet rs) throws SQLException {
        Section s = new Section();
        s.setId(rs.getLong("id"));
        s.setSectionName(rs.getString("section_name"));
        s.setAcademicYear(rs.getString("academic_year"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        s.setCreatedAt(createdAt != null ? createdAt.toLocalDateTime() : null);
        return s;
    }
}
