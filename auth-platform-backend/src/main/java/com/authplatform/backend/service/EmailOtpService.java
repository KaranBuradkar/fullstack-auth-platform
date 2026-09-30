package com.authplatform.backend.service;

import com.authplatform.backend.dto.request.VerifyEmailRequest;
import org.springframework.stereotype.Service;

@Service
public interface EmailOtpService {

    void sendVerificationOtp(String email);

    void verifyVerificationOtp(VerifyEmailRequest request);
}
