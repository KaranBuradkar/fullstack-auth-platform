package com.authplatform.backend.dto.request;

public record RegisterRequest(
        String fullName,
        String email,
        String password,
        String profilePictureUrl,
        String provider
) {
}
