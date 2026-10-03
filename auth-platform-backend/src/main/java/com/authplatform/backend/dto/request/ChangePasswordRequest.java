package com.authplatform.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
        @NotBlank(message = "current email required")
        String currentPassword,

        @NotBlank(message = "new password required")
        String newPassword
) {
}
