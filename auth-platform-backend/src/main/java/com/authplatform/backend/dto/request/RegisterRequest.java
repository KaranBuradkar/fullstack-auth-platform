package com.authplatform.backend.dto.request;

import com.authplatform.backend.entity.enums.AuthProvider;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterRequest(

        @Schema(
                description = "User full name",
                example = "Vijay Waghade",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Full Name is required")
        String fullName,

        @Schema(
                description = "User email",
                example = "user@example.com",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @Email
        @NotBlank(message = "Email is required")
        String email,

        @Schema(
                description = "User password",
                example = "Password@123",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Password is required")
        String password,

        @Schema(
                description = "User profile picture url",
                example = "https://myimage.example.in/u/sjbvisbv.img"
        )
        String profilePictureUrl,

        @Schema(
                description = "Auth provider",
                example = "LOCAL"
        )
        @NotNull(message = "Provider is required")
        AuthProvider provider
) {
}
