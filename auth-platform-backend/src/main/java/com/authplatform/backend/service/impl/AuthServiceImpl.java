package com.authplatform.backend.service.impl;

import com.authplatform.backend.common.exception.ApiException;
import com.authplatform.backend.common.exception.RefreshTokenExpiredException;
import com.authplatform.backend.common.exception.RefreshTokenRevokedException;
import com.authplatform.backend.common.response.ApiErrorCode;
import com.authplatform.backend.dto.request.LoginRequest;
import com.authplatform.backend.dto.request.RegisterRequest;
import com.authplatform.backend.dto.response.AuthResponse;
import com.authplatform.backend.dto.response.UserTokenResponse;
import com.authplatform.backend.entity.User;
import com.authplatform.backend.entity.UserToken;
import com.authplatform.backend.exception.UserAlreadyExistsException;
import com.authplatform.backend.exception.UserTokenNotFoundException;
import com.authplatform.backend.mapper.UserMapper;
import com.authplatform.backend.repository.UserRepository;
import com.authplatform.backend.repository.UserTokenRepository;
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

import java.time.Instant;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserTokenRepository userTokenRepository;

    public AuthServiceImpl(
            UserRepository userRepository,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager,
            UserTokenRepository userTokenRepository) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userTokenRepository = userTokenRepository;
    }

    @Transactional
    @Override
    public AuthResponse register(RegisterRequest request) {

        log.atInfo()
                .addKeyValue("event", "USER_REGISTRATION_STARTED")
                .log("User registration started");

        // 1. Check if user already exist with same email
        User user = resolveUserByEmail(request.email());
        if (user != null && user.getEmail().equals(request.email())) {
            log.atWarn()
                    .addKeyValue("event", "USER_REGISTRATION_FAILED")
                    .addKeyValue("reason", "EMAIL_ALREADY_EXISTS")
                    .log("User registration rejected");
            throw new UserAlreadyExistsException();
        }

        // 2. Map request -> entity
        user = userMapper.toEntity(request);
        // 3. Hash Password
        user.setPassword(passwordEncoder.encode(request.password()));
        // 4. Save New user account
        User savedUser = userRepository.save(user);

        // 5. Tokens for API authentication
        String accessToken = jwtService.generateAccessToken(savedUser);
        String refreshToken = jwtService.generateNewRefreshToken(savedUser);
        Instant expiryDate = jwtService.getExpiry(refreshToken).toInstant();

        // 6. Save User Refresh Token
        UserToken userToken = new UserToken(refreshToken, savedUser, expiryDate);
        UserToken saveUserToken = userTokenRepository.save(userToken);

        log.atInfo()
                .addKeyValue("event", "USER_REGISTRATION_SUCCESS")
                .addKeyValue("userId", savedUser.getId())
                .log("User registered successfully");

        return userMapper.toResponse(
                savedUser,
                accessToken,
                userToken.getRefreshToken(),
                saveUserToken.getExpiryDate()
        );
    }

    @Transactional
    @Override
    public AuthResponse login(LoginRequest request) {

        log.atInfo()
                .addKeyValue("event", "USER_LOGIN_STARTED")
                .log("User Login started");

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
        if (authenticUser == null) throw new ApiException(ApiErrorCode.UNAUTHORIZED);
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(
                        authenticUser,
                        null,
                        authenticUser.getAuthorities()
                );

        // 3. Set User authenticated in SecurityContextHolder
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        // 4. Tokens for API authentication
        String accessToken = jwtService.generateAccessToken(authenticUser);
        String refreshToken = jwtService.generateNewRefreshToken(authenticUser);
        Instant expiryDate = jwtService.getExpiry(refreshToken).toInstant();

        // 5. Update User Refresh Token
        UserToken userToken = userTokenRepository
                .findByUser(authenticUser)
                .orElseThrow(UserTokenNotFoundException::new);

        userToken.setRefreshToken(refreshToken);
        userToken.setExpiryDate(expiryDate);
        userToken.setRevoked(false);
        UserToken saveUserToken = userTokenRepository.save(userToken);

        log.atInfo()
                .addKeyValue("event", "USER_LOGIN_SUCCESS")
                .addKeyValue("userId", authenticUser.getId())
                .log("User login successfully");

        return userMapper.toResponse(
                authenticUser,
                accessToken,
                userToken.getRefreshToken(),
                saveUserToken.getExpiryDate()
        );
    }

    @Transactional
    @Override
    public UserTokenResponse refreshToken(String token) {

        // 1. Verify userToken already exist
        UserToken userToken = verifyRefreshToken(token);

        // 2. Fetch User
        User user = userToken.getUser();

        // 3. Generate Resource of UserToken
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateNewRefreshToken(user);
        Instant expiryDate = jwtService.getExpiry(refreshToken).toInstant();

        // 4. Save User Refresh Token
        userToken.setRefreshToken(refreshToken);
        userToken.setExpiryDate(expiryDate);
        userToken.setRevoked(false);
        UserToken saveUserToken = userTokenRepository.save(userToken);

        return new UserTokenResponse(
                accessToken,
                saveUserToken.getRefreshToken(),
                saveUserToken.getExpiryDate()
        );
    }

    @Override
    public void logout(String name) {
        User user = userRepository.findByEmail(name)
                .orElseThrow(() -> new ApiException(ApiErrorCode.EMAIL_NOT_FOUND));
        UserToken userToken = userTokenRepository.findByUser(user)
                .orElseThrow(UserTokenNotFoundException::new);

        userToken.setRevoked(true);
        userToken.setExpiryDate(Instant.now());

        userTokenRepository.save(userToken);
    }

    /**
     * Verify JWT token is revoked, expired or not
     * @param token JWT Refresh Token
     * @return UserToken entity
     */
    private UserToken verifyRefreshToken(String token) {
        UserToken refreshToken = userTokenRepository
                .findByRefreshToken(token)
                .orElseThrow(UserTokenNotFoundException::new);

        if(refreshToken.isRevoked()) {
            throw new RefreshTokenRevokedException();
        }

        if(refreshToken.getExpiryDate().isBefore(Instant.now())) {
            throw new RefreshTokenExpiredException();
        }

        return refreshToken;
    }

    private User resolveUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElse(null);
    }
}
