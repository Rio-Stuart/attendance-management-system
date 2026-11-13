package com.attendance.exception;

/**
 * Thrown when a requested operation is not valid in the current state
 * (e.g. finalizing an already-cancelled session).
 */
public class InvalidOperationException extends AttendanceException {

    public InvalidOperationException() {
        super();
    }

    public InvalidOperationException(String message) {
        super(message);
    }

    public InvalidOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
