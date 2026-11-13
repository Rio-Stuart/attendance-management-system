package com.attendance.model;

import com.attendance.model.enums.SessionStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class ClassSession {

    private long id;
    private long sectionSubjectId;
    private long sectionId;
    private Long timetableEntryId;
    private LocalDate sessionDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private SessionStatus status;
    private Long createdBy;
    private Long finalizedBy;
    private LocalDateTime createdAt;
    private LocalDateTime finalizedAt;
    private String cancellationReason;

    public ClassSession() {}

    public ClassSession(long id, long sectionSubjectId, long sectionId, Long timetableEntryId,
                         LocalDate sessionDate, LocalTime startTime, LocalTime endTime,
                         SessionStatus status, Long createdBy, Long finalizedBy,
                         LocalDateTime createdAt, LocalDateTime finalizedAt,
                         String cancellationReason) {
        this.id = id;
        this.sectionSubjectId = sectionSubjectId;
        this.sectionId = sectionId;
        this.timetableEntryId = timetableEntryId;
        this.sessionDate = sessionDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.createdBy = createdBy;
        this.finalizedBy = finalizedBy;
        this.createdAt = createdAt;
        this.finalizedAt = finalizedAt;
        this.cancellationReason = cancellationReason;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getSectionSubjectId() { return sectionSubjectId; }
    public void setSectionSubjectId(long sectionSubjectId) { this.sectionSubjectId = sectionSubjectId; }

    public long getSectionId() { return sectionId; }
    public void setSectionId(long sectionId) { this.sectionId = sectionId; }

    public Long getTimetableEntryId() { return timetableEntryId; }
    public void setTimetableEntryId(Long timetableEntryId) { this.timetableEntryId = timetableEntryId; }

    public LocalDate getSessionDate() { return sessionDate; }
    public void setSessionDate(LocalDate sessionDate) { this.sessionDate = sessionDate; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public SessionStatus getStatus() { return status; }
    public void setStatus(SessionStatus status) { this.status = status; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public Long getFinalizedBy() { return finalizedBy; }
    public void setFinalizedBy(Long finalizedBy) { this.finalizedBy = finalizedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getFinalizedAt() { return finalizedAt; }
    public void setFinalizedAt(LocalDateTime finalizedAt) { this.finalizedAt = finalizedAt; }

    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }

    @Override
    public String toString() {
        return "ClassSession{" +
                "id=" + id +
                ", sectionSubjectId=" + sectionSubjectId +
                ", sectionId=" + sectionId +
                ", timetableEntryId=" + timetableEntryId +
                ", sessionDate=" + sessionDate +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", status=" + status +
                ", createdBy=" + createdBy +
                ", finalizedBy=" + finalizedBy +
                ", createdAt=" + createdAt +
                ", finalizedAt=" + finalizedAt +
                ", cancellationReason='" + cancellationReason + '\'' +
                '}';
    }
}
