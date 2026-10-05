import { createContext, useContext, useState } from "react";
import { loginUser, registerUser } from "../api/authApi";
import { TOKEN_KEYS } from "../config/authRoutes";

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [accessToken, setAccessToken] = useState(
    localStorage.getItem(TOKEN_KEYS.ACCESS_TOKEN)
  );

  const [refreshToken, setRefreshToken] = useState(
    localStorage.getItem(TOKEN_KEYS.REFRESH_TOKEN)
  );

  const login = async (credentials) => {
    const response = await loginUser(credentials);

    const accessToken = response.data.accessToken;
    const refreshToken = response.data.refreshToken;

    localStorage.setItem(
      TOKEN_KEYS.ACCESS_TOKEN,
      accessToken
    );

    localStorage.setItem(
      TOKEN_KEYS.REFRESH_TOKEN,
      refreshToken
    );

    setAccessToken(accessToken);
    setRefreshToken(refreshToken);

    return response;
  };

  const register = async (userData) => {
    return await registerUser(userData);
  };

  const logout = () => {
    localStorage.removeItem(TOKEN_KEYS.ACCESS_TOKEN);
    localStorage.removeItem(TOKEN_KEYS.REFRESH_TOKEN);

    setAccessToken(null);
    setRefreshToken(null);
  };

  const isAuthenticated = Boolean(accessToken);

  return (
    <AuthContext.Provider
      value={{
        accessToken,
        refreshToken,
        isAuthenticated,
        login,
        register,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  return useContext(AuthContext);
};