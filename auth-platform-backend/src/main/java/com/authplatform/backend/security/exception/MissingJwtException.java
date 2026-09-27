package com.authplatform.backend.security.exception;

import com.authplatform.backend.common.exception.ApiException;
import com.authplatform.backend.common.response.ApiErrorCode;

public class MissingJwtException extends ApiException {

    public MissingJwtException() {
        super(ApiErrorCode.TOKEN_MISSING);
    }
}
