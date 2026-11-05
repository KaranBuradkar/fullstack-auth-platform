package com.authplatform.backend.security.otp;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class OtpGenerator {
    private final OtpProperties otpProperties;
    private final SecureRandom secureRandom;

    public OtpGenerator(OtpProperties otpProperties) {
        this.otpProperties = otpProperties;
        this.secureRandom = new SecureRandom();
    }

    public String generateOtp() {
        int bound = (int) Math.pow(10, 6);
        int otp = secureRandom.nextInt(bound);
        return String.format("%06d", otp);
    }

    public Long getOtpExpiration() {
        return otpProperties.otpExpiration();
    }
}
