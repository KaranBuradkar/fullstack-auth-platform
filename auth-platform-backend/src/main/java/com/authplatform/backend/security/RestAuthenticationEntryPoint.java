package com.authplatform.backend.security;

import com.authplatform.backend.common.exception.ApiAuthenticationException;
import com.authplatform.backend.common.response.ApiErrorCode;
import com.authplatform.backend.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public RestAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        ApiErrorCode errorCode = ApiErrorCode.UNAUTHORIZED;

        if(authException instanceof ApiAuthenticationException exception) {
            errorCode = exception.getCode();
        }

        ApiResponse<Void> apiResponse = ApiResponse.error(
                errorCode.status().value(),
                errorCode.name(),
                errorCode.message(),
                null,
                request.getRequestURI(),
                getRequestId(request)
        );

        response.setStatus(errorCode.status().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        objectMapper.writeValue(response.getOutputStream(), apiResponse);
    }

    private String getRequestId(HttpServletRequest request) {
        Object requestId = request.getAttribute("requestId");
        return requestId != null ? requestId.toString() : null;
    }
}