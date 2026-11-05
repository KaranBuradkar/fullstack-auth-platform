package com.authplatform.backend.security.otp;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "application.security.otp")
@Validated
public record OtpProperties (
        @Min(value = 30) Long otpExpiration,
        @NotBlank String secretKey,
        @Min(value = 1) int maxAttempts
) {
}
