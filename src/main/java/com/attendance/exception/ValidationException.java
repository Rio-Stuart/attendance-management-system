package com.attendance.exception;

/**
 * Thrown when input data fails validation rules.
 */
public class ValidationException extends AttendanceException {

    public ValidationException() {
        super();
    }

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
