
import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";

import { authService } from "../../services/authService";
import { tokenSessionStorage } from "../../utils/SessionStorage";
import { APP_ROUTES } from "../../config/AppRouteEndpoints";
import { getApiErrorMessage } from "../../utils/apiError";

const ForgotPassword = () => {
  const navigate = useNavigate();

  const [email, setEmail] = useState(() =>
    tokenSessionStorage.getForgotPasswordEmail()
  );

  const [otpSent, setOtpSent] = useState(() =>
    tokenSessionStorage.getForgotPasswordOtpSent()
  );

  const [otp, setOtp] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [showNewPassword, setShowNewPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  // Update email and reset the previous OTP flow.
  const handleEmailChange = (e) => {
    const value = e.target.value;

    setEmail(value);
    setOtp("");
    setNewPassword("");
    setConfirmPassword("");
    setOtpSent(false);
    setError("");
    setSuccess("");

    tokenSessionStorage.saveForgotPasswordEmail(value);
    tokenSessionStorage.saveForgotPasswordOtpSent(false);
  };

  // Send or resend password-reset OTP.
  const handleSendOtp = async (e) => {
    e.preventDefault();

    setError("");
    setSuccess("");

    const normalizedEmail = email.trim().toLowerCase();

    if (!normalizedEmail) {
      setError("Please enter your registered email.");
      return;
    }

    setLoading(true);

    try {
      const response = await authService.sendForgotPasswordOTP({
        email: normalizedEmail,
      });

      if (!response?.success) {
        setError(
          response?.message || "Unable to send OTP. Please try again."
        );
        return;
      }

      setEmail(normalizedEmail);
      setOtp("");
      setNewPassword("");
      setConfirmPassword("");
      setOtpSent(true);

      tokenSessionStorage.saveForgotPasswordEmail(normalizedEmail);
      tokenSessionStorage.saveForgotPasswordOtpSent(true);

      setSuccess("OTP sent successfully. Check your email.");
    } catch (err) {
      setError(getApiErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  // Verify OTP and reset password through the backend.
  const handleResetPassword = async (e) => {
    e.preventDefault();

    setError("");
    setSuccess("");

    if (!otpSent) {
      setError("Please request an OTP first.");
      return;
    }

    if (!/^\d{6}$/.test(otp)) {
      setError("Please enter a valid 6-digit OTP.");
      return;
    }

    if (!newPassword) {
      setError("Please enter your new password.");
      return;
    }

    if (newPassword.length < 8) {
      setError("Password must contain at least 8 characters.");
      return;
    }

    if (newPassword !== confirmPassword) {
      setError("New password and confirm password do not match.");
      return;
    }

    setLoading(true);

    try {
      const payload = {
        email: email.trim().toLowerCase(),
        otp,
        newPassword,
      };

      const response = await authService.resetPassword(payload);

      if (!response?.success) {
        setError(
          response?.message || "Password reset failed. Please try again."
        );
        return;
      }

      tokenSessionStorage.clearForgotPassword();

      setOtpSent(false);
      setOtp("");
      setNewPassword("");
      setConfirmPassword("");

      navigate(APP_ROUTES.AUTH.LOGIN, {
        replace: true,
        state: {
          message: "Password reset successfully. Please log in.",
        },
      });
    } catch (err) {
      setError(getApiErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100 px-4 py-8">
      <div className="w-full max-w-md rounded-2xl bg-white p-8 shadow-lg">
        <h1 className="mb-2 text-center text-3xl font-bold text-gray-800">
          Reset Password
        </h1>

        <p className="mb-6 text-center text-sm text-gray-600">
          Receive an OTP and use it to securely reset your password.
        </p>

        {error && (
          <p
            role="alert"
            className="mb-4 rounded-lg bg-red-100 px-4 py-3 text-sm text-red-700"
          >
            {error}
          </p>
        )}

        {success && (
          <p
            role="status"
            className="mb-4 rounded-lg bg-green-100 px-4 py-3 text-sm text-green-700"
          >
            {success}
          </p>
        )}

        <form onSubmit={handleSendOtp} className="space-y-4">
          <div>
            <label
              htmlFor="email"
              className="mb-1 block text-sm font-medium text-gray-700"
            >
              Email address
            </label>

            <input
              id="email"
              type="email"
              value={email}
              onChange={handleEmailChange}
              placeholder="Enter your registered email"
              autoComplete="email"
              required
              disabled={loading}
              className="w-full rounded-lg border border-gray-300 px-4 py-3
              text-gray-800 outline-none transition focus:border-blue-500
              focus:ring-2 focus:ring-blue-200 disabled:bg-gray-100"
            />
          </div>

          <button
            type="submit"
            disabled={loading || !email.trim()}
            className="w-full rounded-lg bg-blue-600 px-4 py-3 font-semibold
            text-white transition hover:bg-blue-700
            disabled:cursor-not-allowed disabled:opacity-60"
          >
            {loading
              ? "Please wait..."
              : otpSent
                ? "Resend OTP"
                : "Send OTP"}
          </button>
        </form>

        {otpSent && (
          <form
            onSubmit={handleResetPassword}
            className="mt-6 space-y-4 border-t border-gray-200 pt-6"
          >
            <p className="text-sm font-medium text-gray-700">
              Enter the OTP sent to {email}
            </p>

            <div>
              <label
                htmlFor="otp"
                className="mb-1 block text-sm font-medium text-gray-700"
              >
                Email OTP
              </label>

              <input
                id="otp"
                type="text"
                inputMode="numeric"
                autoComplete="one-time-code"
                maxLength={6}
                value={otp}
                onChange={(e) =>
                  setOtp(e.target.value.replace(/\D/g, "").slice(0, 6))
                }
                placeholder="Enter 6-digit OTP"
                required
                disabled={loading}
                className="w-full rounded-lg border border-gray-300 px-4 py-3
                text-gray-800 outline-none transition focus:border-blue-500
                focus:ring-2 focus:ring-blue-200"
              />
            </div>

            <div>
              <label
                htmlFor="newPassword"
                className="mb-1 block text-sm font-medium text-gray-700"
              >
                New password
              </label>

              <div className="relative">
                <input
                  id="newPassword"
                  type={showNewPassword ? "text" : "password"}
                  autoComplete="new-password"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  placeholder="Enter new password"
                  minLength={8}
                  required
                  disabled={loading}
                  className="w-full rounded-lg border border-gray-300 px-4 py-3 pr-12
                  text-gray-800 outline-none transition focus:border-blue-500
                  focus:ring-2 focus:ring-blue-200"
                />

                <button
                  type="button"
                  onClick={() => setShowNewPassword((prev) => !prev)}
                  aria-label={showNewPassword ? "Hide new password" : "Show new password"}
                  aria-pressed={showNewPassword}
                  disabled={loading}
                  className="absolute inset-y-0 right-0 flex items-center px-4
                  text-gray-500 hover:text-gray-700"
                >
                  {showNewPassword ? (
                    <svg
                      xmlns="http://www.w3.org/2000/svg"
                      width="20"
                      height="20"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      strokeWidth="2"
                      strokeLinecap="round"
                      strokeLinejoin="round"
                    >
                      <path d="M3 3l18 18" />
                      <path d="M10.58 10.58a2 2 0 002.83 2.83" />
                      <path d="M9.88 5.09A10.94 10.94 0 0112 4.9c5 0 9 4.1 10 7.1a11.8 11.8 0 01-4.1 5.2" />
                      <path d="M6.6 6.6C4.2 8 2.5 10.1 2 12c.5 1.8 2.2 4 4.6 5.4A10.8 10.8 0 0012 19.1" />
                    </svg>
                  ) : (
                    <svg
                      xmlns="http://www.w3.org/2000/svg"
                      width="20"
                      height="20"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      strokeWidth="2"
                      strokeLinecap="round"
                      strokeLinejoin="round"
                    >
                      <path d="M2 12s3.6-7 10-7 10 7 10 7-3.6 7-10 7-10-7-10-7z" />
                      <circle cx="12" cy="12" r="3" />
                    </svg>
                  )}
                </button>
              </div>
            </div>

            <div>
              <label
                htmlFor="confirmPassword"
                className="mb-1 block text-sm font-medium text-gray-700"
              >
                Confirm new password
              </label>

              <div className="relative">
                <input
                  id="confirmPassword"
                  type={showConfirmPassword ? "text" : "password"}
                  autoComplete="new-password"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  placeholder="Confirm new password"
                  minLength={8}
                  required
                  disabled={loading}
                  className="w-full rounded-lg border border-gray-300 px-4 py-3 pr-12
                  text-gray-800 outline-none transition focus:border-blue-500
                  focus:ring-2 focus:ring-blue-200"
                />

                <button
                  type="button"
                  onClick={() => setShowConfirmPassword((prev) => !prev)}
                  aria-label={
                    showConfirmPassword
                      ? "Hide confirm password"
                      : "Show confirm password"
                  }
                  aria-pressed={showConfirmPassword}
                  disabled={loading}
                  className="absolute inset-y-0 right-0 flex items-center px-4
                  text-gray-500 hover:text-gray-700"
                >
                  {showConfirmPassword ? (
                    <svg
                      xmlns="http://www.w3.org/2000/svg"
                      width="20"
                      height="20"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      strokeWidth="2"
                      strokeLinecap="round"
                      strokeLinejoin="round"
                    >
                      <path d="M3 3l18 18" />
                      <path d="M10.58 10.58a2 2 0 002.83 2.83" />
                      <path d="M9.88 5.09A10.94 10.94 0 0112 4.9c5 0 9 4.1 10 7.1a11.8 11.8 0 01-4.1 5.2" />
                      <path d="M6.6 6.6C4.2 8 2.5 10.1 2 12c.5 1.8 2.2 4 4.6 5.4A10.8 10.8 0 0012 19.1" />
                    </svg>
                  ) : (
                    <svg
                      xmlns="http://www.w3.org/2000/svg"
                      width="20"
                      height="20"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      strokeWidth="2"
                      strokeLinecap="round"
                      strokeLinejoin="round"
                    >
                      <path d="M2 12s3.6-7 10-7 10 7 10 7-3.6 7-10 7-10-7-10-7z" />
                      <circle cx="12" cy="12" r="3" />
                    </svg>
                  )}
                </button>
              </div>
            </div>

            <button
              type="submit"
              disabled={loading || otp.length !== 6}
              className="w-full rounded-lg bg-green-600 px-4 py-3
              font-semibold text-white transition hover:bg-green-700
              disabled:cursor-not-allowed disabled:opacity-60"
            >
              {loading ? "Resetting password..." : "Reset Password"}
            </button>
          </form>
        )}

        <p className="mt-6 text-center text-sm text-gray-600">
          Remember your password?{" "}
          <Link
            to={APP_ROUTES.AUTH.LOGIN}
            className="font-semibold text-blue-600 hover:text-blue-700 hover:underline"
          >
            Back to Login
          </Link>
        </p>
      </div>
    </div>
  );
};

export default ForgotPassword;
