package com.authplatform.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "application.backend")
@Validated
public record BackendProperties(
        String url
) {
}
