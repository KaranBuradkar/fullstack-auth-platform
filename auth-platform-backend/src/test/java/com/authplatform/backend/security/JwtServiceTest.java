
package com.authplatform.backend.security;

import com.authplatform.backend.common.exception.ApiAuthenticationException;
import com.authplatform.backend.config.JwtProperties;
import com.authplatform.backend.entity.enums.Role;
import com.authplatform.backend.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JwtServiceTest {

    private static final String SECRET_KEY =
            "gcghxnukvxkhvbkckhgxhfclngvxcxhhfxfhxchgcghvhgkmsdpfhjkc";
    private static final long ACCESS_EXPIRATION = 15 * 60 * 1000L;
    private static final long REFRESH_EXPIRATION = 7 * 24 * 60 * 60 * 1000L;

    private JwtProperties jwtProperties;
    private JwtService jwtService;
    private User user;

    private SecretKey secretKey;

    @BeforeEach
    void setUp() {
        jwtProperties = mock(JwtProperties.class);

        when(jwtProperties.secretKey()).thenReturn(SECRET_KEY);
        when(jwtProperties.accessTokenExpiration()).thenReturn(ACCESS_EXPIRATION);
        when(jwtProperties.refreshTokenExpiration()).thenReturn(REFRESH_EXPIRATION);

        jwtService = new JwtService(jwtProperties);

        secretKey = Keys.hmacShaKeyFor(
                SECRET_KEY.getBytes(StandardCharsets.UTF_8)
        );

        user = mock(User.class);

        when(user.getId()).thenReturn(1L);
        when(user.getUsername()).thenReturn("karan@dev.com");
        when(user.getRole()).thenReturn(Role.CLIENT);
    }

    // --------------------------------------------------
    // generateAccessToken()
    // --------------------------------------------------

    @Test
    void generateAccessToken_shouldReturnValidJwt() {
        String token = jwtService.generateAccessToken(user);

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void generateAccessToken_shouldContainCorrectClaims() {
        String token = jwtService.generateAccessToken(user);

        var claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertThat(claims.getSubject()).isEqualTo(user.getUsername());
        assertThat(claims.get("user_id", String.class)).isEqualTo(user.getId().toString());
        assertThat(claims.get("role", String.class)).isEqualTo(user.getRole().toString());
    }

    @Test
    void generateAccessToken_shouldHaveCorrectExpiration() {
        long beforeGeneration = System.currentTimeMillis();

        String token = jwtService.generateAccessToken(user);

        long afterGeneration = System.currentTimeMillis();

        Date expiry = jwtService.getExpiry(token);

        long actualExpiry = expiry.getTime();

        assertThat(actualExpiry)
                .isGreaterThanOrEqualTo(beforeGeneration + ACCESS_EXPIRATION - 1000)
                .isLessThanOrEqualTo(afterGeneration + ACCESS_EXPIRATION);
    }

    // --------------------------------------------------
    // generateNewRefreshToken()
    // --------------------------------------------------

    @Test
    void generateNewRefreshToken_shouldReturnValidJwt() {
        String token = jwtService.generateNewRefreshToken(user);

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void generateNewRefreshToken_shouldHaveRefreshExpiration() {
        long beforeGeneration = new Date(System.currentTimeMillis()).getTime();

        String token = jwtService.generateNewRefreshToken(user);

        long afterGeneration = new Date(System.currentTimeMillis()).getTime();

        Date expiry = jwtService.getExpiry(token);

        assertThat(expiry.getTime())
                .isBetween(
                        beforeGeneration + REFRESH_EXPIRATION - 1000,
                        afterGeneration + REFRESH_EXPIRATION
                );
    }

    // --------------------------------------------------
    // getUsernameFromToken()
    // --------------------------------------------------

    @Test
    void getUsernameFromToken_shouldReturnCorrectUsername() {
        String token = jwtService.generateAccessToken(user);

        String username = jwtService.getUsernameFromToken(token);

        assertThat(username).isEqualTo(user.getUsername());
    }

    @Test
    void getUsernameFromToken_shouldThrowExceptionForInvalidToken() {
        assertThatThrownBy(
                () -> jwtService.getUsernameFromToken("invalid.jwt.token")
        ).isInstanceOf(ApiAuthenticationException.class);
    }

    // --------------------------------------------------
    // getExpiry()
    // --------------------------------------------------

    @Test
    void getExpiry_shouldReturnCorrectExpiration() {
        String token = jwtService.generateAccessToken(user);

        Date expiry = jwtService.getExpiry(token);

        assertThat(expiry).isAfter(new Date());
    }

    @Test
    void getExpiry_shouldThrowExceptionForInvalidToken() {
        assertThatThrownBy(
                () -> jwtService.getExpiry("invalid.jwt.token")
        ).isInstanceOf(ApiAuthenticationException.class);
    }

    // --------------------------------------------------
    // isValidAndNotExpiredToken()
    // --------------------------------------------------

    @Test
    void isValidAndNotExpiredToken_shouldReturnTrueForValidAccessToken() {
        String token = jwtService.generateAccessToken(user);

        boolean result = jwtService.isValidAndNotExpiredToken(token);

        assertThat(result).isTrue();
    }

    @Test
    void isValidAndNotExpiredToken_shouldThrowExceptionForRefreshToken() {
        String refreshToken = jwtService.generateNewRefreshToken(user);

        assertThatThrownBy(
                () -> jwtService.isValidAndNotExpiredToken(refreshToken)
        ).isInstanceOf(ApiAuthenticationException.class);
    }

    @Test
    void isValidAndNotExpiredToken_shouldThrowExceptionForTamperedToken() {
        String token = jwtService.generateAccessToken(user);

        String tamperedToken = token.substring(0, token.length() - 2) + "ab";

        assertThatThrownBy(
                () -> jwtService.isValidAndNotExpiredToken(tamperedToken)
        ).isInstanceOf(ApiAuthenticationException.class);
    }

    @Test
    void isValidAndNotExpiredToken_shouldThrowExceptionForExpiredToken() {
        String expiredToken = Jwts.builder()
                .subject(user.getUsername())
                .claim("user_id", user.getId())
                .claim("role", user.getRole())
                .issuedAt(new Date(System.currentTimeMillis() - 10_000))
                .expiration(new Date(System.currentTimeMillis() - 1_000))
                .signWith(secretKey)
                .compact();

        assertThatThrownBy(
                () -> jwtService.isValidAndNotExpiredToken(expiredToken)
        ).isInstanceOf(ApiAuthenticationException.class);
    }
}
