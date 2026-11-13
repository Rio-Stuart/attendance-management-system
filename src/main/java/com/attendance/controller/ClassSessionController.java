package com.attendance.controller;

import com.attendance.dto.ApiResponse;
import com.attendance.dto.SessionCreateRequest;
import com.attendance.model.ClassSession;
import com.attendance.service.ClassSessionService;
import com.attendance.util.DateTimeUtil;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class ClassSessionController {

    private final ClassSessionService classSessionService;

    public ClassSessionController(ClassSessionService classSessionService) {
        this.classSessionService = classSessionService;
    }

    public ApiResponse createSession(SessionCreateRequest request, long adminId) {
        try {
            LocalDate date = DateTimeUtil.parseDate(request.getDate());
            LocalTime start = DateTimeUtil.parseTime(request.getStartTime());
            LocalTime end = DateTimeUtil.parseTime(request.getEndTime());

            ClassSession session = classSessionService.createSession(
                    request.getSectionSubjectId(), request.getSectionId(),
                    date, start, end, request.getTimetableEntryId(), adminId);

            return ApiResponse.ok("Class session created successfully", session);
        } catch (Exception e) {
            return ApiResponse.error("Failed to create session: " + e.getMessage());
        }
    }

    public ApiResponse getSessionsBySection(long sectionId) {
        try {
            // Uses findBySectionSubjectId transitively - for section-level we list all
            List<ClassSession> sessions = classSessionService.findBySectionSubjectId(sectionId);
            return ApiResponse.ok("Sessions retrieved", sessions);
        } catch (Exception e) {
            return ApiResponse.error("Failed to get sessions: " + e.getMessage());
        }
    }

    public ApiResponse getSessionDetails(long sessionId) {
        try {
            ClassSession session = classSessionService.findById(sessionId);
            return ApiResponse.ok("Session details", session);
        } catch (Exception e) {
            return ApiResponse.error("Session not found: " + e.getMessage());
        }
    }

    public ApiResponse cancelSession(long sessionId, String reason) {
        try {
            ClassSession session = classSessionService.cancelSession(sessionId, reason);
            return ApiResponse.ok("Session cancelled successfully", session);
        } catch (Exception e) {
            return ApiResponse.error("Failed to cancel session: " + e.getMessage());
        }
    }
}
