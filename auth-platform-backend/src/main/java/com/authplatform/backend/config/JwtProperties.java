package com.authplatform.backend.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "application.security.jwt")
@Validated
public record JwtProperties(
        @NotBlank String secretKey,
        @Min(value = 60000) Long accessTokenExpiration,
        @Min(value = 360000) Long refreshTokenExpiration
) {}
