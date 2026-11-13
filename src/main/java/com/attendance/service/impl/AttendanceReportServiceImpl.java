package com.attendance.service.impl;

import com.attendance.model.Section;
import com.attendance.model.SectionSubject;
import com.attendance.model.StudentSectionEnrollment;
import com.attendance.model.Subject;
import com.attendance.model.User;
import com.attendance.repository.*;
import com.attendance.service.AttendanceCalculationService;
import com.attendance.service.AttendanceReportService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttendanceReportServiceImpl implements AttendanceReportService {

    private final AttendanceCalculationService calculationService;
    private final StudentEnrollmentRepository enrollmentRepository;
    private final SectionSubjectRepository sectionSubjectRepository;
    private final UserRepository userRepository;
    private final SectionRepository sectionRepository;
    private final SubjectRepository subjectRepository;

    public AttendanceReportServiceImpl(AttendanceCalculationService calculationService,
                                       StudentEnrollmentRepository enrollmentRepository,
                                       SectionSubjectRepository sectionSubjectRepository,
                                       UserRepository userRepository,
                                       SectionRepository sectionRepository,
                                       SubjectRepository subjectRepository) {
        this.calculationService = calculationService;
        this.enrollmentRepository = enrollmentRepository;
        this.sectionSubjectRepository = sectionSubjectRepository;
        this.userRepository = userRepository;
        this.sectionRepository = sectionRepository;
        this.subjectRepository = subjectRepository;
    }

    @Override
    public Map<String, Object> getStudentDashboard(long studentId) {
        Map<String, Object> dashboard = new HashMap<>();

        User student = userRepository.findById(studentId).orElse(null);
        if (student == null) {
            dashboard.put("error", "Student not found");
            return dashboard;
        }

        StudentSectionEnrollment activeEnrollment = enrollmentRepository.findActiveByStudentId(studentId).orElse(null);
        if (activeEnrollment == null) {
            dashboard.put("error", "No active enrollment found");
            return dashboard;
        }

        long sectionId = activeEnrollment.getSectionId();
        Section section = sectionRepository.findById(sectionId).orElse(null);

        dashboard.put("studentId", studentId);
        dashboard.put("studentName", student.getFullName());
        dashboard.put("rollNumber", student.getUsername());
        dashboard.put("sectionName", section != null ? section.getSectionName() : "Unknown");

        // Subject-wise attendance
        List<Map<String, Object>> subjectWise = new ArrayList<>();
        List<SectionSubject> sectionSubjects = sectionSubjectRepository.findBySectionId(sectionId);

        for (SectionSubject ss : sectionSubjects) {
            Subject subject = subjectRepository.findById(ss.getSubjectId()).orElse(null);
            int conducted = calculationService.getClassesConducted(studentId, ss.getId());
            int attended = calculationService.getClassesAttended(studentId, ss.getId());
            int missed = conducted - attended;
            double percentage = conducted > 0 ? ((double) attended / conducted) * 100.0 : 0.0;

            Map<String, Object> subjectEntry = new HashMap<>();
            subjectEntry.put("subjectName", subject != null ? subject.getSubjectName() : "Unknown");
            subjectEntry.put("subjectCode", subject != null ? subject.getSubjectCode() : "");
            subjectEntry.put("classesConducted", conducted);
            subjectEntry.put("classesAttended", attended);
            subjectEntry.put("classesMissed", missed);
            subjectEntry.put("attendancePercentage", Math.round(percentage * 100.0) / 100.0);
            subjectWise.add(subjectEntry);
        }
        dashboard.put("subjectWiseAttendance", subjectWise);

        // Overall attendance (weighted, not average)
        double overall = calculationService.calculateOverallAttendance(studentId, sectionId);
        dashboard.put("overallPercentage", Math.round(overall * 100.0) / 100.0);

        return dashboard;
    }

    @Override
    public List<Map<String, Object>> getSectionReport(long sectionId) {
        List<Map<String, Object>> report = new ArrayList<>();
        List<StudentSectionEnrollment> enrollments = enrollmentRepository.findBySectionId(sectionId);

        for (StudentSectionEnrollment enrollment : enrollments) {
            long studentId = enrollment.getStudentId();
            User student = userRepository.findById(studentId).orElse(null);

            double overall = calculationService.calculateOverallAttendance(studentId, sectionId);
            Map<String, Object> entry = new HashMap<>();
            entry.put("studentId", studentId);
            entry.put("studentName", student != null ? student.getFullName() : "Unknown");
            entry.put("rollNumber", student != null ? student.getUsername() : "");
            entry.put("overallPercentage", Math.round(overall * 100.0) / 100.0);
            report.add(entry);
        }

        return report;
    }

    @Override
    public List<Map<String, Object>> getSubjectReport(long sectionSubjectId) {
        List<Map<String, Object>> report = new ArrayList<>();

        SectionSubject ss = sectionSubjectRepository.findById(sectionSubjectId).orElse(null);
        if (ss == null) return report;

        List<StudentSectionEnrollment> enrollments = enrollmentRepository.findBySectionId(ss.getSectionId());

        for (StudentSectionEnrollment enrollment : enrollments) {
            long studentId = enrollment.getStudentId();
            User student = userRepository.findById(studentId).orElse(null);

            int conducted = calculationService.getClassesConducted(studentId, sectionSubjectId);
            int attended = calculationService.getClassesAttended(studentId, sectionSubjectId);
            int missed = conducted - attended;
            double percentage = conducted > 0 ? ((double) attended / conducted) * 100.0 : 0.0;

            Map<String, Object> entry = new HashMap<>();
            entry.put("studentId", studentId);
            entry.put("studentName", student != null ? student.getFullName() : "Unknown");
            entry.put("rollNumber", student != null ? student.getUsername() : "");
            entry.put("classesConducted", conducted);
            entry.put("classesAttended", attended);
            entry.put("classesMissed", missed);
            entry.put("attendancePercentage", Math.round(percentage * 100.0) / 100.0);
            report.add(entry);
        }

        return report;
    }
}
