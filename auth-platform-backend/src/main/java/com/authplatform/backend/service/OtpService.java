package com.authplatform.backend.service;

import com.authplatform.backend.dto.request.VerifyEmailRequest;
import org.springframework.stereotype.Service;

@Service
public interface OtpService {

    void sendEmailVerificationOtp(String email);

    void verifyEmailVerificationOtp(VerifyEmailRequest request);
}
