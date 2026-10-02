package com.authplatform.backend.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "application.email.resend")
@Validated
public record EmailProperties(
        @NotBlank(message = "api-key is not matched") String apiKey,
        @NotBlank(message = "from-email is not matched") String fromEmail
) {
}
