package com.edsonjr.taskflow.exception;

public class EmailAlreadyExistsException extends ConflictException {

    public EmailAlreadyExistsException(String message) {
        super(message);
    }

    public EmailAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}