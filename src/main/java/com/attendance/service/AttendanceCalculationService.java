package com.attendance.service;

import java.util.List;
import java.util.Map;

public interface AttendanceCalculationService {

    double calculateSubjectAttendance(long studentId, long sectionSubjectId);

    double calculateOverallAttendance(long studentId, long sectionId);

    int getClassesConducted(long studentId, long sectionSubjectId);

    int getClassesAttended(long studentId, long sectionSubjectId);

    int getClassesMissed(long studentId, long sectionSubjectId);

    List<Map<String, Object>> getBelowThresholdReport(long sectionId, double threshold);
}
