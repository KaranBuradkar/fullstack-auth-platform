package com.authplatform.backend.service.impl;

import com.authplatform.backend.common.exception.ApiException;
import com.authplatform.backend.common.response.ApiErrorCode;
import com.authplatform.backend.config.OtpProperties;
import com.authplatform.backend.dto.request.VerifyEmailRequest;
import com.authplatform.backend.entity.EmailVerificationOtp;
import com.authplatform.backend.entity.User;
import com.authplatform.backend.exception.UserNotFoundException;
import com.authplatform.backend.repository.EmailVerificationOtpRepository;
import com.authplatform.backend.repository.UserRepository;
import com.authplatform.backend.service.EmailOtpService;
import com.authplatform.backend.common.service.email.EmailService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class EmailOtpServiceImpl implements EmailOtpService {

    private final UserRepository userRepository;
    private final EmailVerificationOtpRepository emailVerificationOtpRepository;
    private final EmailService emailService;
    private final OtpProperties otpProperties;

    public EmailOtpServiceImpl(
            UserRepository userRepository,
            EmailVerificationOtpRepository emailVerificationOtpRepository,
            EmailService emailService,
            OtpProperties otpProperties
    ) {
        this.userRepository = userRepository;
        this.emailVerificationOtpRepository = emailVerificationOtpRepository;
        this.emailService = emailService;
        this.otpProperties = otpProperties;
    }

    @Transactional
    @Override
    public void sendVerificationOtp(String email) {
        // Fetch user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);

        // Delete Previous never usable otp entries
        emailVerificationOtpRepository.deleteAllByUserEmail(user.getEmail());

        // generate otp
        String otp = otpProperties.generateOtp();

        // Create EmailVerificationOtp
        EmailVerificationOtp verificationOtp = new EmailVerificationOtp();
        verificationOtp.setUser(user);
        verificationOtp.setOtp(otp);
        verificationOtp.setExpiresAt(Instant.now().plusSeconds(otpProperties.getOtpExpiration()));

        EmailVerificationOtp saveEmailOtp = emailVerificationOtpRepository.save(verificationOtp);

        // Send Mail by EmailService
        emailService.sendVerificationOtp(user.getEmail(), saveEmailOtp.getOtp());
    }

    @Transactional
    @Override
    public void verifyVerificationOtp(VerifyEmailRequest request) {

        // Fetch User by user email
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(UserNotFoundException::new);

        // Fetch EmailVerificationOtp
        EmailVerificationOtp verificationOtp = emailVerificationOtpRepository
                .findFirstByUser(user)
                .orElseThrow(() -> new ApiException(ApiErrorCode.OTP_NOT_FOUND));

        // Check otp is expired
        if(verificationOtp.getExpiresAt().isBefore((Instant.now()))) {
            throw new ApiException(ApiErrorCode.OTP_EXPIRED);
        }

        // Check otp is equal to request.otp
        if(!verificationOtp.getOtp().equals(request.otp())) {
            throw new ApiException(ApiErrorCode.INVALID_OTP);
        }

        // Update previous user data
        user.setEmailVerified(true);

        userRepository.save(user);
        emailVerificationOtpRepository.delete(verificationOtp);
    }
}
