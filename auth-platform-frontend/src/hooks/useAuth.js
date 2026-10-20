import { useContext } from "react";
import { AuthContext } from "../context/AuthContext";

// Provides convenient access to the context values and functions
export function useAuth() {
  const context = useContext(AuthContext);

  if (context === null) {
    throw new Error(
      "useAuth must be used within a AuthProvider"
    );
  }

  return context;
}