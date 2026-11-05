package com.authplatform.backend.service.impl;

import com.authplatform.backend.common.exception.ApiAuthenticationException;
import com.authplatform.backend.common.exception.ApiException;
import com.authplatform.backend.common.exception.BadRequestException;
import com.authplatform.backend.common.exception.OtpVerificationException;
import com.authplatform.backend.common.response.ApiErrorCode;
import com.authplatform.backend.common.service.email.EmailService;
import com.authplatform.backend.entity.UserOTP;
import com.authplatform.backend.repository.UserOtpRepository;
import com.authplatform.backend.security.otp.OtpEncoder;
import com.authplatform.backend.security.otp.OtpGenerator;
import com.authplatform.backend.dto.request.ChangePasswordRequest;
import com.authplatform.backend.dto.request.ForgotPasswordRequest;
import com.authplatform.backend.dto.request.ResetPasswordRequest;
import com.authplatform.backend.entity.User;
import com.authplatform.backend.exception.UserNotFoundException;
import com.authplatform.backend.repository.UserRepository;
import com.authplatform.backend.repository.UserTokenRepository;
import com.authplatform.backend.security.otp.OtpProperties;
import com.authplatform.backend.security.otp.OtpPurpose;
import com.authplatform.backend.service.PasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class PasswordServiceImpl implements PasswordService {

    private static final Logger log = LoggerFactory.getLogger(PasswordServiceImpl.class);
    private final UserRepository userRepository;
    private final UserOtpRepository userOtpRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final OtpGenerator otpGenerator;
    private final UserTokenRepository userTokenRepository;
    private final OtpEncoder otpEncoder;
    private final OtpProperties otpProperties;

    public PasswordServiceImpl(
            UserRepository userRepository,
            UserOtpRepository userOtpRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService,
            OtpGenerator otpGenerator,
            UserTokenRepository userTokenRepository,
            OtpEncoder otpEncoder,
            OtpProperties otpProperties
    ) {
        this.userRepository = userRepository;
        this.userOtpRepository = userOtpRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.otpGenerator = otpGenerator;
        this.userTokenRepository = userTokenRepository;
        this.otpEncoder = otpEncoder;
        this.otpProperties = otpProperties;
    }

    @Transactional
    @Override
    public void forgotPassword(ForgotPasswordRequest request) {

        // Fetch user
        User user = userRepository.findByEmail(request.email())
                        .orElseThrow(UserNotFoundException::new);

        // Remove previous reset otp
        userOtpRepository.deleteAllByUserEmail(user.getEmail());

        // Generate otp
        String otp = otpGenerator.generateOtp();

        // Create entity to save in db
        UserOTP resetOtp = UserOTP.of(
                user,
                otpEncoder.encode(OtpPurpose.PASSWORD_RESET, otp),
                0,
                otpProperties.maxAttempts(),
                Instant.now().plusSeconds(otpGenerator.getOtpExpiration())
        );

        // save in db
        userOtpRepository.save(resetOtp);

        // send otp through email
        emailService.sendVerificationOtp(user.getEmail(), otp);
    }

    @Transactional(rollbackFor = OtpVerificationException.class)
    @Override
    public void resetPassword(ResetPasswordRequest request) {

        // Fetch reset otp by user email
        UserOTP resetOtp = userOtpRepository
                        .findFirstByUserEmail(request.email())
                        .orElseThrow(() -> new ApiException(ApiErrorCode.INVALID_REQUEST));

        // Update otp verification attempts
        resetOtp.setAttempt(resetOtp.getAttempt() + 1);

        // Check verify attempt limit
        if(resetOtp.getAttempt() > resetOtp.getMaxAttempt()) {
            throw new OtpVerificationException(ApiErrorCode.OTP_RATE_LIMIT_EXCEEDED);
        }

        // Check otp is expired
        if(resetOtp.getExpiresAt().isBefore((Instant.now()))) {
            throw new OtpVerificationException(ApiErrorCode.OTP_EXPIRED);
        }

        // Check otp is match to request.otp
        boolean valid = otpEncoder.matches(OtpPurpose.PASSWORD_RESET,
                request.otp(), resetOtp.getHashedOtp());
        if(!valid) {
            log.info("OTP received and database is mismatched in {}", OtpPurpose.PASSWORD_RESET);
            throw new OtpVerificationException(ApiErrorCode.INVALID_OTP);
        }

        User user = resetOtp.getUser();

        String encodedPassword = passwordEncoder.encode(request.newPassword());

        user.setPassword(encodedPassword);

        userRepository.save(user);

        userOtpRepository.delete(resetOtp);
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
