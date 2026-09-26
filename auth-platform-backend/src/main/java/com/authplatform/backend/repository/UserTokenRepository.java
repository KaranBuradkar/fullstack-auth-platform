package com.authplatform.backend.repository;

import com.authplatform.backend.entity.User;
import com.authplatform.backend.entity.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserTokenRepository extends JpaRepository<UserToken, Long> {
    Optional<UserToken> findByUser(User user);
}