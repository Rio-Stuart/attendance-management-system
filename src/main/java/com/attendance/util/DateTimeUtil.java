package com.attendance.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateTimeUtil {

    private static final ZoneId APP_TIMEZONE = ZoneId.of("Asia/Kolkata");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    public static LocalDate parseDate(String s) {
        return LocalDate.parse(s, DATE_FORMAT);
    }

    public static LocalTime parseTime(String s) {
        return LocalTime.parse(s, TIME_FORMAT);
    }

    public static String formatDate(LocalDate d) {
        return d != null ? d.format(DATE_FORMAT) : "";
    }

    public static String formatTime(LocalTime t) {
        return t != null ? t.format(TIME_FORMAT) : "";
    }

    public static ZoneId getAppTimezone() {
        return APP_TIMEZONE;
    }

    public static LocalDate today() {
        return LocalDate.now(APP_TIMEZONE);
    }
}
