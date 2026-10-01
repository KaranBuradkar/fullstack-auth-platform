package com.authplatform.backend.repository;

import com.authplatform.backend.entity.EmailVerificationOtp;
import com.authplatform.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationOtpRepository extends JpaRepository<EmailVerificationOtp, Long> {

    Optional<EmailVerificationOtp> findFirstByUser(User user);

    void deleteAllByUserEmail(String email);
}
