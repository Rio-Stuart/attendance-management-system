package com.attendance.service;

import com.attendance.model.ClassSession;

import java.time.LocalDate;
import java.util.List;

public interface TimetableGenerationService {

    List<ClassSession> generateSessionsForDateRange(long sectionId, LocalDate from, LocalDate to);

    List<ClassSession> generateSessionsForSection(long sectionId, int daysAhead);

    boolean isHoliday(LocalDate date);
}
