import { createContext, useContext, useState } from "react";

import { authService } from "../services/authService";
import { tokenStorage } from '../utils/storage';

// Define shared states and functions for updating it
export const AuthContext = createContext(null);

// Makes the context values available to descendant components
export const AuthProvider = ({ children }) => {

  const [accessToken, setAccessToken] = useState(tokenStorage.getAccessToken() || null);

  const [refreshToken, setRefreshToken] = useState(tokenStorage.getRefreshToken() || null);

  const isAuthenticated = Boolean(accessToken);

  const value = {
    accessToken, setAccessToken,
    refreshToken, setRefreshToken,
    isAuthenticated
  }

  return (
    <AuthContext.Provider
      value={value}
    >
      {children}
    </AuthContext.Provider>
  );
};