package com.authplatform.backend.common.exception;

import com.authplatform.backend.common.response.ApiErrorCode;

public class BadRequestException extends ApiException{
    public BadRequestException() {
        super(ApiErrorCode.BAD_REQUEST);
    }

    public BadRequestException(String message) {
        super(ApiErrorCode.BAD_REQUEST, message);
    }
}
