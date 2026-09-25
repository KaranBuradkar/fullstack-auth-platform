package com.authplatform.backend.dto.response;

import com.authplatform.backend.entity.enums.AuthProvider;
import com.authplatform.backend.entity.enums.Role;
import com.authplatform.backend.entity.enums.Status;

public record AuthResponse(
        Long id,
        String fullName,
        String email,
        Role role,
        String profilePictureUrl,
        boolean emailVerified,
        Status status,
        AuthProvider provider
) {
}
