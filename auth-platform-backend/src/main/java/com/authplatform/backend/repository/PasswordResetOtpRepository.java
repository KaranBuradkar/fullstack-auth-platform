package com.authplatform.backend.repository;

import com.authplatform.backend.entity.PasswordResetOtp;
import com.authplatform.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetOtpRepository
        extends JpaRepository<PasswordResetOtp, Long> {

    Optional<PasswordResetOtp> findByUserEmail(String email);

    void deleteAllByUser(User user);
}