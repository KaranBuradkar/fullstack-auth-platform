package com.authplatform.backend.service.impl;

import com.authplatform.backend.common.exception.OtpVerificationException;
import com.authplatform.backend.common.response.ApiErrorCode;
import com.authplatform.backend.entity.UserOTP;
import com.authplatform.backend.security.otp.OtpGenerator;
import com.authplatform.backend.dto.request.VerifyEmailRequest;
import com.authplatform.backend.entity.User;
import com.authplatform.backend.exception.UserNotFoundException;
import com.authplatform.backend.repository.UserOtpRepository;
import com.authplatform.backend.repository.UserRepository;
import com.authplatform.backend.security.otp.OtpEncoder;
import com.authplatform.backend.security.otp.OtpProperties;
import com.authplatform.backend.security.otp.OtpPurpose;
import com.authplatform.backend.service.EmailOtpService;
import com.authplatform.backend.common.service.email.EmailService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class EmailOtpServiceImpl implements EmailOtpService {

    private static final Logger log = LoggerFactory.getLogger(EmailOtpServiceImpl.class);
    private final UserRepository userRepository;
    private final UserOtpRepository userOtpRepository;
    private final EmailService emailService;
    private final OtpGenerator otpGenerator;
    private final OtpEncoder otpEncoder;
    private final OtpProperties otpProperties;

    public EmailOtpServiceImpl(
            UserRepository userRepository,
            UserOtpRepository userOtpRepository,
            EmailService emailService,
            OtpGenerator otpGenerator,
            OtpEncoder otpEncoder, OtpProperties otpProperties
    ) {
        this.userRepository = userRepository;
        this.userOtpRepository = userOtpRepository;
        this.emailService = emailService;
        this.otpGenerator = otpGenerator;
        this.otpEncoder = otpEncoder;
        this.otpProperties = otpProperties;
    }

    @Transactional
    @Override
    public void sendVerificationOtp(String email) {
        // Fetch user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);

        // Delete Previous never usable otp entries
        userOtpRepository.deleteAllByUserEmail(user.getEmail());

        // generate otp
        final String otp = otpGenerator.generateOtp();

        // Create UserOTP
        UserOTP verificationOtp = UserOTP.of(
                user,
                otpEncoder.encode(OtpPurpose.EMAIL_VERIFICATION, otp),
                0,
                otpProperties.maxAttempts(),
                Instant.now().plusSeconds(otpGenerator.getOtpExpiration())
        );

        UserOTP saveEmailOtp = userOtpRepository.save(verificationOtp);

        // Send Mail by EmailService
        emailService.sendVerificationOtp(user.getEmail(), otp);
    }

    @Transactional(dontRollbackOn = OtpVerificationException.class)
    @Override
    public void verifyVerificationOtp(VerifyEmailRequest request) {

        // Fetch User by user email
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(UserNotFoundException::new);

        // Fetch UserOTP
        UserOTP verificationOtp = userOtpRepository
                .findFirstByUser(user)
                .orElseThrow(() -> new OtpVerificationException(ApiErrorCode.OTP_NOT_FOUND));

        verificationOtp.setAttempt(verificationOtp.getAttempt() + 1);

        // Check verify attempt limit
        if(verificationOtp.getAttempt() > verificationOtp.getMaxAttempt()) {
            throw new OtpVerificationException(ApiErrorCode.OTP_RATE_LIMIT_EXCEEDED);
        }

        // Check otp is expired
        if(verificationOtp.getExpiresAt().isBefore((Instant.now()))) {
            throw new OtpVerificationException(ApiErrorCode.OTP_EXPIRED);
        }

        boolean valid = otpEncoder.matches(OtpPurpose.EMAIL_VERIFICATION,
                request.otp(), verificationOtp.getHashedOtp());
        // Check otp is equal to request.otp
        if(!valid) {
            log.info("Invalid email verification OTP");
            throw new OtpVerificationException(ApiErrorCode.INVALID_OTP);
        }

        // Update previous user data
        user.setEmailVerified(true);

        userRepository.save(user);
        userOtpRepository.delete(verificationOtp);
    }
}
