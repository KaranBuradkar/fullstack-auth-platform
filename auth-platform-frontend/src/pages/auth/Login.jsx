import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";

import { useAuth } from "../../hooks/useAuth";
import { authService } from "../../services/authService";
import { useUser } from "../../hooks/useUser";
import { APP_ROUTES } from "../../config/AppRouteEndpoints";
import { tokenStorage } from "../../utils/storage";
import { tokenSessionStorage } from "../../utils/SessionStorage";

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

      if(!response.success) {
        throw new Error("Failed to login user");
      }
      
      const accessToken = response.data.accessToken;
      const refreshToken = response.data.refreshToken;

      tokenStorage.setAccessToken(accessToken);
      tokenStorage.setRefreshToken(refreshToken);

      setAccessToken(accessToken);
      setRefreshToken(refreshToken);

      // If account already verified then open Dashboard
      if(response.data.emailVerified) {
        navigate(APP_ROUTES.USER.DASHBOARD);
        return;
      }
      
      // User details
      const data = {
        id: response.data.id,
        fullName: response.data.fullName,
        email: response.data.email,
        emailVerified: response.data.emailVerified,
        role: response.data.role
      }
      // Save session for do not loss for verification
      tokenSessionStorage.saveUserEmail(data.email);
      // save in user context
      setUser(data);
      // Navigate to verify email
      navigate(APP_ROUTES.AUTH.VERIFY_EMAIL);

    } catch (error) {

      const data = error.response?.data || error;
      setError(data?.message || "Login failed");
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