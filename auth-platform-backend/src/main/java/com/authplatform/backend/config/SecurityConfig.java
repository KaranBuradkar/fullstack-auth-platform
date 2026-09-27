package com.authplatform.backend.config;

import com.authplatform.backend.common.filter.RequestIdFilter;
import com.authplatform.backend.security.JwtAuthFilter;
import com.authplatform.backend.security.RestAccessDeniedHandler;
import com.authplatform.backend.security.RestAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final RequestIdFilter requestIdFilter;
    private final RestAccessDeniedHandler restAccessDeniedHandler;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    public SecurityConfig(
            HandlerExceptionResolver handlerExceptionResolver,
            RestAuthenticationEntryPoint restAuthenticationEntryPoint,
            RestAccessDeniedHandler restAccessDeniedHandler,
            JwtAuthFilter jwtAuthFilter, RequestIdFilter requestIdFilter
    ) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
        this.restAccessDeniedHandler = restAccessDeniedHandler;
        this.requestIdFilter = requestIdFilter;
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
                // Generate Request ID Filter
                .addFilterBefore(requestIdFilter, UsernamePasswordAuthenticationFilter.class)
                // Token validation filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                // any occurred exception handler
                .exceptionHandling(exception ->
                        exception
                                .authenticationEntryPoint(restAuthenticationEntryPoint)
                                .accessDeniedHandler(restAccessDeniedHandler)
                );

        return httpSecurity.build();
    }

    private String[] shouldSkipUrl() {
        return new String[]{
                "/api/v1/auth/register/**",
                "/api/v1/auth/login/**"
        };
    }
}
