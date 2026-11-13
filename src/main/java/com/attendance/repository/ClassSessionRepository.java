package com.attendance.repository;

import com.attendance.model.ClassSession;
import com.attendance.model.enums.SessionStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ClassSessionRepository {

    ClassSession save(ClassSession session);

    Optional<ClassSession> findById(long id);

    List<ClassSession> findBySectionSubjectId(long sectionSubjectId);

    List<ClassSession> findBySectionId(long sectionId);

    List<ClassSession> findBySectionIdAndStatus(long sectionId, SessionStatus status);

    List<ClassSession> findBySectionSubjectIdAndStatus(long sectionSubjectId, SessionStatus status);

    List<ClassSession> findBySectionSubjectIdAndDateRange(long sectionSubjectId, LocalDate start, LocalDate end);

    List<ClassSession> findScheduledSessionsForPrediction(long sectionSubjectId, LocalDate from, LocalDate to);

    Optional<ClassSession> findByUnique(long sectionSubjectId, LocalDate date, LocalTime startTime);

    ClassSession updateStatus(long id, SessionStatus status, Long finalizedBy, LocalDateTime finalizedAt);

    int countFinalizedSessions(long sectionSubjectId);

    boolean existsByUnique(long sectionSubjectId, LocalDate date, LocalTime startTime);

    List<ClassSession> findAll();
}
