package com.authplatform.backend.security;

import com.authplatform.backend.common.response.ApiErrorCode;
import com.authplatform.backend.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public RestAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {

        ApiErrorCode errorCode = ApiErrorCode.FORBIDDEN;

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