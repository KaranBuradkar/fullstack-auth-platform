package com.authplatform.backend.common.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = "Standard API response")
public record ApiResponse<T>(
        @Schema(description = "Indicates request success")
        boolean success,
        @Schema(description = "Response timestamp")
        Instant timestamp,
        @Schema(description = "HTTP status code")
        int status,
        @Schema(description = "Application response code")
        String code,
        @Schema(description = "Response message")
        String message,
        @Schema(description = "Response payload")
        T data,
        @Schema(description = "Error details")
        List<ApiError> errors,
        @Schema(description = "Request path")
        String path,
        @Schema(description = "Unique request identifier")
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
