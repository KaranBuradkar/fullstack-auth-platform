package com.authplatform.backend.repository;

import com.authplatform.backend.entity.User;
import com.authplatform.backend.entity.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserTokenRepository extends JpaRepository<UserToken, Long> {
    Optional<UserToken> findByUser(User user);

    Optional<UserToken> findByRefreshToken(String token);

    @Modifying
    @Query("""
            UPDATE UserToken t 
            SET t.revoked = true 
            WHERE t.user.id = :userId
            """)
    void revokeAllTokensByUserId(@Param("userId") Long userId);
}