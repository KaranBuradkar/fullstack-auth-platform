package com.authplatform.backend.service;

import com.authplatform.backend.common.exception.ApiAuthenticationException;
import com.authplatform.backend.common.exception.ApiException;
import com.authplatform.backend.common.exception.BadRequestException;
import com.authplatform.backend.common.exception.OtpVerificationException;
import com.authplatform.backend.common.service.email.EmailService;
import com.authplatform.backend.dto.request.ChangePasswordRequest;
import com.authplatform.backend.dto.request.ForgotPasswordRequest;
import com.authplatform.backend.dto.request.ResetPasswordRequest;
import com.authplatform.backend.entity.User;
import com.authplatform.backend.entity.UserOTP;
import com.authplatform.backend.exception.UserNotFoundException;
import com.authplatform.backend.repository.UserOtpRepository;
import com.authplatform.backend.repository.UserRepository;
import com.authplatform.backend.repository.UserTokenRepository;
import com.authplatform.backend.security.otp.OtpEncoder;
import com.authplatform.backend.security.otp.OtpGenerator;
import com.authplatform.backend.security.otp.OtpProperties;
import com.authplatform.backend.security.otp.OtpPurpose;

import com.authplatform.backend.service.impl.PasswordServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PasswordServiceImpl.
 * Tests change password, reset password and forget password
 * business logic independently using Mockito
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PasswordServiceImpl Unit Tests")
public class PasswordServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserOtpRepository userOtpRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private OtpGenerator otpGenerator;

    @Mock
    private UserTokenRepository userTokenRepository;

    @Mock
    private OtpEncoder otpEncoder;

    @Mock
    private OtpProperties otpProperties;

    @InjectMocks
    private PasswordServiceImpl passwordService;

    private static final String EMAIL = "user@example.com";

    private static final String RAW_PASSWORD = "Password@123";
    private static final String NEW_PASSWORD = "NewPassword@123";
    private static final String ENCODED_PASSWORD = "$2a$12$encodedPassword";
    private static final String ENCODED_NEW_PASSWORD = "$2a$12$newEncodedPassword";

    private static final String OTP = "123456";
    private static final String HASHED_OTP = "hashed-otp";

    private static final int MAX_ATTEMPTS = 5;
    private static final long OTP_EXPIRATION_SECONDS = 300L;

    private User user;

    @BeforeEach
    void setUp() {
        user = mock(User.class);
    }

    @AfterEach
    void cleanUp() {
        SecurityContextHolder.clearContext();
    }

    // =====================================================
    // 1. FORGOT PASSWORD TESTS
    // =====================================================

    @Nested
    @DisplayName("forgotPassword()")
    class ForgotPasswordTests {

        private ForgotPasswordRequest request;

        @BeforeEach
        void setUp() {
            request = mock(ForgotPasswordRequest.class);
            when(request.email()).thenReturn(EMAIL);
        }

        @Test
        @DisplayName("Should generate and send password reset OTP successfully")
        void forgotPassword_shouldGenerateAndSendOtpSuccessfully() {

            when(userRepository.findByEmail(EMAIL))
                    .thenReturn(Optional.of(user));

            when(user.getEmail()).thenReturn(EMAIL);

            when(otpGenerator.generateOtp())
                    .thenReturn(OTP);

            when(otpGenerator.getOtpExpiration())
                    .thenReturn(OTP_EXPIRATION_SECONDS);

            when(otpProperties.maxAttempts())
                    .thenReturn(MAX_ATTEMPTS);

            when(otpEncoder.encode(OtpPurpose.PASSWORD_RESET, OTP))
                    .thenReturn(HASHED_OTP);

            passwordService.forgotPassword(request);

            verify(userRepository).findByEmail(EMAIL);

            verify(userOtpRepository).deleteAllByUserEmail(EMAIL);

            verify(otpGenerator).generateOtp();

            verify(otpEncoder).encode(OtpPurpose.PASSWORD_RESET, OTP);

            verify(userOtpRepository).save(any(UserOTP.class));

            verify(emailService).sendVerificationOtp(EMAIL, OTP);
        }

        @Test
        @DisplayName("Should save hashed OTP instead of raw OTP")
        void forgotPassword_shouldSaveHashedOtp() {

            when(userRepository.findByEmail(EMAIL))
                    .thenReturn(Optional.of(user));

            when(user.getEmail()).thenReturn(EMAIL);

            when(otpGenerator.generateOtp()).thenReturn(OTP);

            when(otpGenerator.getOtpExpiration())
                    .thenReturn(OTP_EXPIRATION_SECONDS);

            when(otpProperties.maxAttempts())
                    .thenReturn(MAX_ATTEMPTS);

            when(otpEncoder.encode(OtpPurpose.PASSWORD_RESET, OTP))
                    .thenReturn(HASHED_OTP);

            passwordService.forgotPassword(request);

            ArgumentCaptor<UserOTP> captor =
                    ArgumentCaptor.forClass(UserOTP.class);

            verify(userOtpRepository).save(captor.capture());

            UserOTP savedOtp = captor.getValue();

            assertThat(savedOtp.getHashedOtp())
                    .isEqualTo(HASHED_OTP);

            assertThat(savedOtp.getHashedOtp())
                    .isNotEqualTo(OTP);
        }

        @Test
        @DisplayName("Should associate reset OTP with the correct user")
        void forgotPassword_shouldAssociateOtpWithUser() {

            when(userRepository.findByEmail(EMAIL))
                    .thenReturn(Optional.of(user));

            when(user.getEmail()).thenReturn(EMAIL);

            when(otpGenerator.generateOtp()).thenReturn(OTP);

            when(otpGenerator.getOtpExpiration())
                    .thenReturn(OTP_EXPIRATION_SECONDS);

            when(otpProperties.maxAttempts())
                    .thenReturn(MAX_ATTEMPTS);

            when(otpEncoder.encode(OtpPurpose.PASSWORD_RESET, OTP))
                    .thenReturn(HASHED_OTP);

            passwordService.forgotPassword(request);

            ArgumentCaptor<UserOTP> captor =
                    ArgumentCaptor.forClass(UserOTP.class);

            verify(userOtpRepository).save(captor.capture());

            assertThat(captor.getValue().getUser())
                    .isSameAs(user);
        }

        @Test
        @DisplayName("Should initialize OTP attempts to zero")
        void forgotPassword_shouldInitializeAttemptsToZero() {

            when(userRepository.findByEmail(EMAIL))
                    .thenReturn(Optional.of(user));

            when(user.getEmail()).thenReturn(EMAIL);

            when(otpGenerator.generateOtp()).thenReturn(OTP);

            when(otpGenerator.getOtpExpiration())
                    .thenReturn(OTP_EXPIRATION_SECONDS);

            when(otpProperties.maxAttempts())
                    .thenReturn(MAX_ATTEMPTS);

            when(otpEncoder.encode(OtpPurpose.PASSWORD_RESET, OTP))
                    .thenReturn(HASHED_OTP);

            passwordService.forgotPassword(request);

            ArgumentCaptor<UserOTP> captor =
                    ArgumentCaptor.forClass(UserOTP.class);

            verify(userOtpRepository).save(captor.capture());

            assertThat(captor.getValue().getAttempt()).isZero();

            assertThat(captor.getValue().getMaxAttempt())
                    .isEqualTo(MAX_ATTEMPTS);
        }

        @Test
        @DisplayName("Should set OTP expiration using configured duration")
        void forgotPassword_shouldSetOtpExpiration() {

            when(userRepository.findByEmail(EMAIL))
                    .thenReturn(Optional.of(user));

            when(user.getEmail()).thenReturn(EMAIL);

            when(otpGenerator.generateOtp()).thenReturn(OTP);

            when(otpGenerator.getOtpExpiration())
                    .thenReturn(OTP_EXPIRATION_SECONDS);

            when(otpProperties.maxAttempts())
                    .thenReturn(MAX_ATTEMPTS);

            when(otpEncoder.encode(OtpPurpose.PASSWORD_RESET, OTP))
                    .thenReturn(HASHED_OTP);

            Instant beforeExecution = Instant.now();

            passwordService.forgotPassword(request);

            Instant afterExecution = Instant.now();

            ArgumentCaptor<UserOTP> captor =
                    ArgumentCaptor.forClass(UserOTP.class);

            verify(userOtpRepository).save(captor.capture());

            Instant expiry = captor.getValue().getExpiresAt();

            assertThat(expiry)
                    .isBetween(
                            beforeExecution.plusSeconds(OTP_EXPIRATION_SECONDS),
                            afterExecution.plusSeconds(OTP_EXPIRATION_SECONDS)
                    );
        }

        @Test
        @DisplayName("Should delete previous OTP before saving a new OTP")
        void forgotPassword_shouldDeletePreviousOtpBeforeSavingNewOtp() {

            when(userRepository.findByEmail(EMAIL))
                    .thenReturn(Optional.of(user));

            when(user.getEmail()).thenReturn(EMAIL);

            when(otpGenerator.generateOtp()).thenReturn(OTP);

            when(otpGenerator.getOtpExpiration())
                    .thenReturn(OTP_EXPIRATION_SECONDS);

            when(otpProperties.maxAttempts())
                    .thenReturn(MAX_ATTEMPTS);

            when(otpEncoder.encode(OtpPurpose.PASSWORD_RESET, OTP))
                    .thenReturn(HASHED_OTP);

            InOrder inOrder = inOrder(userOtpRepository, emailService);

            passwordService.forgotPassword(request);

            inOrder.verify(userOtpRepository)
                    .deleteAllByUserEmail(EMAIL);

            inOrder.verify(userOtpRepository)
                    .save(any(UserOTP.class));

            inOrder.verify(emailService)
                    .sendVerificationOtp(EMAIL, OTP);
        }

        @Test
        @DisplayName("Should throw exception when user does not exist")
        void forgotPassword_shouldThrowWhenUserNotFound() {

            when(userRepository.findByEmail(EMAIL))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    passwordService.forgotPassword(request))
                    .isInstanceOf(UserNotFoundException.class);

            verify(userOtpRepository, never())
                    .deleteAllByUserEmail(anyString());

            verifyNoInteractions(
                    otpGenerator,
                    otpEncoder,
                    emailService
            );
        }

        @Test
        @DisplayName("Should stop when OTP generation fails")
        void forgotPassword_shouldStopWhenOtpGenerationFails() {

            when(userRepository.findByEmail(EMAIL))
                    .thenReturn(Optional.of(user));

            when(user.getEmail()).thenReturn(EMAIL);

            RuntimeException exception =
                    new RuntimeException("OTP generation failed");

            when(otpGenerator.generateOtp())
                    .thenThrow(exception);

            assertThatThrownBy(() ->
                    passwordService.forgotPassword(request))
                    .isSameAs(exception);

            verify(userOtpRepository)
                    .deleteAllByUserEmail(EMAIL);

            verify(userOtpRepository, never())
                    .save(any(UserOTP.class));

            verifyNoInteractions(emailService);
        }

        @Test
        @DisplayName("Should not send email when OTP persistence fails")
        void forgotPassword_shouldNotSendEmailWhenOtpSaveFails() {

            when(userRepository.findByEmail(EMAIL))
                    .thenReturn(Optional.of(user));

            when(user.getEmail()).thenReturn(EMAIL);

            when(otpGenerator.generateOtp()).thenReturn(OTP);

            when(otpGenerator.getOtpExpiration())
                    .thenReturn(OTP_EXPIRATION_SECONDS);

            when(otpProperties.maxAttempts())
                    .thenReturn(MAX_ATTEMPTS);

            when(otpEncoder.encode(OtpPurpose.PASSWORD_RESET, OTP))
                    .thenReturn(HASHED_OTP);

            RuntimeException exception =
                    new RuntimeException("OTP persistence failed");

            when(userOtpRepository.save(any(UserOTP.class)))
                    .thenThrow(exception);

            assertThatThrownBy(() ->
                    passwordService.forgotPassword(request))
                    .isSameAs(exception);

            verify(emailService, never())
                    .sendVerificationOtp(anyString(), anyString());
        }
    }

    // =====================================================
    // 2. RESET PASSWORD TESTS
    // =====================================================

    @Nested
    @DisplayName("resetPassword()")
    class ResetPasswordTests {

        private ResetPasswordRequest request;

        private UserOTP resetOtp;

        @BeforeEach
        void setUp() {
            request = mock(ResetPasswordRequest.class);

            when(request.email()).thenReturn(EMAIL);

            resetOtp = createResetOtp(
                    user,
                    0,
                    MAX_ATTEMPTS,
                    Instant.now().plusSeconds(300)
            );
        }

        private UserOTP createResetOtp(
                User user,
                int attempts,
                int maxAttempts,
                Instant expiry
        ) {
            return UserOTP.of(
                    user,
                    HASHED_OTP,
                    attempts,
                    maxAttempts,
                    expiry
            );
        }

        private void givenValidResetOtp() {

            when(userOtpRepository.findFirstByUserEmail(EMAIL))
                    .thenReturn(Optional.of(resetOtp));

            when(otpEncoder.matches(
                    OtpPurpose.PASSWORD_RESET,
                    OTP,
                    HASHED_OTP
            )).thenReturn(true);

            when(passwordEncoder.encode(NEW_PASSWORD))
                    .thenReturn(ENCODED_NEW_PASSWORD);
        }

        @Test
        @DisplayName("Should reset password successfully with valid OTP")
        void resetPassword_shouldResetPasswordSuccessfully() {

            when(request.otp()).thenReturn(OTP);
            when(request.newPassword()).thenReturn(NEW_PASSWORD);

            givenValidResetOtp();

            passwordService.resetPassword(request);

            verify(userOtpRepository)
                    .findFirstByUserEmail(EMAIL);

            verify(otpEncoder).matches(
                    OtpPurpose.PASSWORD_RESET,
                    OTP,
                    HASHED_OTP
            );

            verify(passwordEncoder).encode(NEW_PASSWORD);

            verify(user).setPassword(ENCODED_NEW_PASSWORD);

            verify(userRepository).save(user);

            verify(userOtpRepository).delete(resetOtp);
        }

        @Test
        @DisplayName("Should encode new password before saving user")
        void resetPassword_shouldEncodePasswordBeforeSavingUser() {

            when(request.otp()).thenReturn(OTP);
            when(request.newPassword()).thenReturn(NEW_PASSWORD);

            givenValidResetOtp();

            InOrder inOrder = inOrder(
                    passwordEncoder,
                    user,
                    userRepository,
                    userOtpRepository
            );

            passwordService.resetPassword(request);

            inOrder.verify(passwordEncoder).encode(NEW_PASSWORD);

            inOrder.verify(user).setPassword(ENCODED_NEW_PASSWORD);

            inOrder.verify(userRepository).save(user);

            inOrder.verify(userOtpRepository).delete(resetOtp);
        }

        @Test
        @DisplayName("Should never store raw new password")
        void resetPassword_shouldNeverStoreRawPassword() {

            when(request.otp()).thenReturn(OTP);
            when(request.newPassword()).thenReturn(NEW_PASSWORD);

            givenValidResetOtp();

            passwordService.resetPassword(request);

            verify(user).setPassword(argThat(encodedPassword ->
                    encodedPassword != null
                            && !encodedPassword.equals(NEW_PASSWORD)
            ));

            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("Should delete OTP after successful password reset")
        void resetPassword_shouldDeleteOtpAfterSuccessfulReset() {

            when(request.otp()).thenReturn(OTP);
            when(request.newPassword()).thenReturn(NEW_PASSWORD);

            givenValidResetOtp();

            passwordService.resetPassword(request);

            verify(userOtpRepository).delete(resetOtp);
        }

        @Test
        @DisplayName("Should throw exception when reset OTP does not exist")
        void resetPassword_shouldThrowWhenOtpNotFound() {

            when(userOtpRepository.findFirstByUserEmail(EMAIL))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    passwordService.resetPassword(request))
                    .isInstanceOf(ApiException.class);

            verifyNoInteractions(otpEncoder, passwordEncoder);

            verify(userRepository, never()).save(any());

            verify(userOtpRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should reject OTP when maximum attempts are exceeded")
        void resetPassword_shouldRejectWhenMaximumAttemptsExceeded() {

            resetOtp = createResetOtp(
                    user,
                    MAX_ATTEMPTS,
                    MAX_ATTEMPTS,
                    Instant.now().plusSeconds(300)
            );

            when(userOtpRepository.findFirstByUserEmail(EMAIL))
                    .thenReturn(Optional.of(resetOtp));

            assertThatThrownBy(() ->
                    passwordService.resetPassword(request))
                    .isInstanceOf(OtpVerificationException.class);

            assertThat(resetOtp.getAttempt())
                    .isEqualTo(MAX_ATTEMPTS + 1);

            verifyNoInteractions(otpEncoder, passwordEncoder);

            verify(userRepository, never()).save(any());

            verify(userOtpRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should reject expired OTP")
        void resetPassword_shouldRejectExpiredOtp() {

            resetOtp = createResetOtp(
                    user,
                    0,
                    MAX_ATTEMPTS,
                    Instant.now().minusSeconds(60)
            );

            when(userOtpRepository.findFirstByUserEmail(EMAIL))
                    .thenReturn(Optional.of(resetOtp));

            assertThatThrownBy(() ->
                    passwordService.resetPassword(request))
                    .isInstanceOf(OtpVerificationException.class);

            verifyNoInteractions(otpEncoder, passwordEncoder);

            verify(userRepository, never()).save(any());

            verify(userOtpRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should reject invalid OTP")
        void resetPassword_shouldRejectInvalidOtp() {

            when(request.otp()).thenReturn(OTP);
            when(userOtpRepository.findFirstByUserEmail(EMAIL))
                    .thenReturn(Optional.of(resetOtp));

            when(otpEncoder.matches(
                    OtpPurpose.PASSWORD_RESET,
                    OTP,
                    HASHED_OTP
            )).thenReturn(false);

            assertThatThrownBy(() ->
                    passwordService.resetPassword(request))
                    .isInstanceOf(OtpVerificationException.class);

            assertThat(resetOtp.getAttempt()).isEqualTo(1);

            verify(passwordEncoder, never()).encode(anyString());

            verify(userRepository, never()).save(any());

            verify(userOtpRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should not encode password when OTP is invalid")
        void resetPassword_shouldNotEncodePasswordForInvalidOtp() {

            when(request.otp()).thenReturn(OTP);
            when(userOtpRepository.findFirstByUserEmail(EMAIL))
                    .thenReturn(Optional.of(resetOtp));

            when(otpEncoder.matches(
                    OtpPurpose.PASSWORD_RESET,
                    OTP,
                    HASHED_OTP
            )).thenReturn(false);

            assertThatThrownBy(() ->
                    passwordService.resetPassword(request))
                    .isInstanceOf(OtpVerificationException.class);

            verify(passwordEncoder, never()).encode(anyString());
        }

        @Test
        @DisplayName("Should stop when password encoding fails")
        void resetPassword_shouldStopWhenPasswordEncodingFails() {

            when(request.otp()).thenReturn(OTP);
            when(request.newPassword()).thenReturn(NEW_PASSWORD);

            when(userOtpRepository.findFirstByUserEmail(EMAIL))
                    .thenReturn(Optional.of(resetOtp));

            when(otpEncoder.matches(
                    OtpPurpose.PASSWORD_RESET,
                    OTP,
                    HASHED_OTP
            )).thenReturn(true);

            RuntimeException exception =
                    new RuntimeException("Password encoding failed");

            when(passwordEncoder.encode(NEW_PASSWORD))
                    .thenThrow(exception);

            assertThatThrownBy(() ->
                    passwordService.resetPassword(request))
                    .isSameAs(exception);

            verify(userRepository, never()).save(any());

            verify(userOtpRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should propagate user persistence failure")
        void resetPassword_shouldPropagateUserSaveFailure() {

            when(request.otp()).thenReturn(OTP);
            when(request.newPassword()).thenReturn(NEW_PASSWORD);

            when(userOtpRepository.findFirstByUserEmail(EMAIL))
                    .thenReturn(Optional.of(resetOtp));

            when(otpEncoder.matches(
                    OtpPurpose.PASSWORD_RESET,
                    OTP,
                    HASHED_OTP
            )).thenReturn(true);

            when(passwordEncoder.encode(NEW_PASSWORD))
                    .thenReturn(ENCODED_NEW_PASSWORD);

            RuntimeException exception =
                    new RuntimeException("User save failed");

            when(userRepository.save(user))
                    .thenThrow(exception);

            assertThatThrownBy(() ->
                    passwordService.resetPassword(request))
                    .isSameAs(exception);

            verify(userOtpRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should propagate OTP deletion failure")
        void resetPassword_shouldPropagateOtpDeletionFailure() {

            when(request.otp()).thenReturn(OTP);
            when(request.newPassword()).thenReturn(NEW_PASSWORD);

            givenValidResetOtp();

            RuntimeException exception =
                    new RuntimeException("OTP deletion failed");

            doThrow(exception)
                    .when(userOtpRepository).delete(resetOtp);

            assertThatThrownBy(() ->
                    passwordService.resetPassword(request))
                    .isSameAs(exception);

            verify(userRepository).save(user);
            verify(userOtpRepository).delete(resetOtp);
        }
    }

    // =====================================================
    // 3. CHANGE PASSWORD TESTS
    // =====================================================

    @Nested
    @DisplayName("changePassword()")
    class ChangePasswordTests {

        private ChangePasswordRequest request;

        private Authentication authentication;

        private static final String CURRENT_PASSWORD = "Current@123";
        private static final String ENCODED_CURRENT_PASSWORD = "$2a$12$currentHash";

        @BeforeEach
        void setUp() {

            request = mock(ChangePasswordRequest.class);
            authentication = mock(Authentication.class);
        }

        private void givenAuthenticatedUser() {

            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);

            when(authentication.getName())
                    .thenReturn(EMAIL);

            when(userRepository.findByEmail(EMAIL))
                    .thenReturn(Optional.of(user));

            when(user.getPassword())
                    .thenReturn(ENCODED_CURRENT_PASSWORD);
        }

        @Test
        @DisplayName("Should change password successfully")
        void changePassword_shouldChangePasswordSuccessfully() {

            when(request.currentPassword())
                    .thenReturn(CURRENT_PASSWORD);

            when(request.newPassword())
                    .thenReturn(NEW_PASSWORD);

            givenAuthenticatedUser();

            when(passwordEncoder.matches(
                    CURRENT_PASSWORD,
                    ENCODED_CURRENT_PASSWORD
            )).thenReturn(true);

            when(passwordEncoder.matches(
                    NEW_PASSWORD,
                    ENCODED_CURRENT_PASSWORD
            )).thenReturn(false);

            when(passwordEncoder.encode(NEW_PASSWORD))
                    .thenReturn(ENCODED_NEW_PASSWORD);

            changePasswordAndVerifySuccess();
        }

        private void changePasswordAndVerifySuccess() {

            passwordService.changePassword(request);

            verify(passwordEncoder).encode(NEW_PASSWORD);

            verify(user).setPassword(ENCODED_NEW_PASSWORD);

            verify(userRepository).save(user);

            verify(userTokenRepository)
                    .revokeAllTokensByUserId(user.getId());
        }

        @Test
        @DisplayName("Should verify current password before changing it")
        void changePassword_shouldVerifyCurrentPassword() {

            when(request.currentPassword())
                    .thenReturn(CURRENT_PASSWORD);

            when(request.newPassword())
                    .thenReturn(NEW_PASSWORD);

            givenAuthenticatedUser();

            when(passwordEncoder.matches(
                    CURRENT_PASSWORD,
                    ENCODED_CURRENT_PASSWORD
            )).thenReturn(true);

            when(passwordEncoder.matches(
                    NEW_PASSWORD,
                    ENCODED_CURRENT_PASSWORD
            )).thenReturn(false);

            when(passwordEncoder.encode(NEW_PASSWORD))
                    .thenReturn(ENCODED_NEW_PASSWORD);

            passwordService.changePassword(request);

            verify(passwordEncoder).matches(
                    CURRENT_PASSWORD,
                    ENCODED_CURRENT_PASSWORD
            );
        }

        @Test
        @DisplayName("Should reject change when current password is incorrect")
        void changePassword_shouldRejectIncorrectCurrentPassword() {

            when(request.currentPassword())
                    .thenReturn(CURRENT_PASSWORD);

            givenAuthenticatedUser();

            when(passwordEncoder.matches(
                    CURRENT_PASSWORD,
                    ENCODED_CURRENT_PASSWORD
            )).thenReturn(false);

            assertThatThrownBy(() ->
                    passwordService.changePassword(request))
                    .isInstanceOf(BadRequestException.class);

            verify(passwordEncoder, never()).encode(anyString());

            verify(userRepository, never()).save(any());

            verifyNoInteractions(userTokenRepository);
        }

        @Test
        @DisplayName("Should reject using the same password")
        void changePassword_shouldRejectSamePassword() {

            when(request.currentPassword())
                    .thenReturn(CURRENT_PASSWORD);

            when(request.newPassword())
                    .thenReturn(NEW_PASSWORD);

            givenAuthenticatedUser();

            when(passwordEncoder.matches(
                    CURRENT_PASSWORD,
                    ENCODED_CURRENT_PASSWORD
            )).thenReturn(true);

            when(passwordEncoder.matches(
                    NEW_PASSWORD,
                    ENCODED_CURRENT_PASSWORD
            )).thenReturn(true);

            assertThatThrownBy(() ->
                    passwordService.changePassword(request))
                    .isInstanceOf(BadRequestException.class);

            verify(passwordEncoder, never()).encode(anyString());

            verify(userRepository, never()).save(any());

            verifyNoInteractions(userTokenRepository);
        }

        @Test
        @DisplayName("Should encode new password before saving user")
        void changePassword_shouldEncodeBeforeSavingUser() {

            when(request.currentPassword())
                    .thenReturn(CURRENT_PASSWORD);

            when(request.newPassword())
                    .thenReturn(NEW_PASSWORD);

            givenAuthenticatedUser();

            when(passwordEncoder.matches(
                    CURRENT_PASSWORD,
                    ENCODED_CURRENT_PASSWORD
            )).thenReturn(true);

            when(passwordEncoder.matches(
                    NEW_PASSWORD,
                    ENCODED_CURRENT_PASSWORD
            )).thenReturn(false);

            when(passwordEncoder.encode(NEW_PASSWORD))
                    .thenReturn(ENCODED_NEW_PASSWORD);

            InOrder inOrder = inOrder(
                    passwordEncoder,
                    user,
                    userRepository,
                    userTokenRepository
            );

            passwordService.changePassword(request);

            inOrder.verify(passwordEncoder).encode(NEW_PASSWORD);

            inOrder.verify(user).setPassword(ENCODED_NEW_PASSWORD);

            inOrder.verify(userRepository).save(user);

            inOrder.verify(userTokenRepository)
                    .revokeAllTokensByUserId(user.getId());
        }

        @Test
        @DisplayName("Should never save raw new password")
        void changePassword_shouldNeverStoreRawPassword() {

            when(request.currentPassword())
                    .thenReturn(CURRENT_PASSWORD);

            when(request.newPassword())
                    .thenReturn(NEW_PASSWORD);

            givenAuthenticatedUser();

            when(passwordEncoder.matches(
                    CURRENT_PASSWORD,
                    ENCODED_CURRENT_PASSWORD
            )).thenReturn(true);

            when(passwordEncoder.matches(
                    NEW_PASSWORD,
                    ENCODED_CURRENT_PASSWORD
            )).thenReturn(false);

            when(passwordEncoder.encode(NEW_PASSWORD))
                    .thenReturn(ENCODED_NEW_PASSWORD);

            passwordService.changePassword(request);

            verify(user).setPassword(argThat(encodedPassword ->
                    encodedPassword != null
                            && !encodedPassword.equals(NEW_PASSWORD)
            ));
        }

        @Test
        @DisplayName("Should revoke all existing refresh tokens after password change")
        void changePassword_shouldRevokeAllExistingTokens() {

            when(request.currentPassword())
                    .thenReturn(CURRENT_PASSWORD);

            when(request.newPassword())
                    .thenReturn(NEW_PASSWORD);

            givenAuthenticatedUser();

            when(passwordEncoder.matches(
                    CURRENT_PASSWORD,
                    ENCODED_CURRENT_PASSWORD
            )).thenReturn(true);

            when(passwordEncoder.matches(
                    NEW_PASSWORD,
                    ENCODED_CURRENT_PASSWORD
            )).thenReturn(false);

            when(passwordEncoder.encode(NEW_PASSWORD))
                    .thenReturn(ENCODED_NEW_PASSWORD);

            passwordService.changePassword(request);

            verify(userTokenRepository)
                    .revokeAllTokensByUserId(user.getId());
        }

        @Test
        @DisplayName("Should not change password when authentication is missing")
        void changePassword_shouldThrowWhenAuthenticationIsMissing() {

            SecurityContextHolder.clearContext();

            assertThatThrownBy(() ->
                    passwordService.changePassword(request))
                    .isInstanceOf(ApiAuthenticationException.class);

            verifyNoInteractions(
                    userRepository,
                    passwordEncoder,
                    userTokenRepository
            );
        }

        @Test
        @DisplayName("Should throw exception when authenticated user does not exist")
        void changePassword_shouldThrowWhenUserNotFound() {

            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);

            when(authentication.getName()).thenReturn(EMAIL);

            when(userRepository.findByEmail(EMAIL))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    passwordService.changePassword(request))
                    .isInstanceOf(UserNotFoundException.class);

            verifyNoInteractions(passwordEncoder, userTokenRepository);
        }

        @Test
        @DisplayName("Should not revoke tokens when current password is incorrect")
        void changePassword_shouldNotRevokeTokensWhenCurrentPasswordIsIncorrect() {

            when(request.currentPassword())
                    .thenReturn(CURRENT_PASSWORD);

            givenAuthenticatedUser();

            when(passwordEncoder.matches(
                    CURRENT_PASSWORD,
                    ENCODED_CURRENT_PASSWORD
            )).thenReturn(false);

            assertThatThrownBy(() ->
                    passwordService.changePassword(request))
                    .isInstanceOf(BadRequestException.class);

            verify(userTokenRepository, never())
                    .revokeAllTokensByUserId(any());
        }

        @Test
        @DisplayName("Should propagate user persistence failure")
        void changePassword_shouldPropagateUserSaveFailure() {

            when(request.currentPassword())
                    .thenReturn(CURRENT_PASSWORD);

            when(request.newPassword())
                    .thenReturn(NEW_PASSWORD);

            givenAuthenticatedUser();

            when(passwordEncoder.matches(
                    CURRENT_PASSWORD,
                    ENCODED_CURRENT_PASSWORD
            )).thenReturn(true);

            when(passwordEncoder.matches(
                    NEW_PASSWORD,
                    ENCODED_CURRENT_PASSWORD
            )).thenReturn(false);

            when(passwordEncoder.encode(NEW_PASSWORD))
                    .thenReturn(ENCODED_NEW_PASSWORD);

            RuntimeException exception =
                    new RuntimeException("User persistence failed");

            when(userRepository.save(user))
                    .thenThrow(exception);

            assertThatThrownBy(() ->
                    passwordService.changePassword(request))
                    .isSameAs(exception);

            verify(userTokenRepository, never())
                    .revokeAllTokensByUserId(any());
        }

        @Test
        @DisplayName("Should propagate token revocation failure")
        void changePassword_shouldPropagateTokenRevocationFailure() {

            when(request.currentPassword())
                    .thenReturn(CURRENT_PASSWORD);

            when(request.newPassword())
                    .thenReturn(NEW_PASSWORD);

            givenAuthenticatedUser();

            when(passwordEncoder.matches(
                    CURRENT_PASSWORD,
                    ENCODED_CURRENT_PASSWORD
            )).thenReturn(true);

            when(passwordEncoder.matches(
                    NEW_PASSWORD,
                    ENCODED_CURRENT_PASSWORD
            )).thenReturn(false);

            when(passwordEncoder.encode(NEW_PASSWORD))
                    .thenReturn(ENCODED_NEW_PASSWORD);

            RuntimeException exception =
                    new RuntimeException("Token revocation failed");

            doThrow(exception)
                    .when(userTokenRepository)
                    .revokeAllTokensByUserId(any());

            assertThatThrownBy(() ->
                    passwordService.changePassword(request))
                    .isSameAs(exception);

            verify(userRepository).save(user);
        }
    }
}
