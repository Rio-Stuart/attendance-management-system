package com.attendance.exception;

public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String message) {
        super(message);
    }

    public EntityNotFoundException(String entityName, long id) {
        super(entityName + " not found with id: " + id);
    }
}
