package com.attendance.repository;

import com.attendance.model.WeeklyTimetable;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TimetableRepository {

    WeeklyTimetable save(WeeklyTimetable entry);

    Optional<WeeklyTimetable> findById(long id);

    List<WeeklyTimetable> findBySectionSubjectId(long sectionSubjectId);

    List<WeeklyTimetable> findBySectionId(long sectionId);

    List<WeeklyTimetable> findByDayOfWeek(DayOfWeek day);

    List<WeeklyTimetable> findValidEntriesForDate(LocalDate date);

    void delete(long id);
}
