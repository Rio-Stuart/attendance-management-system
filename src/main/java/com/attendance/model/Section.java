package com.attendance.model;

import java.time.LocalDateTime;

public class Section {

    private long id;
    private String sectionName;
    private String academicYear;
    private LocalDateTime createdAt;

    public Section() {}

    public Section(long id, String sectionName, String academicYear, LocalDateTime createdAt) {
        this.id = id;
        this.sectionName = sectionName;
        this.academicYear = academicYear;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getSectionName() { return sectionName; }
    public void setSectionName(String sectionName) { this.sectionName = sectionName; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Section{" +
                "id=" + id +
                ", sectionName='" + sectionName + '\'' +
                ", academicYear='" + academicYear + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
