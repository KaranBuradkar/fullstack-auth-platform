import axiosClient from "../api/axiosClient";
import { API_ENDPOINTSTES } from '../api/endpoints'

// Handles API requests related to authentication operations
export const authService = {

    // Create new user account
    async register(userData) {
        const response = await axiosClient.post(API_ENDPOINTSTES.AUTH.REGISTER, userData);
        return response.data;
    },

    // Login user account
    async login(credentials) {
        const response = await axiosClient.post(API_ENDPOINTSTES.AUTH.LOGIN, credentials);
        return response.data;
    },

    // Update user refresh token
    async refreshToken(payload) {
        const response = await axiosClient.post(API_ENDPOINTSTES.AUTH.REFRESH, payload);
        return response.data;
    },

    // Logout user account from browser and 
    // set invalid current refresh token
    async logout() {
        const response = await axiosClient.post(API_ENDPOINTSTES.AUTH.LOGOUT);
        return response.data;
    },

    // Change user password using current password
    async changePassword(payload) {
        const response = await axiosClient.post(API_ENDPOINTSTES.AUTH.CHANGE_PASSWORD, payload);
        return response.data;
    },

    // Send OTP on email address
    async sendEmailOTP(payload) {
        const response = await axiosClient.post(API_ENDPOINTSTES.AUTH.SEND_EMAIL_OTP, payload);
        return response.data;
    },

    // Send OTP on email address
    async verifyEmailOTP(payload) {
        const response = await axiosClient.post(API_ENDPOINTSTES.AUTH.VERIFY_EMAIL_OTP, payload);
        return response.data;
    },

    // Send OTP on email address
    async sendForgotPasswordOTP(payload) {
        const response = await axiosClient.post(API_ENDPOINTSTES.AUTH.FORGOT_PASSWORD, payload);
        return response.data;
    },

    // Reset password with OTP
    async resetPassword(payload) {
        const response = await axiosClient.post(API_ENDPOINTSTES.AUTH.RESET_PASSWORD, payload);
        return response.data;
    },
}