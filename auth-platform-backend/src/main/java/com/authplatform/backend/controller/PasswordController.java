package com.authplatform.backend.controller;

import com.authplatform.backend.common.controller.BaseController;
import com.authplatform.backend.common.response.ApiResponse;
import com.authplatform.backend.common.response.ApiSuccessCode;
import com.authplatform.backend.dto.request.ChangePasswordRequest;
import com.authplatform.backend.dto.request.ForgotPasswordRequest;
import com.authplatform.backend.dto.request.ResetPasswordRequest;
import com.authplatform.backend.entity.User;
import com.authplatform.backend.exception.UserNotFoundException;
import com.authplatform.backend.repository.UserRepository;
import com.authplatform.backend.service.PasswordService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/password")
public class PasswordController extends BaseController {

    private final PasswordService passwordService;

    public PasswordController(
            PasswordService passwordService,
            HttpServletRequest request
    ) {
        super(request);
        this.passwordService = passwordService;
    }

    @PostMapping("/forgot")
    public ResponseEntity<ApiResponse<Void>> forgot(
            @RequestBody @Valid ForgotPasswordRequest request
    ) {
        passwordService.forgotPassword(request);
        return buildResponse(ApiSuccessCode.PASSWORD_RESET_OTP_SENT, null, HttpStatus.OK);
    }

    @PostMapping("/reset")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        passwordService.resetPassword(request);

        return buildResponse(ApiSuccessCode.PASSWORD_RESET_SUCCESS, null, HttpStatus.OK);
    }

    @PostMapping("/change")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {

        passwordService.changePassword(request);

        return buildResponse(ApiSuccessCode.PASSWORD_CHANGED, null, HttpStatus.OK);
    }
}
