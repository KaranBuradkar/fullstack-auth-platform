package com.authplatform.backend.common.response;

public enum ApiSuccessCode {

    // =========================
    // Authentication
    // =========================

    LOGIN_SUCCESS(
            "Login successful"
    ),

    LOGOUT_SUCCESS(
            "Logout successful"
    ),

    TOKEN_REFRESHED(
            "Authentication token refreshed successfully"
    ),

    // =========================
    // Registration
    // =========================

    USER_REGISTERED(
            "User registered successfully"
    ),

    USER_RETRIEVED(
            "User retrieved successfully"
    ),

    USER_UPDATED(
            "User updated successfully"
    ),

    USER_DELETED(
            "User deleted successfully"
    ),

    // =========================
    // Email Verification
    // =========================

    EMAIL_VERIFICATION_OTP_SENT(
            "Email verification OTP sent successfully"
    ),

    EMAIL_VERIFIED(
            "Email verified successfully"
    ),

    // =========================
    // Phone Verification
    // =========================

    PHONE_VERIFICATION_OTP_SENT(
            "Phone verification OTP sent successfully"
    ),

    PHONE_VERIFIED(
            "Phone number verified successfully"
    ),

    // =========================
    // OTP
    // =========================

    OTP_SENT(
            "OTP sent successfully"
    ),

    OTP_VERIFIED(
            "OTP verified successfully"
    ),

    // =========================
    // Password
    // =========================

    PASSWORD_RESET_OTP_SENT(
            "Password reset OTP sent successfully"
    ),

    PASSWORD_RESET_SUCCESS(
            "Password reset successfully"
    ),

    PASSWORD_CHANGED(
            "Password changed successfully"
    ),

    // =========================
    // OAuth2
    // =========================

    OAUTH2_LOGIN_SUCCESS(
            "OAuth2 login successful"
    ),

    OAUTH2_ACCOUNT_LINKED(
            "OAuth2 account linked successfully"
    );

    private final String message;

    ApiSuccessCode(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }
}