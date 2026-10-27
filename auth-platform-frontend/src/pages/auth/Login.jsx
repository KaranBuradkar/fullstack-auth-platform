import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";

import { useAuth } from "../../hooks/useAuth";
import { authService } from "../../services/authService";
import { useUser } from "../../hooks/useUser";
import { APP_ROUTES } from "../../config/AppRouteEndpoints";
import { tokenStorage } from "../../utils/storage";
import { tokenSessionStorage } from "../../utils/SessionStorage";
import { getApiErrorMessage } from '../../utils/apiError';

const Login = () => {

  const { accessToken, refreshToken, setAccessToken, setRefreshToken } = useAuth();
  const { user, setUser } = useUser();
  const navigate = useNavigate();

  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const [form, setForm] = useState({
    email: "",
    password: "",
    provider: "LOCAL"
  });

  const handleChange = (e) => {
    setForm({
      ...form,
      [e.target.name]: e.target.value,
    });
  };

  // Handle login user
  const handleSubmit = async (e) => {
    e.preventDefault();

    setError("");
    setLoading(true);

    try {
      const response = await authService.login(form);

      if (!response?.success || !response?.data) {
        setError(
          typeof response?.message === "string" &&
          response.message.trim()
            ? response.message
            : "Login could not be completed."
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

      // Login user details
      const loginUser = {
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
      setUser(loginUser);

      if (!loginUser.emailVerified) {
        // Continue email verification
        navigate(APP_ROUTES.AUTH.VERIFY_EMAIL, { replace: true });
        return;
      }      

      // Continue dashboard
      navigate(APP_ROUTES.USER.DASHBOARD, { replace: true });
  
    } catch (error) {
      // Unexpected errors should be logged for debugging,
      if (!error?.response && !error?.request) {
        console.error("Login application error:", error);
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
          Login
        </h1>

        {error && (
          <p className="mb-4 rounded-lg bg-red-100 px-4 py-3 text-sm text-red-600">
            {error}
          </p>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <input
            type="text"
            name="email"
            placeholder="Email"
            value={form.email}
            onChange={handleChange}
            className="w-full rounded-lg border border-gray-300 px-4 py-3 text-gray-800 outline-none 
            transition focus:border-blue-500 focus:ring-2 focus:ring-blue-200"
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

          <div className="mt-2 flex justify-end">
            <Link
              to={APP_ROUTES.AUTH.FORGOT_PASSWORD}
              className="text-sm font-medium text-blue-600
              hover:text-blue-700 hover:underline"
            >
              Forgot Password?
            </Link>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full rounded-lg bg-blue-600 px-4 py-3 font-semibold text-white 
            transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-60"
          >
            {loading ? "Logging in..." : "Login"}
          </button>
        </form>

        <p className="mt-6 text-center text-sm text-gray-600">
          Don't have an account?{" "}
          <Link
            to={APP_ROUTES.AUTH.REGISTER}
            className="font-semibold text-blue-600 hover:text-blue-700 hover:underline"
          >
            Register
          </Link>
        </p>

      </div>
    </div>
  );
};

export default Login;