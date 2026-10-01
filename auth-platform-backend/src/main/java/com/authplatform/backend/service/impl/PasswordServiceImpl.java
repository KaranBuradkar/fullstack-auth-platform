package com.authplatform.backend.service.impl;

import com.authplatform.backend.common.exception.ApiException;
import com.authplatform.backend.common.response.ApiErrorCode;
import com.authplatform.backend.common.service.email.EmailService;
import com.authplatform.backend.config.OtpProperties;
import com.authplatform.backend.dto.request.ForgotPasswordRequest;
import com.authplatform.backend.dto.request.ResetPasswordRequest;
import com.authplatform.backend.entity.PasswordResetOtp;
import com.authplatform.backend.entity.User;
import com.authplatform.backend.exception.UserNotFoundException;
import com.authplatform.backend.repository.PasswordResetOtpRepository;
import com.authplatform.backend.repository.UserRepository;
import com.authplatform.backend.service.PasswordService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class PasswordServiceImpl implements PasswordService {

    private final UserRepository userRepository;
    private final PasswordResetOtpRepository passwordResetOtpRepository;
    private final OtpProperties otpProperties;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public PasswordServiceImpl(
            UserRepository userRepository,
            PasswordResetOtpRepository passwordResetOtpRepository,
            OtpProperties otpProperties, PasswordEncoder passwordEncoder,
            EmailService emailService
    ) {
        this.userRepository = userRepository;
        this.passwordResetOtpRepository = passwordResetOtpRepository;
        this.otpProperties = otpProperties;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Transactional
    @Override
    public void forgotPassword(ForgotPasswordRequest request) {

        User user = userRepository.findByEmail(request.email())
                        .orElseThrow(UserNotFoundException::new);

        // Remove previous reset otp
        passwordResetOtpRepository.deleteAllByUser(user);

        String otp = otpProperties.generateOtp();

        PasswordResetOtp resetOtp = new PasswordResetOtp();
        resetOtp.setOtp(otp);
        resetOtp.setUser(user);
        resetOtp.setExpiresAt(Instant.now().plusSeconds(otpProperties.getOtpExpiration()));
        resetOtp.setCreatedAt(Instant.now());

        PasswordResetOtp savePasswordReset = passwordResetOtpRepository.save(resetOtp);

        emailService.sendVerificationOtp(user.getEmail(), savePasswordReset.getOtp());
    }

    @Transactional
    @Override
    public void resetPassword(ResetPasswordRequest request) {

        PasswordResetOtp resetOtp = passwordResetOtpRepository
                        .findByUserEmail(request.email())
                        .orElseThrow(() -> new ApiException(ApiErrorCode.INVALID_REQUEST));

        if (resetOtp.getExpiresAt().isBefore(Instant.now())) {
            throw new ApiException(ApiErrorCode.OTP_EXPIRED);
        }

        User user = resetOtp.getUser();

        String encodedPassword = passwordEncoder.encode(request.newPassword());

        user.setPassword(encodedPassword);

        userRepository.save(user);

        passwordResetOtpRepository.delete(resetOtp);
    }
}
