package com.attendance.model;

import java.time.LocalDate;

public class UniversityHoliday {

    private long id;
    private LocalDate holidayDate;
    private String description;

    public UniversityHoliday() {}

    public UniversityHoliday(long id, LocalDate holidayDate, String description) {
        this.id = id;
        this.holidayDate = holidayDate;
        this.description = description;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public LocalDate getHolidayDate() { return holidayDate; }
    public void setHolidayDate(LocalDate holidayDate) { this.holidayDate = holidayDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return "UniversityHoliday{" +
                "id=" + id +
                ", holidayDate=" + holidayDate +
                ", description='" + description + '\'' +
                '}';
    }
}
