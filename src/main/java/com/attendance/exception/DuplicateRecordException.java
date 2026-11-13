package com.attendance.exception;

/**
 * Thrown when an insert or update would violate a uniqueness constraint.
 */
public class DuplicateRecordException extends AttendanceException {

    public DuplicateRecordException() {
        super();
    }

    public DuplicateRecordException(String message) {
        super(message);
    }

    public DuplicateRecordException(String message, Throwable cause) {
        super(message, cause);
    }
}
