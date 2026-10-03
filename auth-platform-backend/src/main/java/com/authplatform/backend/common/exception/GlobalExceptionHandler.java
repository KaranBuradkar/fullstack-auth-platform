package com.authplatform.backend.common.exception;

import com.authplatform.backend.common.constants.RequestConstants;
import com.authplatform.backend.common.response.ApiError;
import com.authplatform.backend.common.response.ApiErrorCode;
import com.authplatform.backend.common.response.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final HttpServletRequest request;

    public GlobalExceptionHandler(HttpServletRequest request) {
        this.request = request;
    }

    private String getRequestId() {
        return (String) request.getAttribute(RequestConstants.REQUEST_ID);
    }

    /**
     * Build Error Response
     * @param code = ApiErrorCode
     * @param errors = list of validation errors
     * @return result response
     */
    private ResponseEntity<ApiResponse<Void>> buildErrorResponse(
            ApiErrorCode code,
            List<ApiError> errors
    ) {
        ApiResponse<Void> response = ApiResponse.error(
                code.status().value(),
                code.name(),
                code.message(),
                errors,
                request.getRequestURI(),
                getRequestId()
        );

        return ResponseEntity
                .status(code.status())
                .body(response);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApiException(
            ApiException ex
    ) {
        log.error("Api Exception, requestId={}", getRequestId(), ex);

        ApiErrorCode code = ex.getCode();

        return ResponseEntity.status(code.status())
                .body(ApiResponse.error(
                        code.status().value(),
                        code.name(),
                        ex.getMessage(),
                        null,
                        request.getRequestURI(),
                        getRequestId()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException ex
    ) {
        log.error("MethodArgumentNotValid Exception, requestId={}", getRequestId(), ex);

        List<ApiError> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ApiError(
                        error.getField(),
                        "INVALID_FIELD",
                        error.getDefaultMessage()
                ))
                .toList();

        ApiErrorCode code = ApiErrorCode.VALIDATION_FAILED;

        return buildErrorResponse(code, errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(
            ConstraintViolationException ex
    ) {
        log.error("Constraint Violation Exception, requestId={}", getRequestId(), ex);

        ApiErrorCode code = ApiErrorCode.VALIDATION_FAILED;

        return buildErrorResponse(code, null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleMalformedRequest(
            HttpMessageNotReadableException ex
    ) {
        log.error("Http Message Not Readable, requestId={}", getRequestId(), ex);

        ApiErrorCode code = ApiErrorCode.INVALID_REQUEST;

        return buildErrorResponse(code, null);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(
            AuthenticationException ex
    ) {
        log.error("Authentication Exception, requestId={}", getRequestId(), ex);

        ApiErrorCode code = ApiErrorCode.INVALID_CREDENTIALS;

        return buildErrorResponse(code, null);
    }

    @ExceptionHandler(ServletException.class)
    public ResponseEntity<ApiResponse<Void>> handleServletException(
            ServletException ex
    ) {
        log.error("Servlet Exception, requestId={}", getRequestId(), ex);

        ApiErrorCode code = ApiErrorCode.INVALID_REQUEST;
        if (ex instanceof NoResourceFoundException){
            code = ApiErrorCode.RESOURCE_NOT_FOUND;
        }

        return buildErrorResponse(code, null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(
            DataIntegrityViolationException ex
    ) {
        log.error("Data Integrity Violation, requestId={}", getRequestId(), ex);

        ApiErrorCode code = ApiErrorCode.DATABASE_CONSTRAINT_VIOLATION;

        return buildErrorResponse(code, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(
            Exception ex
    ) {
        log.error(
                "Unhandled exception. requestId={}",
                getRequestId(),
                ex
        );

        ApiErrorCode code = ApiErrorCode.INTERNAL_SERVER_ERROR;

        return buildErrorResponse(code, null);
    }
}
