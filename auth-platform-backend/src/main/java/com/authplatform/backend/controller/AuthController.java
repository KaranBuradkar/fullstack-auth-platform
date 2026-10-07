package com.authplatform.backend.controller;

import com.authplatform.backend.common.controller.BaseController;
import com.authplatform.backend.common.response.ApiResponse;
import com.authplatform.backend.common.response.ApiSuccessCode;
import com.authplatform.backend.dto.request.LoginRequest;
import com.authplatform.backend.dto.request.RefreshTokenRequest;
import com.authplatform.backend.dto.request.RegisterRequest;
import com.authplatform.backend.dto.response.AuthResponse;
import com.authplatform.backend.dto.response.UserTokenResponse;
import com.authplatform.backend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController extends BaseController {

    private final AuthService authService;

    public AuthController(AuthService authService, HttpServletRequest request) {
        super(request);
        this.authService = authService;
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

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<UserTokenResponse>> refreshToken(
            @RequestBody @Valid RefreshTokenRequest request
    ) {
        UserTokenResponse response = authService.refreshToken(request.refreshToken());
        return buildResponse(ApiSuccessCode.TOKEN_REFRESHED, response, HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            Authentication authentication
    ) {
        authService.logout(authentication.getName());
        return buildResponse(ApiSuccessCode.LOGOUT_SUCCESS, null, HttpStatus.OK);
    }

}
