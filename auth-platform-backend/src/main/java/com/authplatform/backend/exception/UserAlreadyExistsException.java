package com.authplatform.backend.exception;

import com.authplatform.backend.common.exception.ApiException;
import com.authplatform.backend.common.response.ApiErrorCode;

public class UserAlreadyExistsException extends ApiException {

    public UserAlreadyExistsException() {
        super(ApiErrorCode.EMAIL_ALREADY_EXISTS);
    }
}
