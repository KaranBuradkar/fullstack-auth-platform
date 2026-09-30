package com.authplatform.backend.common.service.email;

import org.springframework.stereotype.Service;

@Service
public interface EmailService {

    void sendVerificationOtp(String email, String otp);
}
