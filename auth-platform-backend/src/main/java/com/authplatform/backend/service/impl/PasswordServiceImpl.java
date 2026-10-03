package com.authplatform.backend.service.impl;

import com.authplatform.backend.common.exception.ApiAuthenticationException;
import com.authplatform.backend.common.exception.ApiException;
import com.authplatform.backend.common.exception.BadRequestException;
import com.authplatform.backend.common.response.ApiErrorCode;
import com.authplatform.backend.common.service.email.EmailService;
import com.authplatform.backend.config.OtpProperties;
import com.authplatform.backend.dto.request.ChangePasswordRequest;
import com.authplatform.backend.dto.request.ForgotPasswordRequest;
import com.authplatform.backend.dto.request.ResetPasswordRequest;
import com.authplatform.backend.entity.PasswordResetOtp;
import com.authplatform.backend.entity.User;
import com.authplatform.backend.exception.UserNotFoundException;
import com.authplatform.backend.repository.PasswordResetOtpRepository;
import com.authplatform.backend.repository.UserRepository;
import com.authplatform.backend.repository.UserTokenRepository;
import com.authplatform.backend.service.PasswordService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final UserTokenRepository userTokenRepository;

    public PasswordServiceImpl(
            UserRepository userRepository,
            PasswordResetOtpRepository passwordResetOtpRepository,
            OtpProperties otpProperties, PasswordEncoder passwordEncoder,
            EmailService emailService, UserTokenRepository userTokenRepository
    ) {
        this.userRepository = userRepository;
        this.passwordResetOtpRepository = passwordResetOtpRepository;
        this.otpProperties = otpProperties;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.userTokenRepository = userTokenRepository;
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

    @Transactional
    @Override
    public void changePassword(ChangePasswordRequest request) {

        // 1. Get current user
        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();
        if (authentication == null) {
            throw new ApiAuthenticationException(ApiErrorCode.UNAUTHORIZED);
        }
        String email = authentication.getName();

        // 2. Find user
        User user = userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);

        // 3. Verify current password
        if(!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        // 4. Prevent using the same password
        if(passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new BadRequestException("New password must be different from current password");
        }

        // 5. Encode new password
        String encodedPassword = passwordEncoder.encode(request.newPassword());

        // 6. Update password
        user.setPassword(encodedPassword);
        userRepository.save(user);

        // 7. Revoke existing token
        userTokenRepository.revokeAllTokensByUserId(user.getId());
    }
}
