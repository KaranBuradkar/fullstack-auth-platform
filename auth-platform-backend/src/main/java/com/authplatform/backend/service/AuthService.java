package com.authplatform.backend.service;

import com.authplatform.backend.dto.request.LoginRequest;
import com.authplatform.backend.dto.request.RegisterRequest;
import com.authplatform.backend.dto.response.AuthResponse;
import com.authplatform.backend.dto.response.UserTokenResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    UserTokenResponse refreshToken(String token);
}
