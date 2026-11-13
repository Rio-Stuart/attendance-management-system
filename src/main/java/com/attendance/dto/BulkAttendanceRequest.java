package com.attendance.dto;

import java.util.List;

public class BulkAttendanceRequest {
    private long classSessionId;
    private List<AttendanceMarkRequest> attendanceEntries;
    private long markedBy;

    public BulkAttendanceRequest() {}

    public long getClassSessionId() { return classSessionId; }
    public void setClassSessionId(long classSessionId) { this.classSessionId = classSessionId; }
    public List<AttendanceMarkRequest> getAttendanceEntries() { return attendanceEntries; }
    public void setAttendanceEntries(List<AttendanceMarkRequest> attendanceEntries) { this.attendanceEntries = attendanceEntries; }
    public long getMarkedBy() { return markedBy; }
    public void setMarkedBy(long markedBy) { this.markedBy = markedBy; }
}
