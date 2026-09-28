package com.authplatform.backend.security;

import com.authplatform.backend.common.exception.ApiAuthenticationException;
import com.authplatform.backend.common.response.ApiErrorCode;
import com.authplatform.backend.config.JwtProperties;
import com.authplatform.backend.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final Logger log = LoggerFactory.getLogger(JwtService.class);
    private final JwtProperties jwtProperties;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtProperties.secretKey().getBytes(StandardCharsets.UTF_8));
    }

    private String generateToken(User user, Long expiration) {
        return Jwts.builder()
                .subject(user.getUsername())
                .signWith(getSecretKey())
                .claim("user_id", user.getId().toString())
                .claim("role", user.getRole().name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .compact();
    }

    private Claims getChaim(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            throw new ApiAuthenticationException(ApiErrorCode.INVALID_TOKEN);
        }
    }

    public Date getExpiry(String token) {
        Claims chaim = getChaim(token);
        return chaim.getExpiration();
    }

    public boolean isValidAndNotExpiredToken(String token) {
        Date expiry = getExpiry(token);
        return expiry.after(new Date());
    }

    public String getUsernameFromToken(String token) {
        try {
            Claims chaim = getChaim(token);
            return chaim.getSubject();
        } catch (Exception e) {
            log.warn(e.getMessage());
            throw e;
        }
    }

    public String generateAccessToken(User user) {
        return generateToken(user, jwtProperties.accessTokenExpiration());
    }


    @Transactional
    public String generateNewRefreshToken(User user) {
        return generateToken(user, jwtProperties.refreshTokenExpiration());
    }

}
