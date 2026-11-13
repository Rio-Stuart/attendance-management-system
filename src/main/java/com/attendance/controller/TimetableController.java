package com.attendance.controller;

import com.attendance.dto.ApiResponse;
import com.attendance.model.ClassSession;
import com.attendance.model.WeeklyTimetable;
import com.attendance.repository.TimetableRepository;
import com.attendance.service.TimetableGenerationService;

import java.util.List;

public class TimetableController {

    private final TimetableGenerationService timetableService;
    private final TimetableRepository timetableRepository;

    public TimetableController(TimetableGenerationService timetableService,
                               TimetableRepository timetableRepository) {
        this.timetableService = timetableService;
        this.timetableRepository = timetableRepository;
    }

    public ApiResponse generateSessions(long sectionId, int daysAhead) {
        try {
            List<ClassSession> generated = timetableService.generateSessionsForSection(sectionId, daysAhead);
            return ApiResponse.ok("Generated " + generated.size() + " sessions", generated);
        } catch (Exception e) {
            return ApiResponse.error("Failed to generate sessions: " + e.getMessage());
        }
    }

    public ApiResponse getUpcomingSessions(long sectionId) {
        try {
            List<ClassSession> sessions = timetableService.generateSessionsForSection(sectionId, 0);
            return ApiResponse.ok("Upcoming sessions", sessions);
        } catch (Exception e) {
            return ApiResponse.error("Failed to get upcoming sessions: " + e.getMessage());
        }
    }

    public ApiResponse getTimetable(long sectionId) {
        try {
            List<WeeklyTimetable> entries = timetableRepository.findBySectionId(sectionId);
            return ApiResponse.ok("Weekly timetable", entries);
        } catch (Exception e) {
            return ApiResponse.error("Failed to get timetable: " + e.getMessage());
        }
    }
}
