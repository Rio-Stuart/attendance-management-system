package com.attendance.repository;

import com.attendance.model.AttendanceRecord;
import com.attendance.model.enums.AttendanceStatus;

import java.util.List;
import java.util.Optional;

public interface AttendanceRepository {

    AttendanceRecord save(AttendanceRecord record);

    Optional<AttendanceRecord> findById(long id);

    List<AttendanceRecord> findByClassSessionId(long classSessionId);

    Optional<AttendanceRecord> findBySessionAndEnrollment(long classSessionId, long enrollmentId);

    int countPresentByStudentAndSubject(long studentId, long sectionSubjectId);

    int countByStudentAndSubject(long studentId, long sectionSubjectId);

    AttendanceRecord updateStatus(long id, AttendanceStatus status, long updatedBy);

    void saveAll(List<AttendanceRecord> records);

    boolean existsBySessionAndEnrollment(long classSessionId, long enrollmentId);

    List<AttendanceRecord> findByStudentId(long studentId);

    void deleteByClassSessionId(long classSessionId);
}
