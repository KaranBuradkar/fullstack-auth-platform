package com.authplatform.backend.doc;

import com.authplatform.backend.config.BackendProperties;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    private final BackendProperties backendProperties;

    private static final String SECURITY_SCHEME =
            "Bearer Authentication";

    public OpenApiConfig(BackendProperties backendProperties) {
        this.backendProperties = backendProperties;
    }

    @Bean
    public OpenAPI customOpenAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("Auth Platform API")
                        .description("""
                                REST API documentation for Auth Platform.

                                Features:
                                - User authentication
                                - JWT access and refresh tokens
                                - Email OTP verification
                                - Password management
                                - User profile management
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Auth Platform Team")
                        )
                )
                .servers(List.of(
                        new Server()
                                .url(backendProperties.url())
                                .description("Development Server")
                ))
                .components(new Components()
                        .addSecuritySchemes(
                                SECURITY_SCHEME,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .in(SecurityScheme.In.HEADER)
                        )
                );
    }
}
