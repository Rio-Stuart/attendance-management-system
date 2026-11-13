package com.attendance.service;

import com.attendance.model.ClassSession;
import com.attendance.model.enums.SessionStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ClassSessionService {

    ClassSession createSession(long sectionSubjectId, long sectionId, LocalDate date,
                               LocalTime start, LocalTime end, Long timetableEntryId, long createdBy);

    ClassSession findById(long id);

    List<ClassSession> findBySectionSubjectId(long sectionSubjectId);

    ClassSession cancelSession(long sessionId, String reason);

    ClassSession updateSessionStatus(long sessionId, SessionStatus newStatus);
}
