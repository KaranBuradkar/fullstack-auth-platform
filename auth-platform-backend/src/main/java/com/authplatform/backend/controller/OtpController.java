package com.authplatform.backend.controller;

import com.authplatform.backend.common.controller.BaseController;
import com.authplatform.backend.common.response.ApiResponse;
import com.authplatform.backend.common.response.ApiSuccessCode;
import com.authplatform.backend.dto.request.EmailVerificationOtpRequest;
import com.authplatform.backend.dto.request.VerifyEmailRequest;
import com.authplatform.backend.service.OtpService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/otp")
public class OtpController extends BaseController {

    private final OtpService otpService;

    public OtpController(
            OtpService otpService,
            HttpServletRequest request
    ) {
        super(request);
        this.otpService = otpService;
    }


    @PostMapping("/email/send")
    public ResponseEntity<ApiResponse<Void>> sendEmail(
            @RequestBody @Valid EmailVerificationOtpRequest request
    ) {
        otpService.sendEmailVerificationOtp(request.email());
        return buildResponse(ApiSuccessCode.EMAIL_VERIFICATION_OTP_SENT,
                null, HttpStatus.OK);
    }

    @PostMapping("/email/verify")
    public ResponseEntity<ApiResponse<Void>> verifyEmailOtp(
            @RequestBody @Valid VerifyEmailRequest request
    ) {
        otpService.verifyEmailVerificationOtp(request);
        return buildResponse(ApiSuccessCode.EMAIL_VERIFIED, null, HttpStatus.OK);
    }

}
