package com.attendance.service.impl;

import com.attendance.model.ClassSession;
import com.attendance.model.enums.SessionStatus;
import com.attendance.repository.ClassSessionRepository;
import com.attendance.repository.SectionSubjectRepository;
import com.attendance.service.ClassSessionService;
import com.attendance.exception.ValidationException;
import com.attendance.exception.InvalidOperationException;
import com.attendance.exception.ResourceNotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

public class ClassSessionServiceImpl implements ClassSessionService {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    private final ClassSessionRepository classSessionRepository;
    private final SectionSubjectRepository sectionSubjectRepository;

    public ClassSessionServiceImpl(ClassSessionRepository classSessionRepository,
                                   SectionSubjectRepository sectionSubjectRepository) {
        this.classSessionRepository = classSessionRepository;
        this.sectionSubjectRepository = sectionSubjectRepository;
    }

    @Override
    public ClassSession createSession(long sectionSubjectId, long sectionId, LocalDate date,
                                      LocalTime start, LocalTime end, Long timetableEntryId, long createdBy) {
        if (date == null) {
            throw new ValidationException("Session date cannot be null");
        }
        if (start == null || end == null) {
            throw new ValidationException("Start time and end time cannot be null");
        }
        if (!start.isBefore(end)) {
            throw new ValidationException("Start time must be before end time");
        }

        sectionSubjectRepository.findById(sectionSubjectId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "SectionSubject not found with id: " + sectionSubjectId));

        if (classSessionRepository.existsByUnique(sectionSubjectId, date, start)) {
            throw new InvalidOperationException(
                    "A session already exists for sectionSubjectId=" + sectionSubjectId
                    + " on " + date + " at " + start);
        }

        ClassSession session = new ClassSession();
        session.setSectionSubjectId(sectionSubjectId);
        session.setSectionId(sectionId);
        session.setSessionDate(date);
        session.setStartTime(start);
        session.setEndTime(end);
        session.setTimetableEntryId(timetableEntryId);
        session.setStatus(SessionStatus.SCHEDULED);
        session.setCreatedBy(createdBy);
        session.setCreatedAt(LocalDateTime.now(IST));

        return classSessionRepository.save(session);
    }

    @Override
    public ClassSession findById(long id) {
        return classSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ClassSession not found with id: " + id));
    }

    @Override
    public List<ClassSession> findBySectionSubjectId(long sectionSubjectId) {
        return classSessionRepository.findBySectionSubjectId(sectionSubjectId);
    }

    @Override
    public ClassSession cancelSession(long sessionId, String reason) {
        ClassSession session = findById(sessionId);

        if (session.getStatus() == SessionStatus.FINALIZED) {
            throw new InvalidOperationException("Cannot cancel a FINALIZED session");
        }
        if (session.getStatus() == SessionStatus.CANCELLED) {
            throw new InvalidOperationException("Session is already CANCELLED");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new ValidationException("Cancellation reason is required");
        }

        return classSessionRepository.updateStatus(sessionId, SessionStatus.CANCELLED, null, null);
    }

    @Override
    public ClassSession updateSessionStatus(long sessionId, SessionStatus newStatus) {
        if (newStatus == null) {
            throw new ValidationException("New status cannot be null");
        }

        ClassSession session = findById(sessionId);
        SessionStatus current = session.getStatus();

        switch (current) {
            case SCHEDULED:
                if (newStatus != SessionStatus.DRAFT && newStatus != SessionStatus.CANCELLED) {
                    throw new InvalidOperationException(
                            "SCHEDULED sessions can only transition to DRAFT or CANCELLED");
                }
                break;
            case DRAFT:
                if (newStatus != SessionStatus.FINALIZED && newStatus != SessionStatus.CANCELLED) {
                    throw new InvalidOperationException(
                            "DRAFT sessions can only transition to FINALIZED or CANCELLED");
                }
                break;
            case FINALIZED:
                throw new InvalidOperationException("FINALIZED sessions cannot change status");
            case CANCELLED:
                throw new InvalidOperationException("CANCELLED sessions cannot change status");
        }

        LocalDateTime finalizedAt = null;
        Long finalizedBy = null;
        if (newStatus == SessionStatus.FINALIZED) {
            finalizedAt = LocalDateTime.now(IST);
        }

        return classSessionRepository.updateStatus(sessionId, newStatus, finalizedBy, finalizedAt);
    }
}
