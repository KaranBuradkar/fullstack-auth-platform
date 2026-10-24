import { useState } from "react";
import { authService } from "../../services/authService";
import { useUser } from "../../hooks/useUser";
import { useNavigate } from "react-router-dom";
import { APP_ROUTES } from "../../config/AppRouteEndpoints";
import { tokenSessionStorage } from "../../utils/SessionStorage";

const VerifyEmail = () => {
    const { user } = useUser();

    const [otp, setOtp] = useState("");
    const [otpSent, setOtpSent] = useState(tokenSessionStorage.getOtpSent() || false);
    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState("");
    const [error, setError] = useState("");

    const navigate = useNavigate();

    // Send Email OTP
    const handleSendEmailOTP = async () => {
        setLoading(true);
        setMessage("");
        setError("");

        try {
            const payload = {
                email: user.email,
            };

            const response = await authService.sendEmailOTP(payload);

            if (response.success) {
                setOtpSent(true);
                tokenSessionStorage.saveOtpSent(true);
                setMessage(
                    response.message ||
                    "OTP sent successfully. Please check your email."
                );

            } else {
                setError(
                    response.message || "Failed to send OTP."
                );
            }
        } catch (error) {
            console.error("OTP Verification: ", error.response);
            setError(
                error.response?.message ||
                "Unable to send OTP. Please try again."
            );
            console.log(error.message);
            
        } finally {
            setLoading(false);
        }
    };

    // Verify Email OTP
    const handleVerifyEmail = async (e) => {
        e.preventDefault();

        if (!otp.trim()) {
            setError("Please enter the OTP.");
            return;
        }

        setLoading(true);
        setMessage("");
        setError("");

        try {
            // Replace verifyEmailOTP with the actual method
            // implemented in your authService.
            const payload = {
                email: user.email,
                otp: otp.trim(),
            };

            const response = await authService.verifyEmailOTP(payload);

            console.log("Email verification: ", response);
            

            if (response.success) {
                setMessage(
                    response.message ||
                    "Email verified successfully!"
                );

                // Navigate to Dashboard
                navigate(APP_ROUTES.USER.DASHBOARD);
                return;
            } else {
                setError(
                    response.message || "Invalid OTP."
                );
            }
        } catch (error) {
            console.error("OTP Verification: ", error);
            setError(
                error.response?.message ||
                "OTP verification failed. Please try again."
            );
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-gray-100 px-4">
            <div className="w-full max-w-md rounded-2xl bg-white p-8 shadow-lg">

                {/* Header */}
                <div className="mb-6 text-center">
                    <div className="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-blue-100 text-3xl">
                        ✉️
                    </div>

                    <h1 className="text-2xl font-bold text-gray-800">
                        Verify Your Email
                    </h1>

                    <p className="mt-2 text-sm text-gray-500">
                        Verify your email address to secure your account.
                    </p>
                </div>

                {/* Email */}
                <div className="mb-6 rounded-lg bg-gray-50 p-4">
                    <p className="text-sm text-gray-500">
                        Registered email address
                    </p>

                    <p className="mt-1 break-all font-medium text-gray-800">
                        {user?.email || "Email not available"}
                    </p>
                </div>

                {/* Feedback */}
                {message && (
                    <div
                        role="status"
                        className="mb-4 rounded-lg bg-green-50 p-3 text-sm text-green-700"
                    >
                        {message}
                    </div>
                )}

                {error && (
                    <div
                        role="alert"
                        className="mb-4 rounded-lg bg-red-50 p-3 text-sm text-red-700"
                    >
                        {error}
                    </div>
                )}

                {/* Step 1: Send OTP */}
                {!otpSent && (
                    <button
                        type="button"
                        onClick={handleSendEmailOTP}
                        disabled={loading || !user?.email}
                        className="w-full rounded-lg bg-blue-600 px-4 py-3 font-semibold text-white transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-60"
                    >
                        {loading ? "Sending OTP..." : "Send Verification OTP"}
                    </button>
                )}

                {/* Step 2: Enter OTP */}
                {otpSent && (
                    <form onSubmit={handleVerifyEmail}>
                        <label
                            htmlFor="otp"
                            className="mb-2 block text-sm font-medium text-gray-700"
                        >
                            Enter Verification OTP
                        </label>

                        <input
                            id="otp"
                            type="text"
                            inputMode="numeric"
                            autoComplete="one-time-code"
                            maxLength={6}
                            value={otp}
                            onChange={(e) =>
                                setOtp(
                                    e.target.value
                                        .replace(/\D/g, "")
                                        .slice(0, 6)
                                )
                            }
                            placeholder="Enter 6-digit OTP"
                            className="mb-4 w-full rounded-lg border border-gray-300 px-4 py-3 text-center text-xl tracking-[0.5em] outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-200"
                            required
                        />

                        <button
                            type="submit"
                            disabled={loading || otp.length !== 6}
                            className="w-full rounded-lg bg-blue-600 px-4 py-3 font-semibold text-white transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-60"
                        >
                            {loading ? "Verifying..." : "Verify Email"}
                        </button>

                        <button
                            type="button"
                            onClick={handleSendEmailOTP}
                            disabled={loading}
                            className="mt-4 w-full text-sm font-medium text-blue-600 hover:text-blue-800 disabled:opacity-60"
                        >
                            Resend OTP
                        </button>
                    </form>
                )}

                <p className="mt-6 text-center text-xs text-gray-400">
                    Please do not share your verification code with anyone.
                </p>
            </div>
        </div>
    );
};

export default VerifyEmail;