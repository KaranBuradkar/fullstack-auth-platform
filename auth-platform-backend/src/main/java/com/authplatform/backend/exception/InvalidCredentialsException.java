package com.authplatform.backend.exception;

import com.authplatform.backend.common.exception.ApiException;
import com.authplatform.backend.common.response.ApiErrorCode;

public class InvalidCredentialsException extends ApiException {

    public InvalidCredentialsException() {
        super(ApiErrorCode.INVALID_CREDENTIALS);
    }
}
