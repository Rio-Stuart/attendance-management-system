package com.attendance.exception;

/**
 * Thrown when a requested entity (user, session, record, etc.) is not found.
 */
public class ResourceNotFoundException extends AttendanceException {

    public ResourceNotFoundException() {
        super();
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
