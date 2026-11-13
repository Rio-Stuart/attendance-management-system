package com.attendance.repository;

import com.attendance.model.UniversityHoliday;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HolidayRepository {

    UniversityHoliday save(UniversityHoliday holiday);

    Optional<UniversityHoliday> findById(long id);

    List<UniversityHoliday> findAll();

    boolean isHoliday(LocalDate date);

    List<UniversityHoliday> findByDateRange(LocalDate startDate, LocalDate endDate);

    void delete(long id);
}
