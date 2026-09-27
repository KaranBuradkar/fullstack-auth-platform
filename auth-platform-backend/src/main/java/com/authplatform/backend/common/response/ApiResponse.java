package com.authplatform.backend.common.response;

import java.time.Instant;
import java.util.List;

public record ApiResponse<T>(
        boolean success,
        Instant timestamp,
        int status,
        String code,
        String message,
        T data,
        List<ApiError> errors,
        String path,
        String requestId
) {

    public static <T>ApiResponse<T> success(
            int status,
            String code,
            String message,
            T data,
            String path,
            String requestId
    ) {
        return new ApiResponse<>(
                true,
                Instant.now(),
                status,
                code,
                message,
                data,
                null,
                path,
                requestId
        );
    }

    public static ApiResponse<Void> error(
            int status,
            String code,
            String message,
            List<ApiError> errors,
            String path,
            String requestId
    ) {
        return new ApiResponse<>(
                false,
                Instant.now(),
                status,
                code,
                message,
                null,
                errors,
                path,
                requestId
        );
    }
}
