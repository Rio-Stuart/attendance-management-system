package com.attendance.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

public class WeeklyTimetable {

    private long id;
    private long sectionSubjectId;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDate validFrom;
    private LocalDate validTo;

    public WeeklyTimetable() {}

    public WeeklyTimetable(long id, long sectionSubjectId, DayOfWeek dayOfWeek,
                            LocalTime startTime, LocalTime endTime,
                            LocalDate validFrom, LocalDate validTo) {
        this.id = id;
        this.sectionSubjectId = sectionSubjectId;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.validFrom = validFrom;
        this.validTo = validTo;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getSectionSubjectId() { return sectionSubjectId; }
    public void setSectionSubjectId(long sectionSubjectId) { this.sectionSubjectId = sectionSubjectId; }

    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public LocalDate getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDate validFrom) { this.validFrom = validFrom; }

    public LocalDate getValidTo() { return validTo; }
    public void setValidTo(LocalDate validTo) { this.validTo = validTo; }

    @Override
    public String toString() {
        return "WeeklyTimetable{" +
                "id=" + id +
                ", sectionSubjectId=" + sectionSubjectId +
                ", dayOfWeek=" + dayOfWeek +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", validFrom=" + validFrom +
                ", validTo=" + validTo +
                '}';
    }
}
