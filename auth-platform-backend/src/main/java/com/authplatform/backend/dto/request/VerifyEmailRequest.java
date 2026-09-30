package com.authplatform.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record VerifyEmailRequest(
        @NotBlank(message = "Email is required")
        String email,
        @NotBlank(message = "OTP is required")
        String otp
) {
}
