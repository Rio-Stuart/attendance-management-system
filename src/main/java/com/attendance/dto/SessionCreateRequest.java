package com.attendance.dto;

public class SessionCreateRequest {
    private long sectionSubjectId;
    private long sectionId;
    private String date;      // yyyy-MM-dd
    private String startTime; // HH:mm
    private String endTime;   // HH:mm
    private Long timetableEntryId;

    public SessionCreateRequest() {}

    public long getSectionSubjectId() { return sectionSubjectId; }
    public void setSectionSubjectId(long sectionSubjectId) { this.sectionSubjectId = sectionSubjectId; }
    public long getSectionId() { return sectionId; }
    public void setSectionId(long sectionId) { this.sectionId = sectionId; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public Long getTimetableEntryId() { return timetableEntryId; }
    public void setTimetableEntryId(Long timetableEntryId) { this.timetableEntryId = timetableEntryId; }
}
