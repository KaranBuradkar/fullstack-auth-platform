package com.authplatform.backend.security;

import java.util.Set;

public final class SecurityEndpoints {

    private SecurityEndpoints() {
        // Utility class
    }

    public static final String AUTH_BASE = "/api/v1/auth";

    // Public endpoints in Spring Security
    public static final String[] PUBLIC_URL_PATTERNS = {
            // Root
            "/",

            // Authentication
            AUTH_BASE + "/register",
            AUTH_BASE + "/login",
            AUTH_BASE + "/refresh",

            // Password management
            AUTH_BASE + "/password/forgot",
            AUTH_BASE + "/password/reset",

            // API documentation
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**"
    };

    // Endpoints that do not require JWT authentication
    public static final Set<String> JWT_SKIP_EXACT = Set.of(
            AUTH_BASE + "/login",
            AUTH_BASE + "/register",
            AUTH_BASE + "/refresh",
            AUTH_BASE + "/password/forgot",
            AUTH_BASE + "/password/reset",

            // API documentation
            "/swagger-ui",
            "/swagger-ui/index.html",
            "/v3/api-docs/**"
    );

    // Endpoints that unverified users may access
    public static final Set<String> UNVERIFIED_USER_ALLOWED = Set.of(
            AUTH_BASE + "/otp/email/send",
            AUTH_BASE + "/otp/email/verify"
    );

    public static boolean shouldSkipJwt(String uri) {
        return JWT_SKIP_EXACT.contains(uri);
    }

    public static boolean startsWith(String uri) {
        return uri.startsWith("/swagger-ui") ||
                uri.startsWith("/v3/api-docs") ||
                uri.startsWith("/swagger-resources") ||
                uri.startsWith("/webjars") ||
                uri.startsWith("/v3/api-docs/swagger-config");
    }

    public static boolean canAccessWithoutVerifiedEmail(String uri) {
        return UNVERIFIED_USER_ALLOWED.contains(uri);
    }
}
