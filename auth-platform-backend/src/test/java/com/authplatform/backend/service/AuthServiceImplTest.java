package com.authplatform.backend.service;

import com.authplatform.backend.common.exception.ApiException;
import com.authplatform.backend.dto.request.LoginRequest;
import com.authplatform.backend.dto.request.RegisterRequest;
import com.authplatform.backend.dto.response.AuthResponse;
import com.authplatform.backend.entity.User;
import com.authplatform.backend.entity.UserToken;
import com.authplatform.backend.exception.UserAlreadyExistsException;
import com.authplatform.backend.exception.UserTokenNotFoundException;
import com.authplatform.backend.mapper.UserMapper;
import com.authplatform.backend.repository.UserRepository;
import com.authplatform.backend.repository.UserTokenRepository;
import com.authplatform.backend.security.JwtService;

import com.authplatform.backend.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthServiceImpl.
 * Tests registration and authentication business logic independently using Mockito
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImplUnit Tests")
@MockitoSettings(strictness = Strictness.LENIENT)
public class AuthServiceImplTest {

    // Dependencies
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserTokenRepository userTokenRepository;

    // Service under test
    @InjectMocks
    private AuthServiceImpl authService;

    // 1. REGISTER TESTS
    @Nested
    @DisplayName("register()")
    class RegisterTests {

        // Test data
        private RegisterRequest request;
        private User user;
        private User savedUser;
        private UserToken savedUserToken;
        private AuthResponse expectedResponse;

        private static final String EMAIL = "user@dev.com";
        private static final String RAW_PASSWORD = "Password@123";
        private static final String ENCODED_PASSWORD = "$2a$12$encodedPassword";

        private static final String ACCESS_TOKEN = "access-token";
        private static final String REFRESH_TOKEN = "refresh-token";

        private static final Instant EXPIRY_DATE =
                Instant.parse("2027-01-01T00:00:00Z");

        @BeforeEach
        void setUp() {

            request = mock(RegisterRequest.class);

            when(request.email()).thenReturn(EMAIL);
            when(request.password()).thenReturn(RAW_PASSWORD);

            user = new User();

            savedUser = mock(User.class);
            savedUserToken = mock(UserToken.class);
            expectedResponse = mock(AuthResponse.class);

            when(userMapper.toEntity(request)).thenReturn(user);

            when(passwordEncoder.encode(RAW_PASSWORD))
                    .thenReturn(ENCODED_PASSWORD);

            when(userRepository.save(user))
                    .thenReturn(savedUser);

            when(jwtService.generateAccessToken(savedUser))
                    .thenReturn(ACCESS_TOKEN);

            when(jwtService.generateNewRefreshToken(savedUser))
                    .thenReturn(REFRESH_TOKEN);

            when(jwtService.getExpiry(REFRESH_TOKEN))
                    .thenReturn(Date.from(EXPIRY_DATE));

            when(userTokenRepository.save(any(UserToken.class)))
                    .thenReturn(savedUserToken);

            when(savedUserToken.getRefreshToken())
                    .thenReturn(REFRESH_TOKEN);

            when(savedUserToken.getExpiryDate())
                    .thenReturn(EXPIRY_DATE);

            when(userMapper.toResponse(
                    savedUser,
                    ACCESS_TOKEN,
                    REFRESH_TOKEN,
                    EXPIRY_DATE
            )).thenReturn(expectedResponse);
        }

        @Test
        @DisplayName("Should register user successfully")
        void register_shouldRegisterUserSuccessfully() {

            when(userRepository.findByEmail(EMAIL))
                    .thenReturn(Optional.empty());

            AuthResponse response = authService.register(request);

            assertThat(response).isNotNull();
            assertThat(response).isSameAs(expectedResponse);

            verify(userRepository).findByEmail(EMAIL);
            verify(userMapper).toEntity(request);
            verify(passwordEncoder).encode(RAW_PASSWORD);
            verify(userRepository).save(user);

            verify(jwtService).generateAccessToken(savedUser);
            verify(jwtService).generateNewRefreshToken(savedUser);
            verify(jwtService).getExpiry(REFRESH_TOKEN);

            verify(userTokenRepository).save(any(UserToken.class));

            verify(userMapper).toResponse(
                    savedUser,
                    ACCESS_TOKEN,
                    REFRESH_TOKEN,
                    EXPIRY_DATE
            );
        }

        @Test
        @DisplayName("Should code password before saving user")
        void register_shouldEncodePasswordBeforeSavingUser() {

            when(userRepository.findByEmail(EMAIL))
                    .thenReturn(Optional.empty());

            InOrder inOrder = inOrder(passwordEncoder, userRepository);

            authService.register(request);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

            inOrder.verify(passwordEncoder).encode(RAW_PASSWORD);

            inOrder.verify(userRepository).save(captor.capture());

            assertThat(captor.getValue().getPassword())
                    .isEqualTo(ENCODED_PASSWORD);
        }

        @Test
        @DisplayName("Should never store the raw password")
        void register_shouldNeverStoreRawPassword() {

            when(userRepository.findByEmail(EMAIL))
                    .thenReturn(Optional.empty());

            when(passwordEncoder.encode(RAW_PASSWORD))
                    .thenReturn(ENCODED_PASSWORD);

            authService.register(request);

            ArgumentCaptor<User> captor =
                    ArgumentCaptor.forClass(User.class);

            verify(userRepository).save(captor.capture());

            User capturedUser = captor.getValue();

            assertThat(capturedUser.getPassword())
                    .isNotNull()
                    .isEqualTo(ENCODED_PASSWORD)
                    .isNotEqualTo(RAW_PASSWORD);
        }

        @Test
        @DisplayName("Should generate access and refresh tokens")
        void register_shouldGenerateAccessAndRefreshTokens() {

            when(userRepository.findByEmail(EMAIL))
                    .thenReturn(Optional.empty());

            authService.register(request);

            verify(jwtService, times(1))
                    .generateAccessToken(savedUser);

            verify(jwtService, times(1))
                    .generateNewRefreshToken(savedUser);
        }

        @Test
        @DisplayName("Should save refresh token with correct details")
        void register_shouldSaveRefreshTokenWithCorrectDetails() {

            when(userRepository.findByEmail(EMAIL))
                    .thenReturn(Optional.empty());

            authService.register(request);

            ArgumentCaptor<UserToken> captor =
                    ArgumentCaptor.forClass(UserToken.class);

            verify(userTokenRepository).save(captor.capture());

            UserToken capturedToken = captor.getValue();

            assertThat(capturedToken.getRefreshToken())
                    .isEqualTo(REFRESH_TOKEN);

            assertThat(capturedToken.getUser())
                    .isSameAs(savedUser);

            assertThat(capturedToken.getExpiryDate())
                    .isEqualTo(EXPIRY_DATE);
        }

        @Test
        @DisplayName("Should use expiry returned by jwtService")
        void register_shouldUseExpiryReturnedByJwtService() {

            Instant customExpiry =
                    Instant.parse("2027-06-01T00:00:00Z");

            when(userRepository.findByEmail(EMAIL))
                    .thenReturn(Optional.empty());

            when(jwtService.getExpiry(REFRESH_TOKEN))
                    .thenReturn(Date.from(customExpiry));

            authService.register(request);

            ArgumentCaptor<UserToken> captor =
                    ArgumentCaptor.forClass(UserToken.class);

            verify(userTokenRepository).save(captor.capture());

            assertThat(captor.getValue().getExpiryDate())
                    .isEqualTo(customExpiry);
        }

        @Test
        @DisplayName("Should return response from userMapper")
        void register_shouldReturnResponseFromUserMapper() {

            when(userRepository.findByEmail(EMAIL))
                    .thenReturn(Optional.empty());

            AuthResponse response = authService.register(request);

            assertThat(response).isSameAs(expectedResponse);

            verify(userMapper).toResponse(
                    savedUser,
                    ACCESS_TOKEN,
                    REFRESH_TOKEN,
                    EXPIRY_DATE
            );
        }


        @Test
        @DisplayName("Should throw exception when email already exists")
        void register_shouldThrowExceptionWhenEmailAlreadyExists() {

            User existingUser = mock(User.class);

            when(existingUser.getEmail()).thenReturn(EMAIL);

            when(userRepository.findByEmail(EMAIL))
                    .thenReturn(Optional.of(existingUser));

            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(UserAlreadyExistsException.class);

            verify(userRepository).findByEmail(EMAIL);

            verify(userMapper, never()).toEntity(any());
            verify(passwordEncoder, never()).encode(anyString());
            verify(userRepository, never()).save(any());

            verifyNoInteractions(
                    jwtService,
                    userTokenRepository
            );
        }

        @Test
        @DisplayName("Should not generate tokens when email already exists")
        void register_shouldNotGenerateTokensWhenEmailExists() {

            User existingUser = mock(User.class);

            when(existingUser.getEmail()).thenReturn(EMAIL);

            when(userRepository.findByEmail(EMAIL))
                    .thenReturn(Optional.of(existingUser));

            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(UserAlreadyExistsException.class);

            verify(jwtService, never()).generateAccessToken(any());
            verify(jwtService, never()).generateNewRefreshToken(any());
            verify(jwtService, never()).getExpiry(anyString());

            verify(userTokenRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should continue when repository returns empty optional")
        void register_shouldContinueWhenRepositoryReturnsEmptyOptional() {

            when(userRepository.findByEmail(EMAIL))
                    .thenReturn(Optional.empty());

            AuthResponse response = authService.register(request);

            assertThat(response).isSameAs(expectedResponse);

            verify(userMapper).toEntity(request);
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("Should continue when returned user email does not match request")
        void register_shouldContinueWhenReturnedUserEmailDoesNotMatchRequest() {

            User differentUser = mock(User.class);

            when(differentUser.getEmail())
                    .thenReturn("different@example.com");

            when(userRepository.findByEmail(EMAIL))
                    .thenReturn(Optional.of(differentUser));

            AuthResponse response = authService.register(request);

            assertThat(response).isSameAs(expectedResponse);

            verify(userMapper).toEntity(request);
            verify(userRepository).save(user);
        }


        @Test
        @DisplayName("Should propagate exception when email lookup fails")
        void register_shouldPropagateExceptionWhenEmailLookupFails() {

            RuntimeException exception =
                    new RuntimeException("Database unavailable");

            when(userRepository.findByEmail(EMAIL))
                    .thenThrow(exception);

            assertThatThrownBy(() -> authService.register(request))
                    .isSameAs(exception);

            verify(userMapper, never()).toEntity(any());
            verify(passwordEncoder, never()).encode(anyString());
            verify(userRepository, never()).save(any());

            verifyNoInteractions(jwtService, userTokenRepository);
        }

        @Test
        @DisplayName("Should stops when entity mapping fails")
        void register_shouldStopWhenEntityMappingFails() {

            RuntimeException exception =
                    new RuntimeException("Mapping failed");

            when(userRepository.findByEmail(EMAIL))
                    .thenReturn(Optional.empty());

            when(userMapper.toEntity(request))
                    .thenThrow(exception);

            assertThatThrownBy(() -> authService.register(request))
                    .isSameAs(exception);

            verify(passwordEncoder, never()).encode(anyString());
            verify(userRepository, never()).save(any());

            verifyNoInteractions(jwtService, userTokenRepository);
        }

        @Test
        @DisplayName("Should stops when password encoding fails")
        void register_shouldStopWhenPasswordEncodingFails() {

            RuntimeException exception =
                    new RuntimeException("Password encoding failed");

            when(userRepository.findByEmail(EMAIL))
                    .thenReturn(Optional.empty());

            when(passwordEncoder.encode(RAW_PASSWORD))
                    .thenThrow(exception);

            assertThatThrownBy(() -> authService.register(request))
                    .isSameAs(exception);

            verify(userRepository, never()).save(any());

            verifyNoInteractions(jwtService, userTokenRepository);
        }

        @Test
        @DisplayName("Should stops when user persistence fails")
        void register_shouldStopWhenUserPersistenceFails() {

            RuntimeException exception =
                    new RuntimeException("User persistence failed");

            when(userRepository.findByEmail(EMAIL))
                    .thenReturn(Optional.empty());

            when(userRepository.save(user))
                    .thenThrow(exception);

            assertThatThrownBy(() -> authService.register(request))
                    .isSameAs(exception);

            verify(jwtService, never()).generateAccessToken(any());
            verify(jwtService, never()).generateNewRefreshToken(any());
            verify(jwtService, never()).getExpiry(anyString());

            verifyNoInteractions(userTokenRepository);
        }

        @Test
        @DisplayName("Should stops when access token generation fails")
        void register_shouldStopWhenAccessTokenGenerationFails() {

            RuntimeException exception =
                    new RuntimeException("Access token generation failed");

            when(userRepository.findByEmail(EMAIL))
                    .thenReturn(Optional.empty());

            when(jwtService.generateAccessToken(savedUser))
                    .thenThrow(exception);

            assertThatThrownBy(() -> authService.register(request))
                    .isSameAs(exception);

            verify(jwtService, never()).generateNewRefreshToken(any());
            verify(jwtService, never()).getExpiry(anyString());

            verifyNoInteractions(userTokenRepository);
        }

        @Test
        @DisplayName("Should stops when refresh token generation fails")
        void register_shouldStopWhenRefreshTokenGenerationFails() {

            RuntimeException exception =
                    new RuntimeException("Refresh token generation failed");

            when(userRepository.findByEmail(EMAIL))
                    .thenReturn(Optional.empty());

            when(jwtService.generateNewRefreshToken(savedUser))
                    .thenThrow(exception);

            assertThatThrownBy(() -> authService.register(request))
                    .isSameAs(exception);

            verify(jwtService, never()).getExpiry(anyString());

            verifyNoInteractions(userTokenRepository);
        }

        @Test
        void register_shouldStopWhenExpiryLookupFails() {

            RuntimeException exception =
                    new RuntimeException("Expiry lookup failed");

            when(userRepository.findByEmail(EMAIL))
                    .thenReturn(Optional.empty());

            when(jwtService.getExpiry(REFRESH_TOKEN))
                    .thenThrow(exception);

            assertThatThrownBy(() -> authService.register(request))
                    .isSameAs(exception);

            verify(userTokenRepository, never()).save(any());

            verify(userMapper, never()).toResponse(
                    any(),
                    anyString(),
                    anyString(),
                    any(Instant.class)
            );
        }

        @Test
        @DisplayName("Should stop when refresh token persistence fails")
        void register_shouldStopWhenRefreshTokenPersistenceFails() {

            RuntimeException exception =
                    new RuntimeException("Refresh token persistence failed");

            when(userRepository.findByEmail(EMAIL))
                    .thenReturn(Optional.empty());

            when(userTokenRepository.save(any(UserToken.class)))
                    .thenThrow(exception);

            assertThatThrownBy(() -> authService.register(request))
                    .isSameAs(exception);

            verify(userTokenRepository).save(any(UserToken.class));

            verify(userMapper, never()).toResponse(
                    any(),
                    anyString(),
                    anyString(),
                    any(Instant.class)
            );
        }

        @Test
        @DisplayName("Should propagate exception when response mappin fails")
        void register_shouldPropagateExceptionWhenResponseMappingFails() {

            RuntimeException exception =
                    new RuntimeException("Response mapping failed");

            when(userRepository.findByEmail(EMAIL))
                    .thenReturn(Optional.empty());

            when(userMapper.toResponse(
                    savedUser,
                    ACCESS_TOKEN,
                    REFRESH_TOKEN,
                    EXPIRY_DATE
            )).thenThrow(exception);

            assertThatThrownBy(() -> authService.register(request))
                    .isSameAs(exception);

            verify(userRepository).save(user);
            verify(userTokenRepository).save(any(UserToken.class));

            verify(userMapper).toResponse(
                    savedUser,
                    ACCESS_TOKEN,
                    REFRESH_TOKEN,
                    EXPIRY_DATE
            );
        }


        @Test
        @DisplayName("Should perform operations in correct order")
        void register_shouldPerformOperationsInCorrectOrder() {

            when(userRepository.findByEmail(EMAIL))
                    .thenReturn(Optional.empty());

            var inOrder = inOrder(
                    userRepository,
                    userMapper,
                    passwordEncoder,
                    jwtService,
                    userTokenRepository
            );

            authService.register(request);

            inOrder.verify(userRepository).findByEmail(EMAIL);
            inOrder.verify(userMapper).toEntity(request);
            inOrder.verify(passwordEncoder).encode(RAW_PASSWORD);
            inOrder.verify(userRepository).save(user);
            inOrder.verify(jwtService).generateAccessToken(savedUser);
            inOrder.verify(jwtService).generateNewRefreshToken(savedUser);
            inOrder.verify(jwtService).getExpiry(REFRESH_TOKEN);
            inOrder.verify(userTokenRepository).save(any(UserToken.class));
        }

        @Test
        @DisplayName("Should save user only once")
        void register_shouldSaveUserOnlyOnce() {

            when(userRepository.findByEmail(EMAIL))
                    .thenReturn(Optional.empty());

            authService.register(request);

            verify(userRepository, times(1)).save(user);
        }

        @Test
        @DisplayName("Should save refresh token only once")
        void register_shouldSaveRefreshTokenOnlyOnce() {

            when(userRepository.findByEmail(EMAIL))
                    .thenReturn(Optional.empty());

            authService.register(request);

            verify(userTokenRepository, times(1))
                    .save(any(UserToken.class));
        }

        @Test
        @DisplayName("Should call response mapper only once")
        void register_shouldCallResponseMapperOnlyOnce() {

            when(userRepository.findByEmail(EMAIL))
                    .thenReturn(Optional.empty());

            authService.register(request);

            verify(userMapper, times(1)).toResponse(
                    savedUser,
                    ACCESS_TOKEN,
                    REFRESH_TOKEN,
                    EXPIRY_DATE
            );
        }
    }

    // 2. LOGIN TESTS
    @Nested
    @DisplayName("login()")
    class LoginTests {

        private static final String EMAIL = "karan@example.com";
        private static final String PASSWORD = "Password@123";

        private static final String ACCESS_TOKEN = "new-access-token";
        private static final String REFRESH_TOKEN = "new-refresh-token";

        private static final Instant EXPIRY =
                Instant.parse("2027-01-01T00:00:00Z");

        private User user;
        private UserToken userToken;
        private LoginRequest request;
        private Authentication authentication;
        private AuthResponse expectedResponse;
        private List<GrantedAuthority> authorities;

        @BeforeEach
        void setUp() {

            // Clear security context before every test
            SecurityContextHolder.clearContext();

            request = mock(LoginRequest.class);
            user = mock(User.class);
            userToken = mock(UserToken.class);
            authentication = mock(Authentication.class);
            expectedResponse = mock(AuthResponse.class);

            authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_CLIENT")
            );
        }

        @AfterEach
        void cleanUp() {
            SecurityContextHolder.clearContext();
        }

        /*
         * Common successful login arrangement.
         * Call this only in tests that need the complete success path.
         */
        private void givenSuccessfulLogin() {

            when(request.email()).thenReturn(EMAIL);
            when(request.password()).thenReturn(PASSWORD);

            when(authenticationManager.authenticate(
                    any(UsernamePasswordAuthenticationToken.class)
            )).thenReturn(authentication);

            when(authentication.getPrincipal()).thenReturn(user);

            doReturn(authorities)
                    .when(user)
                            .getAuthorities();
//            when(user.getAuthorities()).thenReturn(authorities);

            when(jwtService.generateAccessToken(user))
                    .thenReturn(ACCESS_TOKEN);

            when(jwtService.generateNewRefreshToken(user))
                    .thenReturn(REFRESH_TOKEN);

            when(jwtService.getExpiry(REFRESH_TOKEN))
                    .thenReturn(Date.from(EXPIRY));

            when(userTokenRepository.findByUser(user))
                    .thenReturn(Optional.of(userToken));

            when(userTokenRepository.save(userToken))
                    .thenReturn(userToken);

            when(userToken.getRefreshToken())
                    .thenReturn(REFRESH_TOKEN);

            when(userToken.getExpiryDate())
                    .thenReturn(EXPIRY);

            when(userMapper.toResponse(
                    user,
                    ACCESS_TOKEN,
                    REFRESH_TOKEN,
                    EXPIRY
            )).thenReturn(expectedResponse);
        }

        // =========================================
        // 1. SUCCESSFUL LOGIN
        // =========================================

        @Test
        @DisplayName("Should login successfully")
        void login_shouldLoginSuccessfully() {

            givenSuccessfulLogin();

            AuthResponse response = authService.login(request);

            assertThat(response).isNotNull();
            assertThat(response).isSameAs(expectedResponse);

            verify(authenticationManager).authenticate(
                    any(UsernamePasswordAuthenticationToken.class)
            );

            verify(userTokenRepository).findByUser(user);
            verify(userTokenRepository).save(userToken);
        }

        @Test
        @DisplayName("Should authenticate using requested email and password")
        void login_shouldPassCorrectCredentialsToAuthenticationManager() {

            givenSuccessfulLogin();

            authService.login(request);

            ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
                    ArgumentCaptor.forClass(
                            UsernamePasswordAuthenticationToken.class
                    );

            verify(authenticationManager).authenticate(captor.capture());

            UsernamePasswordAuthenticationToken token = captor.getValue();

            assertThat(token.getPrincipal()).isEqualTo(EMAIL);
            assertThat(token.getCredentials()).isEqualTo(PASSWORD);
        }

        @Test
        @DisplayName("Should authenticate exactly once")
        void login_shouldAuthenticateOnlyOnce() {

            givenSuccessfulLogin();

            authService.login(request);

            verify(authenticationManager, times(1))
                    .authenticate(any(UsernamePasswordAuthenticationToken.class));
        }

        // =========================================
        // 2. AUTHENTICATION FAILURE
        // =========================================

        @Test
        @DisplayName("Should propagate authentication failure")
        void login_shouldPropagateAuthenticationException() {

            RuntimeException exception =
                    new RuntimeException("Authentication failed");

            when(request.email()).thenReturn(EMAIL);
            when(request.password()).thenReturn(PASSWORD);

            when(authenticationManager.authenticate(any()))
                    .thenThrow(exception);

            assertThatThrownBy(() -> authService.login(request))
                    .isSameAs(exception);

            verifyNoInteractions(
                    jwtService,
                    userTokenRepository,
                    userMapper
            );
        }

        @Test
        @DisplayName("Should propagate bad credentials exception")
        void login_shouldThrowBadCredentialsException() {

            when(request.email()).thenReturn(EMAIL);
            when(request.password()).thenReturn(PASSWORD);

            when(authenticationManager.authenticate(any()))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BadCredentialsException.class);

            verifyNoInteractions(
                    jwtService,
                    userTokenRepository,
                    userMapper
            );
        }

        @Test
        @DisplayName("Should propagate user not found authentication failure")
        void login_shouldPropagateUserNotFoundException() {

            when(request.email()).thenReturn(EMAIL);
            when(request.password()).thenReturn(PASSWORD);

            RuntimeException exception =
                    new RuntimeException("User not found");

            when(authenticationManager.authenticate(any()))
                    .thenThrow(exception);

            assertThatThrownBy(() -> authService.login(request))
                    .isSameAs(exception);

            verifyNoInteractions(jwtService, userTokenRepository);
        }

        // =========================================
        // 3. NULL AUTHENTICATION PRINCIPAL
        // =========================================

        @Test
        @DisplayName("Should throw unauthorized exception when principal is null")
        void login_shouldThrowUnauthorizedWhenPrincipalIsNull() {

            when(request.email()).thenReturn(EMAIL);
            when(request.password()).thenReturn(PASSWORD);

            when(authenticationManager.authenticate(any()))
                    .thenReturn(authentication);

            when(authentication.getPrincipal()).thenReturn(null);

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(ApiException.class);

            verifyNoInteractions(jwtService, userTokenRepository, userMapper);

            assertThat(SecurityContextHolder.getContext().getAuthentication())
                    .isNull();
        }

        // =========================================
        // 4. SECURITY CONTEXT
        // =========================================

        @Test
        @DisplayName("Should set authenticated user in SecurityContext")
        void login_shouldSetUserInSecurityContext() {

            givenSuccessfulLogin();

            authService.login(request);

            Authentication contextAuthentication =
                    SecurityContextHolder.getContext().getAuthentication();

            assertThat(contextAuthentication).isNotNull();
            assertThat(contextAuthentication.isAuthenticated()).isTrue();
            assertThat(contextAuthentication.getPrincipal()).isSameAs(user);
        }

        @Test
        @DisplayName("Should preserve user's authorities")
        void login_shouldPreserveUserAuthorities() {

            givenSuccessfulLogin();

            authService.login(request);

            Authentication contextAuthentication =
                    SecurityContextHolder.getContext().getAuthentication();

            assertThat(contextAuthentication.getAuthorities().toArray())
                    .containsExactlyElementsOf(new ArrayList<>(authorities));
        }

        @Test
        @DisplayName("Should create a new authenticated security token")
        void login_shouldCreateAuthenticatedToken() {

            givenSuccessfulLogin();

            authService.login(request);

            Authentication contextAuthentication =
                    SecurityContextHolder.getContext().getAuthentication();

            assertThat(contextAuthentication)
                    .isInstanceOf(UsernamePasswordAuthenticationToken.class);

            assertThat(contextAuthentication.getCredentials()).isNull();
        }

        // =========================================
        // 5. JWT GENERATION
        // =========================================

        @Test
        @DisplayName("Should generate access token for authenticated user")
        void login_shouldGenerateAccessToken() {

            givenSuccessfulLogin();

            authService.login(request);

            verify(jwtService, times(1))
                    .generateAccessToken(user);
        }

        @Test
        @DisplayName("Should generate refresh token for authenticated user")
        void login_shouldGenerateRefreshToken() {

            givenSuccessfulLogin();

            authService.login(request);

            verify(jwtService, times(1))
                    .generateNewRefreshToken(user);
        }

        @Test
        @DisplayName("Should get expiry using newly generated refresh token")
        void login_shouldGetRefreshTokenExpiry() {

            givenSuccessfulLogin();

            authService.login(request);

            verify(jwtService).getExpiry(REFRESH_TOKEN);
        }

        // =========================================
        // 6. REFRESH TOKEN UPDATE
        // =========================================

        @Test
        @DisplayName("Should find existing refresh token by user")
        void login_shouldFindExistingUserToken() {

            givenSuccessfulLogin();

            authService.login(request);

            verify(userTokenRepository).findByUser(user);
        }

        @Test
        @DisplayName("Should replace existing refresh token")
        void login_shouldReplaceExistingRefreshToken() {

            givenSuccessfulLogin();

            authService.login(request);

            verify(userToken).setRefreshToken(REFRESH_TOKEN);
        }

        @Test
        @DisplayName("Should update refresh token expiry")
        void login_shouldUpdateRefreshTokenExpiry() {

            givenSuccessfulLogin();

            authService.login(request);

            verify(userToken).setExpiryDate(EXPIRY);
        }

        @Test
        @DisplayName("Should reactivate previously revoked refresh token")
        void login_shouldSetRevokedFalse() {

            givenSuccessfulLogin();

            authService.login(request);

            verify(userToken).setRevoked(false);
        }

        @Test
        @DisplayName("Should save updated refresh token")
        void login_shouldSaveUpdatedUserToken() {

            givenSuccessfulLogin();

            authService.login(request);

            verify(userTokenRepository, times(1))
                    .save(userToken);
        }

        @Test
        @DisplayName("Should update existing token instead of creating a new token")
        void login_shouldUpdateExistingTokenInsteadOfCreatingNewToken() {

            givenSuccessfulLogin();

            authService.login(request);

            ArgumentCaptor<UserToken> captor =
                    ArgumentCaptor.forClass(UserToken.class);

            verify(userTokenRepository).save(captor.capture());

            assertThat(captor.getValue()).isSameAs(userToken);

            verify(userTokenRepository, never())
                    .save(argThat(token -> token != userToken));
        }

        // =========================================
        // 7. USER TOKEN NOT FOUND
        // =========================================

        @Test
        @DisplayName("Should throw exception when user token does not exist")
        void login_shouldThrowWhenUserTokenNotFound() {

            givenSuccessfulLogin();

            when(userTokenRepository.findByUser(user))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(UserTokenNotFoundException.class);

            verify(userTokenRepository).findByUser(user);

            verify(userTokenRepository, never()).save(any());

            verify(userMapper, never()).toResponse(
                    any(),
                    anyString(),
                    anyString(),
                    any(Instant.class)
            );
        }

        // =========================================
        // 8. JWT FAILURE SCENARIOS
        // =========================================

        @Test
        @DisplayName("Should propagate access token generation failure")
        void login_shouldPropagateAccessTokenGenerationFailure() {

            givenSuccessfulLogin();

            RuntimeException exception =
                    new RuntimeException("Access token failure");

            when(jwtService.generateAccessToken(user))
                    .thenThrow(exception);

            assertThatThrownBy(() -> authService.login(request))
                    .isSameAs(exception);

            verify(jwtService, never()).generateNewRefreshToken(any());
            verify(jwtService, never()).getExpiry(anyString());

            verifyNoInteractions(userTokenRepository);
        }

        @Test
        @DisplayName("Should propagate refresh token generation failure")
        void login_shouldPropagateRefreshTokenGenerationFailure() {

            givenSuccessfulLogin();

            RuntimeException exception =
                    new RuntimeException("Refresh token failure");

            when(jwtService.generateNewRefreshToken(user))
                    .thenThrow(exception);

            assertThatThrownBy(() -> authService.login(request))
                    .isSameAs(exception);

            verify(jwtService, never()).getExpiry(anyString());

            verifyNoInteractions(userTokenRepository);
        }

        @Test
        @DisplayName("Should propagate refresh token expiry failure")
        void login_shouldPropagateExpiryFailure() {

            givenSuccessfulLogin();

            RuntimeException exception =
                    new RuntimeException("Expiry failure");

            when(jwtService.getExpiry(REFRESH_TOKEN))
                    .thenThrow(exception);

            assertThatThrownBy(() -> authService.login(request))
                    .isSameAs(exception);

            verify(userTokenRepository, never()).findByUser(any());
            verify(userTokenRepository, never()).save(any());
        }

        // =========================================
        // 9. PERSISTENCE FAILURE
        // =========================================

        @Test
        @DisplayName("Should propagate refresh token persistence failure")
        void login_shouldPropagateTokenSaveFailure() {

            givenSuccessfulLogin();

            RuntimeException exception =
                    new RuntimeException("Database save failure");

            when(userTokenRepository.save(userToken))
                    .thenThrow(exception);

            assertThatThrownBy(() -> authService.login(request))
                    .isSameAs(exception);

            verify(userTokenRepository).save(userToken);

            verify(userMapper, never()).toResponse(
                    any(),
                    anyString(),
                    anyString(),
                    any(Instant.class)
            );
        }

        // =========================================
        // 10. RESPONSE MAPPING
        // =========================================

        @Test
        @DisplayName("Should return response mapped by UserMapper")
        void login_shouldReturnMappedAuthResponse() {

            givenSuccessfulLogin();

            AuthResponse response = authService.login(request);

            assertThat(response).isSameAs(expectedResponse);
        }

        @Test
        @DisplayName("Should map response using authenticated user and generated tokens")
        void login_shouldMapResponseWithCorrectArguments() {

            givenSuccessfulLogin();

            authService.login(request);

            verify(userMapper).toResponse(
                    user,
                    ACCESS_TOKEN,
                    REFRESH_TOKEN,
                    EXPIRY
            );
        }

        @Test
        @DisplayName("Should propagate response mapping failure")
        void login_shouldPropagateResponseMappingFailure() {

            givenSuccessfulLogin();

            RuntimeException exception =
                    new RuntimeException("Response mapping failure");

            when(userMapper.toResponse(
                    user,
                    ACCESS_TOKEN,
                    REFRESH_TOKEN,
                    EXPIRY
            )).thenThrow(exception);

            assertThatThrownBy(() -> authService.login(request))
                    .isSameAs(exception);

            verify(userTokenRepository).save(userToken);
            verify(userMapper).toResponse(
                    user,
                    ACCESS_TOKEN,
                    REFRESH_TOKEN,
                    EXPIRY
            );
        }

        // =========================================
        // 11. INTERACTION ORDER
        // =========================================

        @Test
        @DisplayName("Should execute login operations in correct order")
        void login_shouldExecuteOperationsInCorrectOrder() {

            givenSuccessfulLogin();

            var inOrder = inOrder(
                    authenticationManager,
                    jwtService,
                    userTokenRepository,
                    userMapper
            );

            authService.login(request);

            inOrder.verify(authenticationManager)
                    .authenticate(any(UsernamePasswordAuthenticationToken.class));

            inOrder.verify(jwtService).generateAccessToken(user);
            inOrder.verify(jwtService).generateNewRefreshToken(user);
            inOrder.verify(jwtService).getExpiry(REFRESH_TOKEN);

            inOrder.verify(userTokenRepository).findByUser(user);
            inOrder.verify(userTokenRepository).save(userToken);

            inOrder.verify(userMapper).toResponse(
                    user,
                    ACCESS_TOKEN,
                    REFRESH_TOKEN,
                    EXPIRY
            );
        }

        // =========================================
        // 12. NO REGISTRATION SIDE EFFECTS
        // =========================================

        @Test
        @DisplayName("Should not create a new user during login")
        void login_shouldNotCreateNewUser() {

            givenSuccessfulLogin();

            authService.login(request);

            verifyNoInteractions(userRepository);

            verify(userMapper, never()).toEntity(any());
            verify(passwordEncoder, never()).encode(anyString());
        }
    }

}
