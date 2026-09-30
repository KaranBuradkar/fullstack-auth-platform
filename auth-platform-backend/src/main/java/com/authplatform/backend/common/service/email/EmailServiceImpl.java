package com.authplatform.backend.common.service.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    public void sendVerificationOtp(String email, String otp) {
        log.info("EMAIL: {}, OTP: {}", email, otp);
    }
}
