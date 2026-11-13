package com.attendance.dto;

public class AttendanceMarkRequest {
    private long classSessionId;
    private long enrollmentId;
    private long studentId;
    private String status; // "PRESENT" or "ABSENT"

    public AttendanceMarkRequest() {}
    public AttendanceMarkRequest(long classSessionId, long enrollmentId, long studentId, String status) {
        this.classSessionId = classSessionId;
        this.enrollmentId = enrollmentId;
        this.studentId = studentId;
        this.status = status;
    }

    public long getClassSessionId() { return classSessionId; }
    public void setClassSessionId(long classSessionId) { this.classSessionId = classSessionId; }
    public long getEnrollmentId() { return enrollmentId; }
    public void setEnrollmentId(long enrollmentId) { this.enrollmentId = enrollmentId; }
    public long getStudentId() { return studentId; }
    public void setStudentId(long studentId) { this.studentId = studentId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
