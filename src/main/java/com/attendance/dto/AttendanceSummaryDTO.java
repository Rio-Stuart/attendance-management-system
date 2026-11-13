package com.attendance.dto;

public class AttendanceSummaryDTO {
    private long studentId;
    private String studentName;
    private String rollNumber;
    private String subjectName;
    private String sectionName;
    private int classesConducted;
    private int classesAttended;
    private int classesMissed;
    private double attendancePercentage;

    public AttendanceSummaryDTO() {}

    public long getStudentId() { return studentId; }
    public void setStudentId(long studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public String getSectionName() { return sectionName; }
    public void setSectionName(String sectionName) { this.sectionName = sectionName; }
    public int getClassesConducted() { return classesConducted; }
    public void setClassesConducted(int classesConducted) { this.classesConducted = classesConducted; }
    public int getClassesAttended() { return classesAttended; }
    public void setClassesAttended(int classesAttended) { this.classesAttended = classesAttended; }
    public int getClassesMissed() { return classesMissed; }
    public void setClassesMissed(int classesMissed) { this.classesMissed = classesMissed; }
    public double getAttendancePercentage() { return attendancePercentage; }
    public void setAttendancePercentage(double p) { this.attendancePercentage = p; }

    @Override
    public String toString() {
        return String.format("  %-30s | Conducted: %3d | Attended: %3d | Missed: %3d | %.2f%%",
                subjectName, classesConducted, classesAttended, classesMissed, attendancePercentage);
    }
}
