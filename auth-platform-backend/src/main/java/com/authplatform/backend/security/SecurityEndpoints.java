package com.authplatform.backend.security;

import java.util.Set;

public final class SecurityEndpoints {

    private SecurityEndpoints() {
        // Utility class
    }

    public static final String AUTH_BASE = "/api/v1/auth";

    // Public endpoints in Spring Security
    public static final String[] PUBLIC_URL_PATTERNS = {
            "/api/v1/auth/register",
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            AUTH_BASE + "/password/forgot",
            AUTH_BASE + "/password/reset",
    };

    // Endpoints that do not require JWT authentication
    public static final Set<String> JWT_SKIP_EXACT = Set.of(
            AUTH_BASE + "/login",
            AUTH_BASE + "/register",
            AUTH_BASE + "/refresh",
            AUTH_BASE + "/password/forgot",
            AUTH_BASE + "/password/reset"
    );

    // Endpoints that unverified users may access
    public static final Set<String> UNVERIFIED_USER_ALLOWED = Set.of(
            AUTH_BASE + "/otp/email/send",
            AUTH_BASE + "/otp/email/verify"
    );

    public static boolean shouldSkipJwt(String uri) {
        return JWT_SKIP_EXACT.contains(uri);
    }

    public static boolean canAccessWithoutVerifiedEmail(String uri) {
        return UNVERIFIED_USER_ALLOWED.contains(uri);
    }
}
