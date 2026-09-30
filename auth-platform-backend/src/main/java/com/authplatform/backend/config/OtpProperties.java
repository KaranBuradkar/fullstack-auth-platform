package com.authplatform.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

@ConfigurationProperties(prefix = "application.security.otp")
@Validated
public class OtpProperties {

    private final Long otpExpiration;
    private final SecureRandom random;

    public OtpProperties(String seed, Long otpExpiration) {
        this.otpExpiration = otpExpiration;
        this.random = new SecureRandom(seed.getBytes(StandardCharsets.UTF_8));
    }

    public String generateOtp() {
        return String.format("%06d", random.nextInt(1_000_000));
    }

    public Long getOtpExpiration() {
        return otpExpiration;
    }
}
