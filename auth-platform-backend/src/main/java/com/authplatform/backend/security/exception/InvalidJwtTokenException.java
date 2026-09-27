package com.authplatform.backend.security.exception;

import com.authplatform.backend.common.exception.ApiException;
import com.authplatform.backend.common.response.ApiErrorCode;

public class InvalidJwtTokenException extends ApiException {

    public InvalidJwtTokenException() {
        super(ApiErrorCode.INVALID_TOKEN);
    }
}
