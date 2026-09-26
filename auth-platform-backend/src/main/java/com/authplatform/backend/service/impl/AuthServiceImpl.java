package com.authplatform.backend.service.impl;

import com.authplatform.backend.dto.request.LoginRequest;
import com.authplatform.backend.dto.request.RegisterRequest;
import com.authplatform.backend.dto.response.AuthResponse;
import com.authplatform.backend.entity.User;
import com.authplatform.backend.exception.UserAlreadyExistException;
import com.authplatform.backend.mapper.UserMapper;
import com.authplatform.backend.repository.UserRepository;
import com.authplatform.backend.security.JwtService;
import com.authplatform.backend.service.AuthService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthServiceImpl(
            UserRepository userRepository,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager
    ) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    @Override
    public AuthResponse register(RegisterRequest request) {

        // 1. Check if user already exist with same email
        User user = resolveUserByEmail(request.email());
        if (user != null && user.getEmail().equals(request.email())) {
            log.debug("User already exist with {}", request.email());
            throw new UserAlreadyExistException(request.email());
        }

        // 2. Map request -> entity
        user = userMapper.toEntity(request);
        // 3. Hash Password
        user.setPassword(passwordEncoder.encode(request.password()));
        // 4. Save New user account
        User savedUser = userRepository.save(user);

        // 5. Tokens for API authentication
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateNewRefreshToken(user);

        return userMapper.toResponse(savedUser, accessToken, refreshToken);
    }

    @Transactional
    @Override
    public AuthResponse login(LoginRequest request) {

        // 1. Request credentials create local(Temp) user and
        // authenticate by AuthenticationManager of UserDetailsAuthService(implemented)
        // if user authenticate then move forward else throw "userNotFound"
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        // 2. Extract user from authentication
        User authenticUser = (User) authentication.getPrincipal();
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(authenticUser, null, authenticUser.getAuthorities());

        // 3. Set User authenticated in SecurityContextHolder
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        // 4. Tokens for API authentication
        String accessToken = jwtService.generateAccessToken(authenticUser);
        String refreshToken = jwtService.updateRefreshToken(authenticUser);

        return userMapper.toResponse(authenticUser, accessToken, refreshToken);
    }

    private User resolveUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElse(null);
    }
}
