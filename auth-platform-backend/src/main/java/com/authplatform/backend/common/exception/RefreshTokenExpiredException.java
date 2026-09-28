package com.authplatform.backend.common.exception;

import com.authplatform.backend.common.response.ApiErrorCode;

public class RefreshTokenExpiredException extends ApiException {

    public RefreshTokenExpiredException() {
        super(ApiErrorCode.REFRESH_TOKEN_EXPIRED);
    }
}
