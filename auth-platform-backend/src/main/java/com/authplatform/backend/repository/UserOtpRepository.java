package com.authplatform.backend.repository;

import com.authplatform.backend.entity.UserOTP;
import com.authplatform.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserOtpRepository extends JpaRepository<UserOTP, Long> {

    Optional<UserOTP> findFirstByUser(User user);

    Optional<UserOTP> findFirstByUserEmail(String email);

    void deleteAllByUserEmail(String email);
}
