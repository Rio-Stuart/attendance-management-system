package com.attendance.model;

import com.attendance.model.enums.EnrollmentStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class StudentSectionEnrollment {

    private long id;
    private long studentId;
    private long sectionId;
    private String academicYear;
    private LocalDate startDate;
    private LocalDate endDate;
    private EnrollmentStatus status;
    private LocalDateTime createdAt;

    public StudentSectionEnrollment() {}

    public StudentSectionEnrollment(long id, long studentId, long sectionId, String academicYear,
                                     LocalDate startDate, LocalDate endDate,
                                     EnrollmentStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.studentId = studentId;
        this.sectionId = sectionId;
        this.academicYear = academicYear;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getStudentId() { return studentId; }
    public void setStudentId(long studentId) { this.studentId = studentId; }

    public long getSectionId() { return sectionId; }
    public void setSectionId(long sectionId) { this.sectionId = sectionId; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public EnrollmentStatus getStatus() { return status; }
    public void setStatus(EnrollmentStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "StudentSectionEnrollment{" +
                "id=" + id +
                ", studentId=" + studentId +
                ", sectionId=" + sectionId +
                ", academicYear='" + academicYear + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}
