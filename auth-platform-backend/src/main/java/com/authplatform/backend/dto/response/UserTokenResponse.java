package com.authplatform.backend.dto.response;

import java.time.Instant;

public record UserTokenResponse(
        String accessToken,
        String refreshToken,
        Instant expirationIn
) {
}
