package com.attendance.controller;

import com.attendance.dto.ApiResponse;
import com.attendance.service.AttendanceCalculationService;
import com.attendance.service.AttendanceReportService;

import java.util.List;
import java.util.Map;

public class ReportController {

    private final AttendanceReportService reportService;
    private final AttendanceCalculationService calculationService;

    public ReportController(AttendanceReportService reportService,
                            AttendanceCalculationService calculationService) {
        this.reportService = reportService;
        this.calculationService = calculationService;
    }

    public ApiResponse getStudentDashboard(long studentId) {
        try {
            Map<String, Object> dashboard = reportService.getStudentDashboard(studentId);
            return ApiResponse.ok("Student dashboard", dashboard);
        } catch (Exception e) {
            return ApiResponse.error("Failed to get dashboard: " + e.getMessage());
        }
    }

    public ApiResponse getSectionReport(long sectionId) {
        try {
            List<Map<String, Object>> report = reportService.getSectionReport(sectionId);
            return ApiResponse.ok("Section report", report);
        } catch (Exception e) {
            return ApiResponse.error("Failed to get section report: " + e.getMessage());
        }
    }

    public ApiResponse getBelowThresholdReport(long sectionId, double threshold) {
        try {
            List<Map<String, Object>> report = calculationService.getBelowThresholdReport(sectionId, threshold);
            return ApiResponse.ok("Below " + threshold + "% report", report);
        } catch (Exception e) {
            return ApiResponse.error("Failed to get threshold report: " + e.getMessage());
        }
    }

    public ApiResponse getSubjectReport(long sectionSubjectId) {
        try {
            List<Map<String, Object>> report = reportService.getSubjectReport(sectionSubjectId);
            return ApiResponse.ok("Subject report", report);
        } catch (Exception e) {
            return ApiResponse.error("Failed to get subject report: " + e.getMessage());
        }
    }
}
