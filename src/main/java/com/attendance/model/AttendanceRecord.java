package com.attendance.model;

import com.attendance.model.enums.AttendanceStatus;
import java.time.LocalDateTime;

public class AttendanceRecord {

    private long id;
    private long classSessionId;
    private long studentSectionEnrollmentId;
    private long studentId;
    private long sectionId;
    private AttendanceStatus attendanceStatus;
    private Long markedBy;
    private LocalDateTime markedAt;
    private LocalDateTime updatedAt;

    public AttendanceRecord() {}

    public AttendanceRecord(long id, long classSessionId, long studentSectionEnrollmentId,
                             long studentId, long sectionId, AttendanceStatus attendanceStatus,
                             Long markedBy, LocalDateTime markedAt, LocalDateTime updatedAt) {
        this.id = id;
        this.classSessionId = classSessionId;
        this.studentSectionEnrollmentId = studentSectionEnrollmentId;
        this.studentId = studentId;
        this.sectionId = sectionId;
        this.attendanceStatus = attendanceStatus;
        this.markedBy = markedBy;
        this.markedAt = markedAt;
        this.updatedAt = updatedAt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getClassSessionId() { return classSessionId; }
    public void setClassSessionId(long classSessionId) { this.classSessionId = classSessionId; }

    public long getStudentSectionEnrollmentId() { return studentSectionEnrollmentId; }
    public void setStudentSectionEnrollmentId(long studentSectionEnrollmentId) { this.studentSectionEnrollmentId = studentSectionEnrollmentId; }

    public long getStudentId() { return studentId; }
    public void setStudentId(long studentId) { this.studentId = studentId; }

    public long getSectionId() { return sectionId; }
    public void setSectionId(long sectionId) { this.sectionId = sectionId; }

    public AttendanceStatus getAttendanceStatus() { return attendanceStatus; }
    public void setAttendanceStatus(AttendanceStatus attendanceStatus) { this.attendanceStatus = attendanceStatus; }

    public Long getMarkedBy() { return markedBy; }
    public void setMarkedBy(Long markedBy) { this.markedBy = markedBy; }

    public LocalDateTime getMarkedAt() { return markedAt; }
    public void setMarkedAt(LocalDateTime markedAt) { this.markedAt = markedAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "AttendanceRecord{" +
                "id=" + id +
                ", classSessionId=" + classSessionId +
                ", studentSectionEnrollmentId=" + studentSectionEnrollmentId +
                ", studentId=" + studentId +
                ", sectionId=" + sectionId +
                ", attendanceStatus=" + attendanceStatus +
                ", markedBy=" + markedBy +
                ", markedAt=" + markedAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
