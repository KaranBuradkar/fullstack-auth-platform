package com.authplatform.backend.common.exception;

import com.authplatform.backend.common.response.ApiErrorCode;
import org.springframework.security.core.AuthenticationException;

public class ApiAuthenticationException extends AuthenticationException {

    private final ApiErrorCode code;

    public ApiAuthenticationException(ApiErrorCode code) {
        super(code.message());
        this.code = code;
    }

    public ApiAuthenticationException(ApiErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public ApiErrorCode getCode() {
        return code;
    }
}
