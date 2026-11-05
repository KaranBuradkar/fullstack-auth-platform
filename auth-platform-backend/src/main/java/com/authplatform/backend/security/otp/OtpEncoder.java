package com.authplatform.backend.security.otp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HexFormat;

@Service
public class OtpEncoder {

    private final String HASH_ALGORITHM = "HmacSHA256";
    private final SecretKeySpec secretKey;

    public OtpEncoder(@Autowired OtpProperties otpProperties) {
        this.secretKey = new SecretKeySpec(
                otpProperties.secretKey()
                        .getBytes(StandardCharsets.UTF_8),
                HASH_ALGORITHM
        );
    }

    public String encode(OtpPurpose purpose, String otp) {
        try {
            Mac mac = Mac.getInstance(HASH_ALGORITHM);
            mac.init(secretKey);

            String payload = purpose + ":" + otp;

            byte[] hash = mac.doFinal(
                    payload.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("OTP encoding failed", ex);
        }
    }

    public boolean matches(OtpPurpose purpose, String rawOtp, String encodedOtp) {
        if (rawOtp == null || encodedOtp == null) {
            return false;
        }

        String calculatedHash = encode(purpose, rawOtp);

        return MessageDigest.isEqual(
                calculatedHash.getBytes(StandardCharsets.UTF_8),
                encodedOtp.getBytes(StandardCharsets.UTF_8)
        );
    }
}