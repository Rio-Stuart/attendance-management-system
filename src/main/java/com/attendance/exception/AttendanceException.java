package com.attendance.exception;

/**
 * Base runtime exception for the Attendance Management System.
 */
public class AttendanceException extends RuntimeException {

    public AttendanceException() {
        super();
    }

    public AttendanceException(String message) {
        super(message);
    }

    public AttendanceException(String message, Throwable cause) {
        super(message, cause);
    }

    public AttendanceException(Throwable cause) {
        super(cause);
    }
}
