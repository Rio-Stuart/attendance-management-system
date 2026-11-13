package com.attendance.repository.impl;

import com.attendance.config.DatabaseConfig;
import com.attendance.exception.DataAccessException;
import com.attendance.model.UniversityHoliday;
import com.attendance.repository.HolidayRepository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcHolidayRepository implements HolidayRepository {

    @Override
    public UniversityHoliday save(UniversityHoliday holiday) {
        String sql = "INSERT INTO university_holidays (holiday_date, description) VALUES (?, ?)";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDate(1, Date.valueOf(holiday.getHolidayDate()));
            ps.setString(2, holiday.getDescription());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) holiday.setId(keys.getLong(1));
            }
            return holiday;
        } catch (SQLException e) {
            throw new DataAccessException("Error saving holiday", e);
        }
    }

    @Override
    public Optional<UniversityHoliday> findById(long id) {
        String sql = "SELECT * FROM university_holidays WHERE id = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DataAccessException("Error finding holiday", e);
        }
    }

    @Override
    public boolean isHoliday(LocalDate date) {
        String sql = "SELECT COUNT(*) FROM university_holidays WHERE holiday_date = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            throw new DataAccessException("Error checking holiday", e);
        }
    }

    @Override
    public List<UniversityHoliday> findByDateRange(LocalDate from, LocalDate to) {
        String sql = "SELECT * FROM university_holidays WHERE holiday_date >= ? AND holiday_date <= ? ORDER BY holiday_date";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            return extractList(ps);
        } catch (SQLException e) {
            throw new DataAccessException("Error finding holidays by range", e);
        }
    }

    @Override
    public List<UniversityHoliday> findAll() {
        String sql = "SELECT * FROM university_holidays ORDER BY holiday_date";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            return extractList(ps);
        } catch (SQLException e) {
            throw new DataAccessException("Error finding all holidays", e);
        }
    }

    @Override
    public void delete(long id) {
        String sql = "DELETE FROM university_holidays WHERE id = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting holiday", e);
        }
    }

    private List<UniversityHoliday> extractList(PreparedStatement ps) throws SQLException {
        List<UniversityHoliday> results = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) results.add(mapRow(rs));
        }
        return results;
    }

    private UniversityHoliday mapRow(ResultSet rs) throws SQLException {
        UniversityHoliday h = new UniversityHoliday();
        h.setId(rs.getLong("id"));
        h.setHolidayDate(rs.getDate("holiday_date").toLocalDate());
        h.setDescription(rs.getString("description"));
        return h;
    }
}
