package com.authplatform.backend.dto.response;

import com.authplatform.backend.entity.enums.AuthProvider;
import com.authplatform.backend.entity.enums.Role;
import com.authplatform.backend.entity.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

public record AuthResponse(

        @Schema(
                description = "User ID",
                example = "121"
        )
        Long id,

        @Schema(
                description = "User Full Name",
                example = "Vijay Waghade"
        )
        String fullName,

        @Schema(
                description = "User email",
                example = "user@example.com"
        )
        String email,

        @Schema(
                description = "User Role",
                example = "CLIENT"
        )
        Role role,

        @Schema(
                description = "User profile picture url",
                example = "https://myimage.example.in/u/sjbvisbv.img"
        )
        String profilePictureUrl,

        @Schema(
                description = "User email is verified or not",
                example = "true"
        )
        boolean emailVerified,

        @Schema(
                description = "User Role",
                example = "CLIENT"
        )
        Status status,

        @Schema(
                description = "User auth provider",
                example = "LOCAL"
        )
        AuthProvider provider,

        @Schema(
                description = "JWT access token",
                example = "evgck.payload.signature"
        )
        String accessToken,

        @Schema(
                description = "JWT refresh token",
                example = "CLIENT"
        )
        String refreshToken,

        @Schema(
                description = "JWT access token expired in",
                example = "2026-10-31T17:09:19.466Z"
        )
        Instant expirationIn,

        @Schema(
                description = "User account created at",
                example = "2026-10-31T17:09:19.466Z"
        )
        Instant createdAt,

        @Schema(
                description = "User account recent updates",
                example = "2026-10-31T17:09:19.466Z"
        )
        Instant updatedAt
) {
}
