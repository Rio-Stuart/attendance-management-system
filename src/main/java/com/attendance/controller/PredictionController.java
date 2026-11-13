package com.attendance.controller;

import com.attendance.dto.ApiResponse;
import com.attendance.service.AttendancePredictionService;

import java.util.Map;

public class PredictionController {

    private final AttendancePredictionService predictionService;

    public PredictionController(AttendancePredictionService predictionService) {
        this.predictionService = predictionService;
    }

    public ApiResponse predictAttendance(long studentId, long sectionSubjectId, int daysAhead) {
        try {
            Map<String, Object> prediction = predictionService.predictAttendance(
                    studentId, sectionSubjectId, daysAhead);
            return ApiResponse.ok("Attendance prediction", prediction);
        } catch (Exception e) {
            return ApiResponse.error("Failed to predict: " + e.getMessage());
        }
    }

    public ApiResponse predictWithScenario(long studentId, long sectionSubjectId,
                                            int daysAhead, int toAttend) {
        try {
            Map<String, Object> prediction = predictionService.predictWithScenario(
                    studentId, sectionSubjectId, daysAhead, toAttend);
            return ApiResponse.ok("Attendance prediction (scenario)", prediction);
        } catch (Exception e) {
            return ApiResponse.error("Failed to predict: " + e.getMessage());
        }
    }
}
