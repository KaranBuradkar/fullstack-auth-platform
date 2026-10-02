package com.authplatform.backend.common.exception;

import com.authplatform.backend.common.response.ApiErrorCode;

public class EmailFailedToSendException extends ApiException {
    public EmailFailedToSendException() {
        super(ApiErrorCode.EMAIL_FAILED_TO_SEND);
    }
}
