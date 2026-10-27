// Centralize endpoints of backend api calling
// https://authplatform.com/api/v1/ + API_ENDPOINTS
export const API_ENDPOINTSTES = {

    // Authentication backend api endpoints
    AUTH: {
        LOGIN: "/auth/login",
        REGISTER: "/auth/register",
        REFRESH: "/auth/refresh",
        LOGOUT: "/auth/logout",
        SEND_EMAIL_OTP: "/auth/otp/email/send",
        VERIFY_EMAIL_OTP: "/auth/otp/email/verify",
        CHANGE_PASSWORD: "/auth/password/change",
        FORGOT_PASSWORD: "/auth/password/forgot",
        RESET_PASSWORD: "/auth/password/reset",
    },

    // User profile related backend api endpoints
    USER: {
        PROFILE: "/users/me"
    }
};