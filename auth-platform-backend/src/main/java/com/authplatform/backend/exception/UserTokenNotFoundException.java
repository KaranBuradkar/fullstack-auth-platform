package com.authplatform.backend.exception;

public class UserTokenNotFoundException extends RuntimeException {
    public UserTokenNotFoundException(String message) {
        super(message);
    }
}
