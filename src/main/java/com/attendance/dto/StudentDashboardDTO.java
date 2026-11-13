package com.attendance.dto;

import java.util.List;

public class StudentDashboardDTO {
    private long studentId;
    private String studentName;
    private String sectionName;
    private double overallPercentage;
    private List<AttendanceSummaryDTO> subjectWiseAttendance;

    public StudentDashboardDTO() {}

    public long getStudentId() { return studentId; }
    public void setStudentId(long studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getSectionName() { return sectionName; }
    public void setSectionName(String sectionName) { this.sectionName = sectionName; }
    public double getOverallPercentage() { return overallPercentage; }
    public void setOverallPercentage(double overallPercentage) { this.overallPercentage = overallPercentage; }
    public List<AttendanceSummaryDTO> getSubjectWiseAttendance() { return subjectWiseAttendance; }
    public void setSubjectWiseAttendance(List<AttendanceSummaryDTO> s) { this.subjectWiseAttendance = s; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("========== STUDENT DASHBOARD ==========\n");
        sb.append("Student : ").append(studentName).append("\n");
        sb.append("Section : ").append(sectionName).append("\n");
        sb.append(String.format("Overall : %.2f%%\n", overallPercentage));
        sb.append("---------------------------------------\n");
        if (subjectWiseAttendance != null) {
            for (AttendanceSummaryDTO s : subjectWiseAttendance) {
                sb.append(s.toString()).append("\n");
            }
        }
        sb.append("=======================================");
        return sb.toString();
    }
}
