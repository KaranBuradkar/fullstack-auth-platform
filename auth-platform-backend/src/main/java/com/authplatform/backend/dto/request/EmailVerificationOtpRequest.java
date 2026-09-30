package com.authplatform.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record EmailVerificationOtpRequest(
        @NotBlank(message = "Email is required") String email
) {
}
