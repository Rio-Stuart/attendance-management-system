package com.attendance.model.enums;

/**
 * Days of the week enum.
 * Can be used interchangeably with {@link java.time.DayOfWeek} via {@link #toJavaDayOfWeek()}.
 */
public enum DayOfWeekEnum {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY;

    /**
     * Converts this enum to the standard {@link java.time.DayOfWeek}.
     */
    public java.time.DayOfWeek toJavaDayOfWeek() {
        return java.time.DayOfWeek.valueOf(this.name());
    }

    /**
     * Creates a DayOfWeekEnum from a {@link java.time.DayOfWeek}.
     */
    public static DayOfWeekEnum fromJavaDayOfWeek(java.time.DayOfWeek dow) {
        return DayOfWeekEnum.valueOf(dow.name());
    }
}
