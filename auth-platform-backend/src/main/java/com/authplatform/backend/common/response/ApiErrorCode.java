package com.authplatform.backend.common.response;

import org.springframework.http.HttpStatus;

public enum ApiErrorCode {

    // =========================
    // Validation
    // =========================

    VALIDATION_FAILED(
            HttpStatus.BAD_REQUEST,
            "One or more fields are invalid"
    ),

    INVALID_REQUEST(
            HttpStatus.BAD_REQUEST,
            "The request is invalid"
    ),

    MISSING_REQUIRED_FIELD(
            HttpStatus.BAD_REQUEST,
            "A required field is missing"
    ),

    INVALID_PARAMETER(
            HttpStatus.BAD_REQUEST,
            "One or more parameters are invalid"
    ),

    // =========================
    // Authentication
    // =========================

    INVALID_CREDENTIALS(
            HttpStatus.UNAUTHORIZED,
            "Invalid username or password"
    ),

    UNAUTHORIZED(
            HttpStatus.UNAUTHORIZED,
            "Authentication is required"
    ),

    INVALID_TOKEN(
            HttpStatus.UNAUTHORIZED,
            "The authentication token is invalid"
    ),

    TOKEN_EXPIRED(
            HttpStatus.UNAUTHORIZED,
            "The authentication token has expired"
    ),

    TOKEN_MISSING(
            HttpStatus.UNAUTHORIZED,
            "Authentication token is missing"
    ),

    REFRESH_TOKEN_INVALID(
            HttpStatus.UNAUTHORIZED,
            "The refresh token is invalid"
    ),

    REFRESH_TOKEN_EXPIRED(
            HttpStatus.UNAUTHORIZED,
            "The refresh token has expired"
    ),

    REFRESH_TOKEN_REVOKED(
            HttpStatus.UNAUTHORIZED,
            "The Refresh token has been revoked and can no longer be used"),

    // =========================
    // Authorization
    // =========================

    FORBIDDEN(
            HttpStatus.FORBIDDEN,
            "You do not have permission to perform this operation"
    ),

    INSUFFICIENT_PERMISSION(
            HttpStatus.FORBIDDEN,
            "You do not have sufficient permission"
    ),

    // =========================
    // User / Registration
    // =========================

    USER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "User not found"
    ),

    USER_TOKEN_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "User Token not found"
    ),

    EMAIL_ALREADY_EXISTS(
            HttpStatus.CONFLICT,
            "An account with this email already exists"
    ),

    EMAIL_ALREADY_VERIFIED(
            HttpStatus.CONFLICT,
            "An account with this email already verified"
    ),

    EMAIL_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "User email not found"
    ),

    EMAIL_FAILED_TO_SEND(
            HttpStatus.BAD_GATEWAY,
            "Failed to send email"
    ),

    PHONE_ALREADY_EXISTS(
            HttpStatus.CONFLICT,
            "An account with this phone number already exists"
    ),

    ACCOUNT_DISABLED(
            HttpStatus.FORBIDDEN,
            "This account is disabled"
    ),

    ACCOUNT_LOCKED(
            HttpStatus.FORBIDDEN,
            "This account is locked"
    ),

    ACCOUNT_NOT_VERIFIED(
            HttpStatus.FORBIDDEN,
            "This account has not been verified"
    ),

    // =========================
    // OTP
    // =========================

    INVALID_OTP(
            HttpStatus.BAD_REQUEST,
            "The OTP is invalid"
    ),

    OTP_EXPIRED(
            HttpStatus.BAD_REQUEST,
            "The OTP has expired"
    ),

    OTP_TOO_MANY_ATTEMPTS(
            HttpStatus.TOO_MANY_REQUESTS,
            "Too many OTP verification attempts"
    ),

    OTP_RATE_LIMIT_EXCEEDED(
            HttpStatus.TOO_MANY_REQUESTS,
            "Too many OTP requests. Please try again later"
    ),

    OTP_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "No active OTP request was found"
    ),

    // =========================
    // Password
    // =========================

    CURRENT_PASSWORD_INCORRECT(
            HttpStatus.BAD_REQUEST,
            "The current password is incorrect"
    ),

    PASSWORD_RESET_TOKEN_INVALID(
            HttpStatus.BAD_REQUEST,
            "The password reset token is invalid"
    ),

    PASSWORD_RESET_TOKEN_EXPIRED(
            HttpStatus.BAD_REQUEST,
            "The password reset token has expired"
    ),

    PASSWORD_SAME_AS_CURRENT(
            HttpStatus.BAD_REQUEST,
            "The new password must be different from the current password"
    ),

    // =========================
    // OAuth2
    // =========================

    OAUTH2_AUTHENTICATION_FAILED(
            HttpStatus.UNAUTHORIZED,
            "OAuth2 authentication failed"
    ),

    OAUTH2_PROVIDER_NOT_SUPPORTED(
            HttpStatus.BAD_REQUEST,
            "The requested OAuth2 provider is not supported"
    ),

    OAUTH2_EMAIL_NOT_AVAILABLE(
            HttpStatus.BAD_REQUEST,
            "The OAuth2 provider did not provide an email address"
    ),

    OAUTH2_ACCOUNT_LINK_FAILED(
            HttpStatus.CONFLICT,
            "The OAuth2 account could not be linked"
    ),

    // =========================
    // Resource
    // =========================

    RESOURCE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "The requested resource was not found"
    ),

    RESOURCE_ALREADY_EXISTS(
            HttpStatus.CONFLICT,
            "The requested resource already exists"
    ),

    // =========================
    // Request / Rate Limiting
    // =========================

    TOO_MANY_REQUESTS(
            HttpStatus.TOO_MANY_REQUESTS,
            "Too many requests. Please try again later"
    ),

    REQUEST_TIMEOUT(
            HttpStatus.REQUEST_TIMEOUT,
            "The request timed out"
    ),

    BAD_REQUEST(
            HttpStatus.BAD_REQUEST,
            "Bad request"
    ),

    // =========================
    // DATABASE
    // =========================

    DATABASE_CONSTRAINT_VIOLATION(
            HttpStatus.CONFLICT,
            "The request conflicts with existing data"
    ),

    // =========================
    // Server
    // =========================

    INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred"
    ),

    SERVICE_UNAVAILABLE(
            HttpStatus.SERVICE_UNAVAILABLE,
            "The service is temporarily unavailable"
    );

    private final HttpStatus status;
    private final String message;

    ApiErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus status() {
        return status;
    }

    public String message() {
        return message;
    }
}