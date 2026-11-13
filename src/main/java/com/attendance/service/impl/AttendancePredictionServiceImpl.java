package com.attendance.service.impl;

import com.attendance.model.ClassSession;
import com.attendance.model.StudentSectionEnrollment;
import com.attendance.model.enums.SessionStatus;
import com.attendance.repository.ClassSessionRepository;
import com.attendance.repository.HolidayRepository;
import com.attendance.repository.StudentEnrollmentRepository;
import com.attendance.service.AttendanceCalculationService;
import com.attendance.service.AttendancePredictionService;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AttendancePredictionServiceImpl implements AttendancePredictionService {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    private final AttendanceCalculationService calculationService;
    private final ClassSessionRepository classSessionRepository;
    private final StudentEnrollmentRepository enrollmentRepository;
    private final HolidayRepository holidayRepository;

    public AttendancePredictionServiceImpl(AttendanceCalculationService calculationService,
                                           ClassSessionRepository classSessionRepository,
                                           StudentEnrollmentRepository enrollmentRepository,
                                           HolidayRepository holidayRepository) {
        this.calculationService = calculationService;
        this.classSessionRepository = classSessionRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.holidayRepository = holidayRepository;
    }

    @Override
    public Map<String, Object> predictAttendance(long studentId, long sectionSubjectId, int daysAhead) {
        // Current stats
        int P = calculationService.getClassesAttended(studentId, sectionSubjectId);
        int T = calculationService.getClassesConducted(studentId, sectionSubjectId);
        double currentPercentage = T > 0 ? ((double) P / T) * 100.0 : 0.0;

        // Get upcoming SCHEDULED sessions
        LocalDate today = LocalDate.now(IST);
        LocalDate endDate = today.plusDays(daysAhead);

        List<ClassSession> upcomingAll = classSessionRepository
                .findScheduledSessionsForPrediction(sectionSubjectId, today, endDate);

        // Filter out holidays and sessions outside enrollment period
        List<ClassSession> upcoming = upcomingAll.stream()
                .filter(s -> !holidayRepository.isHoliday(s.getSessionDate()))
                .filter(s -> enrollmentRepository.isStudentEnrolledInSectionOnDate(
                        studentId, s.getSectionId(), s.getSessionDate()))
                .collect(Collectors.toList());

        int K = upcoming.size();

        double predictedIfAllAttend = (T + K) > 0 ? ((double) (P + K) / (T + K)) * 100.0 : 0.0;
        double predictedIfAllAbsent = (T + K) > 0 ? ((double) P / (T + K)) * 100.0 : 0.0;

        Map<String, Object> result = new HashMap<>();
        result.put("currentConducted", T);
        result.put("currentAttended", P);
        result.put("currentPercentage", Math.round(currentPercentage * 100.0) / 100.0);
        result.put("upcomingSessions", K);
        result.put("predictedIfAllAttend", Math.round(predictedIfAllAttend * 100.0) / 100.0);
        result.put("predictedIfAllAbsent", Math.round(predictedIfAllAbsent * 100.0) / 100.0);
        return result;
    }

    @Override
    public Map<String, Object> predictWithScenario(long studentId, long sectionSubjectId,
                                                    int daysAhead, int sessionsToAttend) {
        Map<String, Object> base = predictAttendance(studentId, sectionSubjectId, daysAhead);

        int P = (int) base.get("currentAttended");
        int T = (int) base.get("currentConducted");
        int K = (int) base.get("upcomingSessions");
        int X = Math.min(sessionsToAttend, K);

        double predicted = (T + K) > 0 ? ((double) (P + X) / (T + K)) * 100.0 : 0.0;
        base.put("sessionsToAttend", X);
        base.put("predictedPercentage", Math.round(predicted * 100.0) / 100.0);
        return base;
    }
}
