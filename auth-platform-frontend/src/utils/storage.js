const TOKEN_KEYS = {
  ACCESS_TOKEN: "accessToken",
  REFRESH_TOKEN: "refreshToken",
};

export const tokenStorage = {
  getAccessToken() {
    return localStorage.getItem(TOKEN_KEYS.ACCESS_TOKEN);
  },

  setAccessToken(token) {
    localStorage.setItem(TOKEN_KEYS.ACCESS_TOKEN, token);
  },

  getRefreshToken() {
    return localStorage.getItem(TOKEN_KEYS.REFRESH_TOKEN);
  },

  setRefreshToken(token) {
    localStorage.setItem(TOKEN_KEYS.REFRESH_TOKEN, token);
  },

  getRefreshToken() {
    return localStorage.getItem(TOKEN_KEYS.REFRESH_TOKEN);
  },

  setRefreshToken(token) {
    localStorage.setItem(TOKEN_KEYS.REFRESH_TOKEN, token);
  },

  clear() {
    localStorage.removeItem(TOKEN_KEYS.ACCESS_TOKEN);
    localStorage.removeItem(TOKEN_KEYS.REFRESH_TOKEN);
  },
};