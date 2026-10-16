import { useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import { useState } from "react";
import { AUTH_ROUTES } from "../../config/authRoutes";

const ChangePassword = () => {
    const { changePassword } = useAuth();
    const navigate = useNavigate();

    const [form, setForm] = useState({
        currentPassword: "",
        newPassword: "",
    });

    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");
    const [showCurrentPassword, setShowCurrentPassword] = useState(false);
    const [showNewPassword, setShowNewPassword] = useState(false);

    const handleChange = (e) => {
        setForm({
            ...form,
            [e.target.name]: e.target.value,
        });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        setLoading(true);
        setError("");

        try {
            const response = await changePassword(form);

            if (response?.success) {
                navigate(AUTH_ROUTES.DASHBOARD);
            } else {
                setError(response?.message || "Unable to change password.");
            }
        } catch (error) {
            setError(
                error?.response?.data?.message ||
                error?.message ||
                "Something went wrong. Please try again."
            );

            console.error("Change password error:", error);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-100 px-4 py-10">

            <div className="w-full max-w-md">

                {/* Card */}
                <div className="overflow-hidden rounded-2xl border border-white/70 bg-white shadow-xl shadow-blue-900/5">

                    {/* Header */}
                    <div className="px-6 pb-6 pt-8 text-center sm:px-8">

                        <div className="mx-auto mb-5 flex h-16 w-16 items-center justify-center rounded-2xl bg-blue-100 text-blue-600">
                            <svg
                                xmlns="http://www.w3.org/2000/svg"
                                fill="none"
                                viewBox="0 0 24 24"
                                strokeWidth={1.7}
                                stroke="currentColor"
                                className="h-8 w-8"
                            >
                                <path
                                    strokeLinecap="round"
                                    strokeLinejoin="round"
                                    d="M16.5 10.5V6.75a4.5 4.5 0 0 0-9 0v3.75m-.75 0h10.5c.828 0 1.5.672 1.5 1.5v7.5c0 .828-.672 1.5-1.5 1.5H6.75a1.5 1.5 0 0 1-1.5-1.5V12c0-.828.672-1.5 1.5-1.5Z"
                                />
                            </svg>
                        </div>

                        <h1 className="text-2xl font-bold tracking-tight text-slate-900">
                            Change Password
                        </h1>

                        <p className="mt-2 text-sm leading-6 text-slate-500">
                            Keep your account secure by updating your password.
                        </p>
                    </div>

                    {/* Form */}
                    <div className="px-6 pb-8 sm:px-8">

                        {error && (
                            <div
                                role="alert"
                                className="mb-5 flex items-start gap-3 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700"
                            >
                                <svg
                                    xmlns="http://www.w3.org/2000/svg"
                                    fill="none"
                                    viewBox="0 0 24 24"
                                    strokeWidth={1.8}
                                    stroke="currentColor"
                                    className="mt-0.5 h-5 w-5 shrink-0"
                                >
                                    <path
                                        strokeLinecap="round"
                                        strokeLinejoin="round"
                                        d="M12 9v3.75m9-.75a9 9 0 1 1-18 0 9 9 0 0 1 18 0Zm-9 3.75h.008v.008H12v-.008Z"
                                    />
                                </svg>

                                <p>{error}</p>
                            </div>
                        )}

                        <form onSubmit={handleSubmit} className="space-y-5">

                            {/* Current Password */}
                            <div>
                                <label
                                    htmlFor="currentPassword"
                                    className="mb-2 block text-sm font-semibold text-slate-700"
                                >
                                    Current Password
                                </label>

                                <div className="relative">
                                    <input
                                        id="currentPassword"
                                        type={showCurrentPassword ? "text" : "password"}
                                        name="currentPassword"
                                        placeholder="Enter current password"
                                        value={form.currentPassword}
                                        onChange={handleChange}
                                        autoComplete="current-password"
                                        required
                                        className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 pr-12 text-sm text-slate-800 outline-none transition placeholder:text-slate-400 hover:border-slate-300 focus:border-blue-500 focus:bg-white focus:ring-4 focus:ring-blue-500/10"
                                    />

                                    <button
                                        type="button"
                                        onClick={() =>
                                            setShowCurrentPassword(!showCurrentPassword)
                                        }
                                        aria-label={
                                            showCurrentPassword
                                                ? "Hide current password"
                                                : "Show current password"
                                        }
                                        className="absolute inset-y-0 right-0 flex items-center px-4 text-slate-400 transition hover:text-blue-600"
                                    >
                                        {showCurrentPassword ? (
                                            <svg
                                                xmlns="http://www.w3.org/2000/svg"
                                                fill="none"
                                                viewBox="0 0 24 24"
                                                strokeWidth={1.7}
                                                stroke="currentColor"
                                                className="h-5 w-5"
                                            >
                                                <path
                                                    strokeLinecap="round"
                                                    strokeLinejoin="round"
                                                    d="M3 3l18 18M10.58 10.58a2 2 0 0 0 2.83 2.83M9.88 5.09A10.9 10.9 0 0 1 12 4.88c5.05 0 8.27 4.12 9 7.12a9.7 9.7 0 0 1-2.12 3.92M6.61 6.61C4.35 8.04 3.37 10.05 3 12c.73 3 3.95 7.12 9 7.12 1.05 0 2.03-.22 2.91-.6"
                                                />
                                            </svg>
                                        ) : (
                                            <svg
                                                xmlns="http://www.w3.org/2000/svg"
                                                fill="none"
                                                viewBox="0 0 24 24"
                                                strokeWidth={1.7}
                                                stroke="currentColor"
                                                className="h-5 w-5"
                                            >
                                                <path
                                                    strokeLinecap="round"
                                                    strokeLinejoin="round"
                                                    d="M2.25 12s3.5-7.125 9.75-7.125S21.75 12 21.75 12 18.25 19.125 12 19.125 2.25 12 2.25 12Z"
                                                />
                                                <circle cx="12" cy="12" r="3" />
                                            </svg>
                                        )}
                                    </button>
                                </div>
                            </div>

                            {/* New Password */}
                            <div>
                                <label
                                    htmlFor="newPassword"
                                    className="mb-2 block text-sm font-semibold text-slate-700"
                                >
                                    New Password
                                </label>

                                <div className="relative">
                                    <input
                                        id="newPassword"
                                        type={showNewPassword ? "text" : "password"}
                                        name="newPassword"
                                        placeholder="Enter new password"
                                        value={form.newPassword}
                                        onChange={handleChange}
                                        autoComplete="new-password"
                                        required
                                        minLength={8}
                                        className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 pr-12 text-sm text-slate-800 outline-none transition placeholder:text-slate-400 hover:border-slate-300 focus:border-blue-500 focus:bg-white focus:ring-4 focus:ring-blue-500/10"
                                    />

                                    <button
                                        type="button"
                                        onClick={() =>
                                            setShowNewPassword(!showNewPassword)
                                        }
                                        aria-label={
                                            showNewPassword
                                                ? "Hide new password"
                                                : "Show new password"
                                        }
                                        className="absolute inset-y-0 right-0 flex items-center px-4 text-slate-400 transition hover:text-blue-600"
                                    >
                                        {showNewPassword ? (
                                            <svg
                                                xmlns="http://www.w3.org/2000/svg"
                                                fill="none"
                                                viewBox="0 0 24 24"
                                                strokeWidth={1.7}
                                                stroke="currentColor"
                                                className="h-5 w-5"
                                            >
                                                <path
                                                    strokeLinecap="round"
                                                    strokeLinejoin="round"
                                                    d="M3 3l18 18M10.58 10.58a2 2 0 0 0 2.83 2.83M9.88 5.09A10.9 10.9 0 0 1 12 4.88c5.05 0 8.27 4.12 9 7.12a9.7 9.7 0 0 1-2.12 3.92M6.61 6.61C4.35 8.04 3.37 10.05 3 12c.73 3 3.95 7.12 9 7.12 1.05 0 2.03-.22 2.91-.6"
                                                />
                                            </svg>
                                        ) : (
                                            <svg
                                                xmlns="http://www.w3.org/2000/svg"
                                                fill="none"
                                                viewBox="0 0 24 24"
                                                strokeWidth={1.7}
                                                stroke="currentColor"
                                                className="h-5 w-5"
                                            >
                                                <path
                                                    strokeLinecap="round"
                                                    strokeLinejoin="round"
                                                    d="M2.25 12s3.5-7.125 9.75-7.125S21.75 12 21.75 12 18.25 19.125 12 19.125 2.25 12 2.25 12Z"
                                                />
                                                <circle cx="12" cy="12" r="3" />
                                            </svg>
                                        )}
                                    </button>
                                </div>

                                <p className="mt-2 text-xs text-slate-500">
                                    Use at least 8 characters for your new password.
                                </p>
                            </div>

                            {/* Submit */}
                            <button
                                type="submit"
                                disabled={loading}
                                className="flex w-full items-center justify-center gap-2 rounded-xl bg-blue-600 px-4 py-3.5 text-sm font-semibold text-white shadow-lg shadow-blue-600/20 transition duration-200 hover:-translate-y-0.5 hover:bg-blue-700 hover:shadow-blue-600/30 focus:outline-none focus:ring-4 focus:ring-blue-500/20 disabled:translate-y-0 disabled:cursor-not-allowed disabled:opacity-60 disabled:shadow-none"
                            >
                                {loading ? (
                                    <>
                                        <svg
                                            className="h-5 w-5 animate-spin"
                                            xmlns="http://www.w3.org/2000/svg"
                                            fill="none"
                                            viewBox="0 0 24 24"
                                        >
                                            <circle
                                                className="opacity-25"
                                                cx="12"
                                                cy="12"
                                                r="10"
                                                stroke="currentColor"
                                                strokeWidth="4"
                                            />
                                            <path
                                                className="opacity-75"
                                                fill="currentColor"
                                                d="M4 12a8 8 0 0 1 8-8V0C5.373 0 0 5.373 0 12h4Z"
                                            />
                                        </svg>

                                        Updating Password...
                                    </>
                                ) : (
                                    <>
                                        Update Password

                                        <svg
                                            xmlns="http://www.w3.org/2000/svg"
                                            fill="none"
                                            viewBox="0 0 24 24"
                                            strokeWidth={1.8}
                                            stroke="currentColor"
                                            className="h-5 w-5"
                                        >
                                            <path
                                                strokeLinecap="round"
                                                strokeLinejoin="round"
                                                d="M5 12h14m-7-7 7 7-7 7"
                                            />
                                        </svg>
                                    </>
                                )}
                            </button>
                        </form>

                        {/* Security Note */}
                        <div className="mt-6 flex items-start gap-3 rounded-xl bg-slate-50 p-4">
                            <svg
                                xmlns="http://www.w3.org/2000/svg"
                                fill="none"
                                viewBox="0 0 24 24"
                                strokeWidth={1.7}
                                stroke="currentColor"
                                className="mt-0.5 h-5 w-5 shrink-0 text-emerald-600"
                            >
                                <path
                                    strokeLinecap="round"
                                    strokeLinejoin="round"
                                    d="M9 12.75 11.25 15 15 9.75m6.023-5.036A11.95 11.95 0 0 1 12 2.25a11.95 11.95 0 0 1-9.023 2.464A12 12 0 0 0 3 7.5c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.031 9-11.622 0-.962-.113-1.897-.327-2.786Z"
                                />
                            </svg>

                            <p className="text-xs leading-5 text-slate-500">
                                <span className="font-semibold text-slate-700">
                                    Security tip:
                                </span>{" "}
                                Never share your password with anyone. Choose a
                                unique password that you don't use elsewhere.
                            </p>
                        </div>
                    </div>
                </div>

                <p className="mt-5 text-center text-xs text-slate-400">
                    Protected by your account security settings
                </p>
            </div>
        </div>
    );
};

export default ChangePassword;