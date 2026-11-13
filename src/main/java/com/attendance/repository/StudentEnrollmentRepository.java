package com.attendance.repository;

import com.attendance.model.StudentSectionEnrollment;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StudentEnrollmentRepository {

    StudentSectionEnrollment save(StudentSectionEnrollment enrollment);

    Optional<StudentSectionEnrollment> findById(long id);

    List<StudentSectionEnrollment> findByStudentId(long studentId);

    Optional<StudentSectionEnrollment> findActiveByStudentId(long studentId);

    List<StudentSectionEnrollment> findBySectionId(long sectionId);

    List<StudentSectionEnrollment> findActiveEnrollmentsBySectionIdOnDate(long sectionId, LocalDate date);

    StudentSectionEnrollment deactivate(long id, LocalDate endDate);

    boolean isStudentEnrolledInSectionOnDate(long studentId, long sectionId, LocalDate date);
}
