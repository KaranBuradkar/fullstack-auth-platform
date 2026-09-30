package com.authplatform.backend.common.controller;

import com.authplatform.backend.common.constants.RequestConstants;
import com.authplatform.backend.common.response.ApiResponse;
import com.authplatform.backend.common.response.ApiSuccessCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public abstract class BaseController {

    protected final HttpServletRequest request;

    protected BaseController(HttpServletRequest request) {
        this.request = request;
    }

    /**
     * Build API Response
     * @param code = which task completed and message
     * @param data = return response
     * @param status = HttpStatus code
     * @param <T> = Generic data type for custom data response
     * @return build ResponseEntity instance
     */
    protected <T> ResponseEntity<ApiResponse<T>> buildResponse(
            ApiSuccessCode code, T data,
            HttpStatus status
    ) {
        ApiResponse<T> response = ApiResponse.success(
                status.value(),
                code.name(),
                code.message(),
                data,
                request.getRequestURI(),
                getRequestId()
        );
        return ResponseEntity.status(status).body(response);
    }

    /**
     * Get API Request ID
     * @return requestId
     */
    private String getRequestId() {
        return (String) request.getAttribute(RequestConstants.REQUEST_ID);
    }
}
