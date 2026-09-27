package com.authplatform.backend.exception;

import com.authplatform.backend.common.exception.ApiException;
import com.authplatform.backend.common.response.ApiErrorCode;

public class UserNotFoundException extends ApiException {

    public UserNotFoundException() {
        super(ApiErrorCode.USER_NOT_FOUND);
    }
}
