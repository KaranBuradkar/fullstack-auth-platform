package com.authplatform.backend.common.exception;

import com.authplatform.backend.common.response.ApiErrorCode;

public class OtpVerificationException extends ApiException{
    public OtpVerificationException(ApiErrorCode code) {
        super(code);
    }
}
