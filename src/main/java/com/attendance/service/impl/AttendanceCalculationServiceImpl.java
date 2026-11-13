package com.attendance.service.impl;

import com.attendance.model.ClassSession;
import com.attendance.model.SectionSubject;
import com.attendance.model.StudentSectionEnrollment;
import com.attendance.model.enums.SessionStatus;
import com.attendance.repository.AttendanceRepository;
import com.attendance.repository.ClassSessionRepository;
import com.attendance.repository.SectionSubjectRepository;
import com.attendance.repository.StudentEnrollmentRepository;
import com.attendance.service.AttendanceCalculationService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttendanceCalculationServiceImpl implements AttendanceCalculationService {

    private final AttendanceRepository attendanceRepository;
    private final ClassSessionRepository classSessionRepository;
    private final StudentEnrollmentRepository enrollmentRepository;
    private final SectionSubjectRepository sectionSubjectRepository;

    public AttendanceCalculationServiceImpl(AttendanceRepository attendanceRepository,
                                            ClassSessionRepository classSessionRepository,
                                            StudentEnrollmentRepository enrollmentRepository,
                                            SectionSubjectRepository sectionSubjectRepository) {
        this.attendanceRepository = attendanceRepository;
        this.classSessionRepository = classSessionRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.sectionSubjectRepository = sectionSubjectRepository;
    }

    @Override
    public double calculateSubjectAttendance(long studentId, long sectionSubjectId) {
        int conducted = getClassesConducted(studentId, sectionSubjectId);
        if (conducted == 0) return 0.0;
        int attended = getClassesAttended(studentId, sectionSubjectId);
        return ((double) attended / conducted) * 100.0;
    }

    @Override
    public double calculateOverallAttendance(long studentId, long sectionId) {
        // Get all section-subjects for this section
        List<SectionSubject> sectionSubjects = sectionSubjectRepository.findBySectionId(sectionId);

        int totalAttended = 0;
        int totalConducted = 0;

        for (SectionSubject ss : sectionSubjects) {
            totalConducted += getClassesConducted(studentId, ss.getId());
            totalAttended += getClassesAttended(studentId, ss.getId());
        }

        if (totalConducted == 0) return 0.0;
        return ((double) totalAttended / totalConducted) * 100.0;
    }

    @Override
    public int getClassesConducted(long studentId, long sectionSubjectId) {
        // Count of FINALIZED sessions within the student's enrollment period
        return attendanceRepository.countByStudentAndSubject(studentId, sectionSubjectId);
    }

    @Override
    public int getClassesAttended(long studentId, long sectionSubjectId) {
        return attendanceRepository.countPresentByStudentAndSubject(studentId, sectionSubjectId);
    }

    @Override
    public int getClassesMissed(long studentId, long sectionSubjectId) {
        return getClassesConducted(studentId, sectionSubjectId) - getClassesAttended(studentId, sectionSubjectId);
    }

    @Override
    public List<Map<String, Object>> getBelowThresholdReport(long sectionId, double threshold) {
        List<Map<String, Object>> report = new ArrayList<>();

        // Get all active enrollments in this section
        List<StudentSectionEnrollment> enrollments = enrollmentRepository.findBySectionId(sectionId);
        List<SectionSubject> sectionSubjects = sectionSubjectRepository.findBySectionId(sectionId);

        for (StudentSectionEnrollment enrollment : enrollments) {
            long studentId = enrollment.getStudentId();

            for (SectionSubject ss : sectionSubjects) {
                int conducted = getClassesConducted(studentId, ss.getId());
                if (conducted == 0) continue;

                int attended = getClassesAttended(studentId, ss.getId());
                int missed = conducted - attended;
                double percentage = ((double) attended / conducted) * 100.0;

                if (percentage < threshold) {
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("studentId", studentId);
                    entry.put("sectionSubjectId", ss.getId());
                    entry.put("classesConducted", conducted);
                    entry.put("classesAttended", attended);
                    entry.put("classesMissed", missed);
                    entry.put("attendancePercentage", Math.round(percentage * 100.0) / 100.0);
                    report.add(entry);
                }
            }
        }

        return report;
    }
}
