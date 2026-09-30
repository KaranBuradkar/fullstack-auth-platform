package com.authplatform.backend.service.impl;

import com.authplatform.backend.dto.request.VerifyEmailRequest;
import com.authplatform.backend.service.EmailOtpService;
import com.authplatform.backend.service.OtpService;
import org.springframework.stereotype.Service;

@Service
public class OtpServiceImpl implements OtpService {

    private final EmailOtpService emailOtpService;

    public OtpServiceImpl(EmailOtpService emailOtpService) {
        this.emailOtpService = emailOtpService;
    }

    @Override
    public void sendEmailVerificationOtp(String email) {
        emailOtpService.sendVerificationOtp(email);
    }

    @Override
    public void verifyEmailVerificationOtp(VerifyEmailRequest request) {
        emailOtpService.verifyVerificationOtp(request);
    }
}
