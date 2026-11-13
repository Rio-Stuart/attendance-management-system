package com.attendance.dto;

public class SessionDTO {
    private long id;
    private String sectionName;
    private String subjectName;
    private String date;
    private String startTime;
    private String endTime;
    private String status;

    public SessionDTO() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getSectionName() { return sectionName; }
    public void setSectionName(String sectionName) { this.sectionName = sectionName; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return String.format("  [%d] %s | %s | %s | %s - %s | %s",
                id, sectionName, subjectName, date, startTime, endTime, status);
    }
}
