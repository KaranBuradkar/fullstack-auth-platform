package com.authplatform.backend.dto.response;

public record ErrorResponse(
        String message,
        String error
) {
}
