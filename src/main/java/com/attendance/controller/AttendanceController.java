package com.attendance.controller;

import com.attendance.dto.ApiResponse;
import com.attendance.dto.AttendanceMarkRequest;
import com.attendance.dto.BulkAttendanceRequest;
import com.attendance.model.AttendanceRecord;
import com.attendance.model.StudentSectionEnrollment;
import com.attendance.model.enums.AttendanceStatus;
import com.attendance.repository.StudentEnrollmentRepository;
import com.attendance.service.AttendanceService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttendanceController {

    private final AttendanceService attendanceService;
    private final StudentEnrollmentRepository enrollmentRepository;

    public AttendanceController(AttendanceService attendanceService,
                                StudentEnrollmentRepository enrollmentRepository) {
        this.attendanceService = attendanceService;
        this.enrollmentRepository = enrollmentRepository;
    }

    public ApiResponse loadRoster(long sessionId) {
        try {
            List<AttendanceRecord> existing = attendanceService.getSessionAttendance(sessionId);
            return ApiResponse.ok("Roster loaded", existing);
        } catch (Exception e) {
            return ApiResponse.error("Failed to load roster: " + e.getMessage());
        }
    }

    public ApiResponse markAttendance(AttendanceMarkRequest request, long adminId) {
        try {
            AttendanceStatus status = AttendanceStatus.valueOf(request.getStatus());
            attendanceService.markAttendance(request.getClassSessionId(),
                    request.getStudentId(), request.getEnrollmentId(), status, adminId);
            return ApiResponse.ok("Attendance marked successfully");
        } catch (Exception e) {
            return ApiResponse.error("Failed to mark attendance: " + e.getMessage());
        }
    }

    public ApiResponse markBulkAttendance(BulkAttendanceRequest request) {
        try {
            Map<Long, AttendanceStatus> enrollmentStatusMap = new HashMap<>();
            for (AttendanceMarkRequest entry : request.getAttendanceEntries()) {
                enrollmentStatusMap.put(entry.getEnrollmentId(),
                        AttendanceStatus.valueOf(entry.getStatus()));
            }
            attendanceService.markBulkAttendance(request.getClassSessionId(),
                    enrollmentStatusMap, request.getMarkedBy());
            return ApiResponse.ok("Bulk attendance marked successfully");
        } catch (Exception e) {
            return ApiResponse.error("Failed to mark bulk attendance: " + e.getMessage());
        }
    }

    public ApiResponse saveDraft(BulkAttendanceRequest request) {
        try {
            Map<Long, AttendanceStatus> enrollmentStatusMap = new HashMap<>();
            for (AttendanceMarkRequest entry : request.getAttendanceEntries()) {
                enrollmentStatusMap.put(entry.getEnrollmentId(),
                        AttendanceStatus.valueOf(entry.getStatus()));
            }
            attendanceService.saveDraft(request.getClassSessionId(),
                    enrollmentStatusMap, request.getMarkedBy());
            return ApiResponse.ok("Draft saved successfully");
        } catch (Exception e) {
            return ApiResponse.error("Failed to save draft: " + e.getMessage());
        }
    }

    public ApiResponse finalizeAttendance(long sessionId, long adminId) {
        try {
            attendanceService.finalizeAttendance(sessionId, adminId);
            return ApiResponse.ok("Attendance finalized successfully");
        } catch (Exception e) {
            return ApiResponse.error("Failed to finalize: " + e.getMessage());
        }
    }

    public ApiResponse editAttendance(long recordId, String newStatus, long adminId, String reason) {
        try {
            AttendanceStatus status = AttendanceStatus.valueOf(newStatus);
            attendanceService.editFinalizedAttendance(recordId, status, adminId, reason);
            return ApiResponse.ok("Attendance record updated successfully");
        } catch (Exception e) {
            return ApiResponse.error("Failed to edit attendance: " + e.getMessage());
        }
    }
}
