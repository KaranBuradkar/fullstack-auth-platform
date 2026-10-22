import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";

import { useAuth } from "../../hooks/useAuth";
import { useUser } from "../../hooks/useUser";
import { APP_ROUTES } from "../../config/AppRouteEndpoints";
import { authService } from "../../services/authService";
import { tokenStorage } from "../../utils/storage";
import { tokenSessionStorage } from "../../utils/SessionStorage";

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
    setForm({
      ...form,
      [e.target.name]: e.target.value,
    });
  };

  // Handle register new account
  const handleSubmit = async (e) => {
    e.preventDefault();

    setError("");
    setLoading(true);

    try {
      // User Registration
      const response = await authService.register(form);

      if(!response.success) {
        throw new Error("");
      }

      const accessToken = response.data.accessToken;
      const refreshToken = response.data.refreshToken;
      const email = response.data.email;

      tokenStorage.setAccessToken(accessToken);
      tokenStorage.setRefreshToken(refreshToken);

      setAccessToken(accessToken);
      setRefreshToken(refreshToken);

      // User details
      const data = {
        id: response.data.id,
        fullName: response.data.fullName,
        email: response.data.email,
        emailVerified: response.data.emailVerified,
        role: response.data.role
      };

      // Save session for do not loss for verification
      tokenSessionStorage.saveUserEmail(data.email);
      // save in user context
      setUser(data);

      // Navigate to verify email
      navigate(APP_ROUTES.AUTH.VERIFY_EMAIL);

    } catch (error) {
      // Handle api error response only
      if(error.response?.data) {
        setError(error.response?.data?.message || "Register failed");
      } 
      // Other error 
      else throw new Error("Frontend logic breaks!!");
      
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
          <p className="mb-4 rounded-lg bg-red-100 px-4 py-3 text-sm text-red-600">
            {error}
          </p>
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