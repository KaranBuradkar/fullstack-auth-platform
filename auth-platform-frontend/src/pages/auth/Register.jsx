import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";

import { useAuth } from "../../hooks/useAuth";
import { useUser } from "../../hooks/useUser";
import { APP_ROUTES } from "../../config/AppRouteEndpoints";
import { authService } from "../../services/authService";
import { tokenStorage } from "../../utils/storage";
import { tokenSessionStorage } from "../../utils/SessionStorage";
import { getApiErrorMessage } from '../../utils/apiError';

const Register = () => {
  const { accessToken, setAccessToken, refreshToken, setRefreshToken } = useAuth();
  const { user, setUser } = useUser();
  const navigate = useNavigate();

  const [form, setForm] = useState({
    fullName: "",
    email: "",
    password: "",
    provider: "LOCAL"
  });

  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  
  const handleChange = (e) => {
    const { name, value } = e.target;

    setForm((previous) => ({
      ...previous,
      [name]: value,
    }));

    setError("");
  };

  // Handle register new account
  const handleSubmit = async (e) => {
    e.preventDefault();

    setError("");
    setLoading(true);

    try {
      // User Registration
      const response = await authService.register(form);
    
      if (!response?.success || !response?.data) {
        setError(
          typeof response?.message === "string" &&
          response.message.trim()
            ? response.message
            : "Registration could not be completed."
        );
        return;
      }

      const data = response.data;

      // Validate the minimum expected response shape
      if (
        typeof data.accessToken !== "string" ||
        typeof data.refreshToken !== "string" ||
        typeof data.email !== "string"
      ) {
        throw new Error("Invalid registration response");
      }

      // Registered user details
      const registeredUser = {
        id: data.id,
        fullName: data.fullName,
        email: data.email,
        emailVerified: data.emailVerified,
        role: data.role,
      };

      // Save authentication tokens
      tokenStorage.setAccessToken(data.accessToken);
      tokenStorage.setRefreshToken(data.refreshToken);

      setAccessToken(data.accessToken);
      setRefreshToken(data.refreshToken);

      // Save session for do not loss for verification
      tokenSessionStorage.saveUserEmail(data.email);

      // Update shared user state
      setUser(registeredUser);

      // Continue email verification
      navigate(APP_ROUTES.AUTH.VERIFY_EMAIL, { replace: true });
    } catch (error) {
      // Unexpected errors should be logged for debugging,
      if (!error?.response && !error?.request) {
        console.error("Registration application error:", error);
      }

      setError(getApiErrorMessage(error));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100 px-4">
      <div className="w-full max-w-md rounded-2xl bg-white p-8 shadow-lg">
        <h1 className="mb-6 text-center text-3xl font-bold text-gray-800">
          Create Account
        </h1>

        {error && (
          <div
            role="alert"
            aria-live="polite"
            className="mb-4 rounded-lg bg-red-100 px-4 py-3 text-sm text-red-700"
          >
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <input
            type="text"
            name="fullName"
            placeholder="Full Name"
            value={form.fullName}
            onChange={handleChange}
            className="w-full rounded-lg border border-gray-300 px-4 py-3 text-gray-800 
            outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-200"
          />

          <input
            type="email"
            name="email"
            placeholder="Email"
            value={form.email}
            onChange={handleChange}
            className="w-full rounded-lg border border-gray-300 px-4 py-3 text-gray-800 
            outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-200"
          />

          <input
            type="password"
            name="password"
            placeholder="Password"
            value={form.password}
            onChange={handleChange}
            className="w-full rounded-lg border border-gray-300 px-4 py-3 text-gray-800 
            outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-200"
          />

          <button
            type="submit"
            disabled={loading}
            className="w-full rounded-lg bg-blue-600 px-4 py-3 font-semibold text-white 
            transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-60"
          >
            {loading ? "Creating..." : "Register"}
          </button>
        </form>

        <p className="mt-6 text-center text-sm text-gray-600">
          Already have an account?{" "}
          <Link
            to={APP_ROUTES.AUTH.LOGIN}
            className="font-semibold text-blue-600 hover:text-blue-700 hover:underline"
          >
            Login
          </Link>
        </p>
      </div>
    </div>
  );
};

export default Register;