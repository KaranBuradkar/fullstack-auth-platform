package com.authplatform.backend.security.exception;

import com.authplatform.backend.common.exception.ApiException;
import com.authplatform.backend.common.response.ApiErrorCode;

public class TokenExpiredException extends ApiException {

    public TokenExpiredException() {
        super(ApiErrorCode.TOKEN_EXPIRED);
    }
}