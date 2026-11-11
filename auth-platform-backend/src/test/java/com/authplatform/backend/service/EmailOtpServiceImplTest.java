package com.authplatform.backend.service;

import com.authplatform.backend.common.exception.OtpVerificationException;
import com.authplatform.backend.common.response.ApiErrorCode;
import com.authplatform.backend.common.service.email.EmailService;
import com.authplatform.backend.dto.request.VerifyEmailRequest;
import com.authplatform.backend.entity.User;
import com.authplatform.backend.entity.UserOTP;
import com.authplatform.backend.exception.UserNotFoundException;
import com.authplatform.backend.repository.UserOtpRepository;
import com.authplatform.backend.repository.UserRepository;
import com.authplatform.backend.security.otp.OtpEncoder;
import com.authplatform.backend.security.otp.OtpGenerator;
import com.authplatform.backend.security.otp.OtpProperties;
import com.authplatform.backend.security.otp.OtpPurpose;

import com.authplatform.backend.service.impl.EmailOtpServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EmailOtpServiceImpl.
 * Tests SendVerificationOtpTests and VerifyVerificationOtp business logic independently using Mockito
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EmailOtpServiceImplUnit Tests")
public class EmailOtpServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserOtpRepository userOtpRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private OtpGenerator otpGenerator;

    @Mock
    private OtpEncoder otpEncoder;

    @Mock
    private OtpProperties otpProperties;

    @InjectMocks
    private EmailOtpServiceImpl emailOtpService;

    private static final String EMAIL = "test@example.com";
    private static final String OTP = "123456";
    private static final String HASHED_OTP = "hashed-otp";

    private User user;

    @BeforeEach
    void setUp() {
        user = mock(User.class);
    }

    // =========================================================
    // SEND VERIFICATION OTP TESTS
    // =========================================================

    @Nested
    @DisplayName("sendVerificationOtp")
    class SendVerificationOtpTests {

        @Test
        void sendVerificationOtp_shouldSendOtpSuccessfully() {


            // Arrange
            when(user.getEmail()).thenReturn(EMAIL);

            when(userRepository.findByEmail(EMAIL))
                    .thenReturn(Optional.of(user));

            when(otpGenerator.generateOtp())
                    .thenReturn(OTP);

            when(otpEncoder.encode(OtpPurpose.EMAIL_VERIFICATION, OTP))
                    .thenReturn(HASHED_OTP);

            when(otpGenerator.getOtpExpiration())
                    .thenReturn(300L);

            when(otpProperties.maxAttempts())
                    .thenReturn(5);

            when(userOtpRepository.save(any(UserOTP.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            emailOtpService.sendVerificationOtp(EMAIL);

            // Assert
            verify(userRepository).findByEmail(EMAIL);

            verify(userOtpRepository).deleteAllByUserEmail(EMAIL);

            verify(otpGenerator).generateOtp();

            verify(otpEncoder).encode(
                    OtpPurpose.EMAIL_VERIFICATION,
                    OTP
            );

            ArgumentCaptor<UserOTP> otpCaptor =
                    ArgumentCaptor.forClass(UserOTP.class);

            verify(userOtpRepository).save(otpCaptor.capture());

            UserOTP savedOtp = otpCaptor.getValue();

            assertThat(savedOtp).isNotNull();
            assertThat(savedOtp.getHashedOtp()).isEqualTo(HASHED_OTP);
            assertThat(savedOtp.getAttempt()).isZero();
            assertThat(savedOtp.getMaxAttempt()).isEqualTo(5);
            assertThat(savedOtp.getExpiresAt())
                    .isAfter(Instant.now());

            verify(emailService).sendVerificationOtp(EMAIL, OTP);
        }

        @Test
        void sendVerificationOtp_shouldThrowExceptionWhenUserNotFound() {

            // Arrange
            when(userRepository.findByEmail(EMAIL))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() ->
                    emailOtpService.sendVerificationOtp(EMAIL))
                    .isInstanceOf(UserNotFoundException.class);

            verify(userRepository).findByEmail(EMAIL);

            verify(userOtpRepository, never())
                    .deleteAllByUserEmail(anyString());

            verify(otpGenerator, never()).generateOtp();

            verify(userOtpRepository, never()).save(any());

            verify(emailService, never())
                    .sendVerificationOtp(anyString(), anyString());
        }

        @Test
        void sendVerificationOtp_shouldDeletePreviousOtpBeforeSavingNewOtp() {

            // Arrange
            when(user.getEmail()).thenReturn(EMAIL);
            when(userRepository.findByEmail(EMAIL))
                    .thenReturn(Optional.of(user));

            when(otpGenerator.generateOtp()).thenReturn(OTP);

            when(otpEncoder.encode(OtpPurpose.EMAIL_VERIFICATION, OTP))
                    .thenReturn(HASHED_OTP);

            when(otpGenerator.getOtpExpiration()).thenReturn(300L);

            when(otpProperties.maxAttempts()).thenReturn(5);

            // Act
            emailOtpService.sendVerificationOtp(EMAIL);

            // Assert
            var inOrder = inOrder(userOtpRepository);

            inOrder.verify(userOtpRepository)
                    .deleteAllByUserEmail(EMAIL);

            inOrder.verify(userOtpRepository)
                    .save(any(UserOTP.class));
        }
    }

    // =========================================================
    // VERIFY VERIFICATION OTP TESTS
    // =========================================================

    @Nested
    @DisplayName("verifyVerificationOtp")
    class VerifyVerificationOtpTests {

        private VerifyEmailRequest request;
        private UserOTP verificationOtp;

        @BeforeEach
        void setUp() {

            request = new VerifyEmailRequest(EMAIL, OTP);

            verificationOtp = mock(UserOTP.class);
        }

        private void givenValidOtpRecord() {

            when(userRepository.findByEmail(EMAIL))
                    .thenReturn(Optional.of(user));

            when(userOtpRepository.findFirstByUser(user))
                    .thenReturn(Optional.of(verificationOtp));

            when(verificationOtp.getAttempt()).thenReturn(0);

            when(verificationOtp.getMaxAttempt()).thenReturn(5);

            when(verificationOtp.getExpiresAt())
                    .thenReturn(Instant.now().plusSeconds(300));

            when(verificationOtp.getHashedOtp())
                    .thenReturn(HASHED_OTP);
        }

        @Test
        void verifyVerificationOtp_shouldVerifyEmailSuccessfully() {

            // Arrange
            givenValidOtpRecord();

            when(otpEncoder.matches(
                    OtpPurpose.EMAIL_VERIFICATION,
                    OTP,
                    HASHED_OTP
            )).thenReturn(true);

            // Act
            emailOtpService.verifyVerificationOtp(request);

            // Assert
            verify(verificationOtp).setAttempt(1);

            verify(otpEncoder).matches(
                    OtpPurpose.EMAIL_VERIFICATION,
                    OTP,
                    HASHED_OTP
            );

            verify(user).setEmailVerified(true);

            verify(userRepository).save(user);

            verify(userOtpRepository).delete(verificationOtp);
        }

        @Test
        void verifyVerificationOtp_shouldThrowExceptionWhenUserNotFound() {

            // Arrange
            when(userRepository.findByEmail(EMAIL))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() ->
                    emailOtpService.verifyVerificationOtp(request))
                    .isInstanceOf(UserNotFoundException.class);

            verify(userRepository).findByEmail(EMAIL);

            verify(userOtpRepository, never())
                    .findFirstByUser(any());

            verifyNoInteractions(otpEncoder);
        }

        @Test
        void verifyVerificationOtp_shouldThrowExceptionWhenOtpNotFound() {

            // Arrange
            when(userRepository.findByEmail(EMAIL))
                    .thenReturn(Optional.of(user));

            when(userOtpRepository.findFirstByUser(user))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() ->
                    emailOtpService.verifyVerificationOtp(request))
                    .isInstanceOf(OtpVerificationException.class)
                    .hasMessageContaining(
                            ApiErrorCode.OTP_NOT_FOUND.message()
                    );

            verify(user, never()).setEmailVerified(true);

            verify(userRepository, never()).save(any());

            verify(userOtpRepository, never()).delete(any());

            verifyNoInteractions(otpEncoder);
        }

        @Test
        void verifyVerificationOtp_shouldRejectOtpWhenMaxAttemptsExceeded() {

            // Arrange
            when(userRepository.findByEmail(EMAIL))
                    .thenReturn(Optional.of(user));

            when(userOtpRepository.findFirstByUser(user))
                    .thenReturn(Optional.of(verificationOtp));

            when(verificationOtp.getAttempt()).thenReturn(5);

            when(verificationOtp.getMaxAttempt()).thenReturn(5);

            // Act & Assert
            assertThatThrownBy(() ->
                    emailOtpService.verifyVerificationOtp(request))
                    .isInstanceOf(OtpVerificationException.class)
                    .hasMessageContaining(
                            ApiErrorCode.OTP_RATE_LIMIT_EXCEEDED.message()
                    );

            // Verify
            verify(verificationOtp, never()).setAttempt(anyInt());

            verify(otpEncoder, never()).matches(
                    any(),
                    anyString(),
                    anyString()
            );

            verify(user, never()).setEmailVerified(true);

            verify(userRepository, never()).save(any());

            verify(userOtpRepository, never()).delete(any());
        }

        @Test
        void verifyVerificationOtp_shouldRejectExpiredOtp() {

            // Arrange
            when(userRepository.findByEmail(EMAIL))
                    .thenReturn(Optional.of(user));

            when(userOtpRepository.findFirstByUser(user))
                    .thenReturn(Optional.of(verificationOtp));

            when(verificationOtp.getAttempt()).thenReturn(0);

            when(verificationOtp.getMaxAttempt()).thenReturn(5);

            when(verificationOtp.getExpiresAt())
                    .thenReturn(Instant.now().plusSeconds(300));

            when(verificationOtp.getExpiresAt())
                    .thenReturn(Instant.now().minusSeconds(60));

            // Act & Assert
            assertThatThrownBy(() ->
                    emailOtpService.verifyVerificationOtp(request))
                    .isInstanceOf(OtpVerificationException.class)
                    .hasMessageContaining(
                            ApiErrorCode.OTP_EXPIRED.message()
                    );

            verify(verificationOtp).setAttempt(1);

            verify(otpEncoder, never()).matches(
                    any(),
                    anyString(),
                    anyString()
            );

            verify(userRepository, never()).save(any());

            verify(userOtpRepository, never()).delete(any());
        }

        @Test
        void verifyVerificationOtp_shouldRejectInvalidOtp() {

            // Arrange
            givenValidOtpRecord();

            when(otpEncoder.matches(
                    OtpPurpose.EMAIL_VERIFICATION,
                    OTP,
                    HASHED_OTP
            )).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() ->
                    emailOtpService.verifyVerificationOtp(request))
                    .isInstanceOf(OtpVerificationException.class)
                    .hasMessageContaining(
                            ApiErrorCode.INVALID_OTP.message()
                    );

            verify(verificationOtp).setAttempt(1);

//            verify(user).setEmailVerified(false);

            verify(userRepository, never()).save(any());

            verify(userOtpRepository, never()).delete(any());
        }

        @Test
        void verifyVerificationOtp_shouldAllowLastRemainingAttempt() {

            // Arrange
            givenValidOtpRecord();

            when(verificationOtp.getAttempt()).thenReturn(4);

            when(otpEncoder.matches(
                    OtpPurpose.EMAIL_VERIFICATION,
                    OTP,
                    HASHED_OTP
            )).thenReturn(true);

            // Act
            emailOtpService.verifyVerificationOtp(request);

            // Assert
            verify(verificationOtp).setAttempt(5);

            verify(user).setEmailVerified(true);

            verify(userRepository).save(user);

            verify(userOtpRepository).delete(verificationOtp);
        }
    }
}

