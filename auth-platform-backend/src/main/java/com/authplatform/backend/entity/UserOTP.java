package com.authplatform.backend.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
public class UserOTP {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "hashed_otp", nullable = false)
    private String hashedOtp;

    @Column(name = "attempt", nullable = false)
    private int attempt;

    @Column(name = "max_attempt", nullable = false)
    private int maxAttempt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    public UserOTP() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getHashedOtp() {
        return hashedOtp;
    }

    public void setHashedOtp(String hashedOtp) {
        this.hashedOtp = hashedOtp;
    }

    public int getAttempt() {
        return attempt;
    }

    public void setAttempt(int attempt) {
        this.attempt = attempt;
    }

    public int getMaxAttempt() {
        return maxAttempt;
    }

    public void setMaxAttempt(int maxAttempt) {
        this.maxAttempt = maxAttempt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public static UserOTP of(User user, String hashedOtp, int attempt, int maxAttempt, Instant expiresAt) {
        UserOTP userOTP = new UserOTP();
        userOTP.setUser(user);
        userOTP.setHashedOtp(hashedOtp);
        userOTP.setAttempt(attempt);
        userOTP.setMaxAttempt(maxAttempt);
        userOTP.setExpiresAt(expiresAt);
        return userOTP;
    }
}
