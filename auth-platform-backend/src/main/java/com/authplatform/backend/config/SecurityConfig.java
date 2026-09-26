package com.authplatform.backend.config;

import com.authplatform.backend.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
public class SecurityConfig {

    private final HandlerExceptionResolver handlerExceptionResolver;
    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(
            HandlerExceptionResolver handlerExceptionResolver,
            JwtAuthFilter jwtAuthFilter
    ) {
        this.handlerExceptionResolver = handlerExceptionResolver;
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception{
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session
                        -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Request Validator
                .authorizeHttpRequests(req -> {
                    req.requestMatchers(shouldSkipUrl())
                            .permitAll()
                            .anyRequest().authenticated();
                })
                // Token validation filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                // any occurred exception handler
                .exceptionHandling(this::resolveConfigure);

        return httpSecurity.build();
    }

    private void resolveConfigure(ExceptionHandlingConfigurer<HttpSecurity> configurer) {
        configurer.accessDeniedHandler(
                (request, response, e)
                        -> handlerExceptionResolver.resolveException(request, response, null, e)
        );
    }

    private String[] shouldSkipUrl() {
        return new String[]{
                "/api/v1/auth/register/**",
                "/api/v1/auth/login/**"
        };
    }
}
