package com.authplatform.backend.controller;

import com.authplatform.backend.common.constants.RequestConstants;
import com.authplatform.backend.common.response.ApiResponse;
import com.authplatform.backend.common.response.ApiSuccessCode;
import com.authplatform.backend.dto.request.LoginRequest;
import com.authplatform.backend.dto.request.RegisterRequest;
import com.authplatform.backend.dto.response.AuthResponse;
import com.authplatform.backend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final HttpServletRequest request;

    public AuthController(AuthService authService, HttpServletRequest request) {
        this.authService = authService;
        this.request = request;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @RequestBody @Valid RegisterRequest request
    ) {
        AuthResponse response = authService.register(request);
        return buildResponse(ApiSuccessCode.USER_REGISTERED, response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @RequestBody @Valid LoginRequest request
    ) {
        AuthResponse response = authService.login(request);
        return buildResponse(ApiSuccessCode.LOGIN_SUCCESS, response, HttpStatus.OK);
    }

    /**
     * Build API Response
     * @param code = which task completed and message
     * @param data = return response
     * @param status = HttpStatus code
     * @param <T> = Generic data type for custom data response
     * @return build ResponseEntity instance
     */
    private <T> ResponseEntity<ApiResponse<T>> buildResponse(
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

    private String getRequestId() {
        return (String) request.getAttribute(RequestConstants.REQUEST_ID);
    }
}
