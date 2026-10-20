import axios from "axios";
import { tokenStorage } from '../utils/storage'
import { useAuth } from "../hooks/useAuth";
import { API_ERROR_CODE } from "../utils/ApiErrorCode";

// Create API Request
const axiosClient = axios.create({
  baseURL: import.meta.env.VITE_BACKEND_API_BASE_URL,
  timeout: 15000, // 15 secounds
  headers: {
    "Content-Type": "application/json",
  },
});

// Access Token Handling
axiosClient.interceptors.request.use((config) => {
  const token = tokenStorage.getAccessToken();

  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

// Error Handling
axiosClient.interceptors.response.use(
  (response) => response,

  async (error) => {
    const originalRequest = error.config;

    console.log("API ERROR: ", error.response?.data?.message || "no defined");
    

    // Access Token expired then call 
    // update refresh token
    if (
      error.response?.status === 401 && 
      error.response?.data?.code == API_ERROR_CODE.INVALID_TOKEN &&
      !originalRequest._retry
    ) {
      originalRequest._retry = true;

      const { updateRefreshToken } = useAuth();
      updateRefreshToken();
    }

    return Promise.reject(error);
  }
);

// Expose axios client for api
export default axiosClient;