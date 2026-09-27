package com.authplatform.backend.common.exception;

import com.authplatform.backend.common.response.ApiErrorCode;

public class ApiException extends RuntimeException {

    private final ApiErrorCode code;

    public ApiException(ApiErrorCode code) {
        super(code.message());
        this.code = code;
    }

    public ApiException(ApiErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public ApiErrorCode getCode() {
        return code;
    }
}