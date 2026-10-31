package com.authplatform.backend.dto.request;

import com.authplatform.backend.entity.enums.AuthProvider;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(

        @Schema(
                description = "Registered user email",
                example = "user@example.com",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @Email
        @NotBlank(message = "Email is required")
        String email,

        @Schema(
                description = "Registered user password",
                example = "Pass@123",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Password is required")
        String password,

        @Schema(
                description = "Auth Provider",
                example = "LOCAL",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Provider is required")
        AuthProvider provider
) {
}
