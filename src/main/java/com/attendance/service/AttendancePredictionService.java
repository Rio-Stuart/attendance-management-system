package com.attendance.service;

import java.util.Map;

public interface AttendancePredictionService {

    Map<String, Object> predictAttendance(long studentId, long sectionSubjectId, int daysAhead);

    Map<String, Object> predictWithScenario(long studentId, long sectionSubjectId,
                                            int daysAhead, int sessionsToAttend);
}
