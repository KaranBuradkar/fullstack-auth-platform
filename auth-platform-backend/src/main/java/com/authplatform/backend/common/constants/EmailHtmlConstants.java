package com.authplatform.backend.common.constants;

public final class EmailHtmlConstants {

    private EmailHtmlConstants() {}

    public static String buildVerificationOtpEmail(String otp) {
        return """
                <!DOCTYPE html>
                <html>
                <body style="font-family: Arial, sans-serif;">

                    <h2>Verify your email</h2>

                    <p>
                        Thank you for registering with Auth Platform.
                    </p>

                    <p>Your verification OTP is:</p>

                    <h1>%s</h1>

                    <p>
                        This OTP will expire in 5 minutes.
                    </p>

                    <p>
                        If you did not create this account,
                        you can safely ignore this email.
                    </p>

                </body>
                </html>
                """.formatted(otp);
    }
}
