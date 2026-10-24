const SESSION_TOKEN_KEYS = {
  USER: {
    EMAIL: "user_email",
    EMAIL_VERIFY_OTP_SEND: "email_verify_otp_sent",
  }
};

export const tokenSessionStorage = {
    // User email store
    saveUserEmail(email) {
        if(email == null || email == "") {
            console.warn("Email is null or empty");
            return;
        }

        sessionStorage.setItem(
            SESSION_TOKEN_KEYS.USER.EMAIL, 
            email.trim().toLowerCase()
        );
    },

    // Get user email
    getUserEmail() {
        return sessionStorage.getItem(SESSION_TOKEN_KEYS.USER.EMAIL) || "";
    },

    // Clear email
    clearUserEmail() {
        return sessionStorage.removeItem(SESSION_TOKEN_KEYS.USER.EMAIL);
    },

    // OTP SEND
    saveOtpSent(otpSent) {
        if(!email) return;

        sessionStorage.setItem(
            SESSION_TOKEN_KEYS.USER.EMAIL_VERIFY_OTP_SEND, 
            email.trim().toLowerCase()
        );
    },

    // Get OTP send
    getOtpSent() {
        return sessionStorage.getItem(SESSION_TOKEN_KEYS.USER.EMAIL_VERIFY_OTP_SEND) || false;
    },

    // Clear Otp sent status
    clearOtpSent() {
        return sessionStorage.removeItem(SESSION_TOKEN_KEYS.USER.EMAIL_VERIFY_OTP_SEND);
    }
}

