package com.authplatform.backend.common.response;

public record ApiError(
        String filed,
        String code,
        String message
) {
}
