package com.attendance.repository.impl;

import com.attendance.config.DatabaseConfig;
import com.attendance.exception.DataAccessException;
import com.attendance.exception.EntityNotFoundException;
import com.attendance.model.WeeklyTimetable;
import com.attendance.repository.TimetableRepository;

import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcTimetableRepository implements TimetableRepository {

    @Override
    public WeeklyTimetable save(WeeklyTimetable entry) {
        String sql = "INSERT INTO weekly_timetable (section_subject_id, day_of_week, start_time, end_time, valid_from, valid_to) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, entry.getSectionSubjectId());
            ps.setString(2, entry.getDayOfWeek().name());
            ps.setTime(3, Time.valueOf(entry.getStartTime()));
            ps.setTime(4, Time.valueOf(entry.getEndTime()));
            if (entry.getValidFrom() != null) {
                ps.setDate(5, Date.valueOf(entry.getValidFrom()));
            } else {
                ps.setNull(5, Types.DATE);
            }
            if (entry.getValidTo() != null) {
                ps.setDate(6, Date.valueOf(entry.getValidTo()));
            } else {
                ps.setNull(6, Types.DATE);
            }
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) entry.setId(keys.getLong(1));
            }
            return entry;
        } catch (SQLException e) {
            throw new DataAccessException("Error saving timetable entry", e);
        }
    }

    @Override
    public Optional<WeeklyTimetable> findById(long id) {
        String sql = "SELECT * FROM weekly_timetable WHERE id = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DataAccessException("Error finding timetable entry", e);
        }
    }

    @Override
    public List<WeeklyTimetable> findBySectionSubjectId(long sectionSubjectId) {
        return queryByLong("SELECT * FROM weekly_timetable WHERE section_subject_id = ?", sectionSubjectId);
    }

    @Override
    public List<WeeklyTimetable> findBySectionId(long sectionId) {
        String sql = "SELECT wt.* FROM weekly_timetable wt JOIN section_subjects ss ON wt.section_subject_id = ss.id WHERE ss.section_id = ?";
        return queryByLong(sql, sectionId);
    }

    @Override
    public List<WeeklyTimetable> findByDayOfWeek(DayOfWeek day) {
        String sql = "SELECT * FROM weekly_timetable WHERE day_of_week = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, day.name());
            return extractList(ps);
        } catch (SQLException e) {
            throw new DataAccessException("Error finding timetable entries by day", e);
        }
    }

    @Override
    public List<WeeklyTimetable> findValidEntriesForDate(LocalDate date) {
        String sql = "SELECT * FROM weekly_timetable WHERE day_of_week = ? " +
                "AND (valid_from IS NULL OR valid_from <= ?) AND (valid_to IS NULL OR valid_to >= ?)";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, date.getDayOfWeek().name());
            ps.setDate(2, Date.valueOf(date));
            ps.setDate(3, Date.valueOf(date));
            return extractList(ps);
        } catch (SQLException e) {
            throw new DataAccessException("Error finding valid timetable entries for date", e);
        }
    }

    @Override
    public void delete(long id) {
        String sql = "DELETE FROM weekly_timetable WHERE id = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting timetable entry", e);
        }
    }

    private List<WeeklyTimetable> queryByLong(String sql, long param) {
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, param);
            return extractList(ps);
        } catch (SQLException e) {
            throw new DataAccessException("Error querying timetable", e);
        }
    }

    private List<WeeklyTimetable> extractList(PreparedStatement ps) throws SQLException {
        List<WeeklyTimetable> results = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) results.add(mapRow(rs));
        }
        return results;
    }

    private WeeklyTimetable mapRow(ResultSet rs) throws SQLException {
        WeeklyTimetable wt = new WeeklyTimetable();
        wt.setId(rs.getLong("id"));
        wt.setSectionSubjectId(rs.getLong("section_subject_id"));
        wt.setDayOfWeek(DayOfWeek.valueOf(rs.getString("day_of_week")));
        wt.setStartTime(rs.getTime("start_time").toLocalTime());
        wt.setEndTime(rs.getTime("end_time").toLocalTime());
        Date validFrom = rs.getDate("valid_from");
        wt.setValidFrom(validFrom != null ? validFrom.toLocalDate() : null);
        Date validTo = rs.getDate("valid_to");
        wt.setValidTo(validTo != null ? validTo.toLocalDate() : null);
        return wt;
    }
}
