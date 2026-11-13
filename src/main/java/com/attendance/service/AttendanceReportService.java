package com.attendance.service;

import java.util.List;
import java.util.Map;

public interface AttendanceReportService {

    Map<String, Object> getStudentDashboard(long studentId);

    List<Map<String, Object>> getSectionReport(long sectionId);

    List<Map<String, Object>> getSubjectReport(long sectionSubjectId);
}
