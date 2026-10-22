const SESSION_TOKEN_KEYS = {
  USER: {
    EMAIL: "user_email",
  }
};

export const tokenSessionStorage = {
    saveUserEmail(email) {
        if(!email) return;

        sessionStorage.setItem(
            SESSION_TOKEN_KEYS.USER.EMAIL, 
            email.trim().toLowerCase()
        );
    },

    getUserEmail() {
        return sessionStorage.getItem(SESSION_TOKEN_KEYS.USER.EMAIL) || "";
    },

    clearUserEmail() {
        return sessionStorage.removeItem(SESSION_TOKEN_KEYS.USER.EMAIL);
    }
}

