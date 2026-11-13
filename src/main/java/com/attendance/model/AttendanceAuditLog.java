package com.attendance.model;

import java.time.LocalDateTime;

public class AttendanceAuditLog {

    private long id;
    private Long attendanceRecordId;
    private String previousStatus;
    private String newStatus;
    private Long changedBy;
    private LocalDateTime changedAt;
    private String reason;

    public AttendanceAuditLog() {}

    public AttendanceAuditLog(long id, Long attendanceRecordId, String previousStatus,
                               String newStatus, Long changedBy, LocalDateTime changedAt,
                               String reason) {
        this.id = id;
        this.attendanceRecordId = attendanceRecordId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.changedBy = changedBy;
        this.changedAt = changedAt;
        this.reason = reason;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public Long getAttendanceRecordId() { return attendanceRecordId; }
    public void setAttendanceRecordId(Long attendanceRecordId) { this.attendanceRecordId = attendanceRecordId; }

    public String getPreviousStatus() { return previousStatus; }
    public void setPreviousStatus(String previousStatus) { this.previousStatus = previousStatus; }

    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }

    public Long getChangedBy() { return changedBy; }
    public void setChangedBy(Long changedBy) { this.changedBy = changedBy; }

    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    @Override
    public String toString() {
        return "AttendanceAuditLog{" +
                "id=" + id +
                ", attendanceRecordId=" + attendanceRecordId +
                ", previousStatus='" + previousStatus + '\'' +
                ", newStatus='" + newStatus + '\'' +
                ", changedBy=" + changedBy +
                ", changedAt=" + changedAt +
                ", reason='" + reason + '\'' +
                '}';
    }
}
