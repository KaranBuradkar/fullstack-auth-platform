package com.authplatform.backend.exception;

public class MissingJWTException extends RuntimeException {
    public MissingJWTException(String message) {
        super(message);
    }
}
