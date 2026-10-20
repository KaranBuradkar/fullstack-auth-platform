import axiosClient from "../api/axiosClient";
import { API_ENDPOINTSTES } from '../api/endpoints'

// Handles API requests related to user operations
export const userService = {
    
    // Get user details
    async userDetails() {
        const response = await axiosClient.get(API_ENDPOINTSTES.USER.PROFILE);
        return response.data;
    }
}