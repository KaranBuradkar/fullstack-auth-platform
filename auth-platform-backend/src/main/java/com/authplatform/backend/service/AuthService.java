package com.authplatform.backend.service;

import com.authplatform.backend.dto.request.RegisterRequest;
import com.authplatform.backend.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);
}
