package com.authplatform.backend.common.exception;

import com.authplatform.backend.common.response.ApiErrorCode;

public class RefreshTokenRevokedException extends ApiException {
    public RefreshTokenRevokedException() {
        super(ApiErrorCode.REFRESH_TOKEN_REVOKED);
    }
}
