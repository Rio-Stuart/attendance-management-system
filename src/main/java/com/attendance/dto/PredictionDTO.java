package com.attendance.dto;

public class PredictionDTO {
    private String subjectName;
    private int currentConducted;
    private int currentAttended;
    private double currentPercentage;
    private int upcomingSessions;
    private double predictedIfAllAttend;
    private double predictedIfAllAbsent;

    public PredictionDTO() {}

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public int getCurrentConducted() { return currentConducted; }
    public void setCurrentConducted(int c) { this.currentConducted = c; }
    public int getCurrentAttended() { return currentAttended; }
    public void setCurrentAttended(int a) { this.currentAttended = a; }
    public double getCurrentPercentage() { return currentPercentage; }
    public void setCurrentPercentage(double p) { this.currentPercentage = p; }
    public int getUpcomingSessions() { return upcomingSessions; }
    public void setUpcomingSessions(int u) { this.upcomingSessions = u; }
    public double getPredictedIfAllAttend() { return predictedIfAllAttend; }
    public void setPredictedIfAllAttend(double p) { this.predictedIfAllAttend = p; }
    public double getPredictedIfAllAbsent() { return predictedIfAllAbsent; }
    public void setPredictedIfAllAbsent(double p) { this.predictedIfAllAbsent = p; }

    @Override
    public String toString() {
        return String.format(
            "Subject: %s\n  Current: %d/%d (%.2f%%)\n  Upcoming: %d sessions\n  If all attend: %.2f%%\n  If all absent: %.2f%%",
            subjectName, currentAttended, currentConducted, currentPercentage,
            upcomingSessions, predictedIfAllAttend, predictedIfAllAbsent);
    }
}
