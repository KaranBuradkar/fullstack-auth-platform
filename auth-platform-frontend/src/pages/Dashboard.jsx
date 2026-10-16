import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { AUTH_ROUTES } from "../config/authRoutes";

const Dashboard = () => {
    const { logout, refreshUserToken } = useAuth();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate(AUTH_ROUTES.LOGIN);
    };

    const handleRefreshToken = async () => {
        try {
            await refreshUserToken();
        } catch (error) {
            console.error("Refresh token failed:", error);
        }
    };

    const handleChangePassword = () => {
        navigate(AUTH_ROUTES.CHANGE_PASSWORD);
    };

    return (
        <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-100 px-4 py-10">

            <div className="w-full max-w-md">

                {/* Dashboard Card */}
                <div className="overflow-hidden rounded-2xl border border-white/70 bg-white shadow-xl shadow-blue-900/5">

                    {/* Header */}
                    <div className="px-6 pb-7 pt-9 text-center sm:px-8">

                        {/* Profile Icon */}
                        <div className="mx-auto mb-5 flex h-20 w-20 items-center justify-center rounded-3xl bg-blue-100 text-blue-600 ring-8 ring-blue-50">

                            <svg
                                xmlns="http://www.w3.org/2000/svg"
                                fill="none"
                                viewBox="0 0 24 24"
                                strokeWidth={1.5}
                                stroke="currentColor"
                                className="h-10 w-10"
                            >
                                <path
                                    strokeLinecap="round"
                                    strokeLinejoin="round"
                                    d="M15.75 6a3.75 3.75 0 1 1-7.5 0 3.75 3.75 0 0 1 7.5 0ZM4.5 20.1a7.5 7.5 0 0 1 15 0A17.9 17.9 0 0 1 12 21.75c-2.7 0-5.25-.6-7.5-1.65Z"
                                />
                            </svg>

                        </div>

                        <h1 className="text-3xl font-bold tracking-tight text-slate-900">
                            Dashboard
                        </h1>

                        <p className="mt-2 text-sm leading-6 text-slate-500">
                            Welcome back! Manage your account and security settings.
                        </p>

                        {/* Authentication Status */}
                        <div className="mt-5 inline-flex items-center gap-2 rounded-full border border-emerald-200 bg-emerald-50 px-4 py-2">

                            <span className="relative flex h-2.5 w-2.5">
                                <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-emerald-400 opacity-50"></span>
                                <span className="relative inline-flex h-2.5 w-2.5 rounded-full bg-emerald-500"></span>
                            </span>

                            <span className="text-xs font-semibold text-emerald-700">
                                Authenticated Session
                            </span>

                        </div>
                    </div>

                    {/* Actions */}
                    <div className="border-t border-slate-100 px-6 py-7 sm:px-8">

                        <h2 className="mb-4 text-sm font-semibold uppercase tracking-wider text-slate-400">
                            Account Actions
                        </h2>

                        <div className="space-y-3">

                            {/* Refresh Token */}
                            <button
                                type="button"
                                onClick={handleRefreshToken}
                                className="group flex w-full items-center gap-4 rounded-xl border border-slate-200 bg-white p-4 text-left transition duration-200 hover:border-blue-200 hover:bg-blue-50 focus:outline-none focus:ring-4 focus:ring-blue-500/10"
                            >
                                <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-blue-100 text-blue-600 transition group-hover:bg-blue-600 group-hover:text-white">

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
                                            d="M16.023 9.348h4.992V4.356M20.5 9.5A8.25 8.25 0 0 0 5.02 6.27M7.977 14.652H2.985v4.992M3.5 14.5a8.25 8.25 0 0 0 15.48 3.23"
                                        />
                                    </svg>

                                </div>

                                <div className="min-w-0 flex-1">
                                    <p className="font-semibold text-slate-800">
                                        Refresh Token
                                    </p>

                                    <p className="mt-1 text-xs text-slate-500">
                                        Renew your authentication token
                                    </p>
                                </div>

                                <svg
                                    xmlns="http://www.w3.org/2000/svg"
                                    fill="none"
                                    viewBox="0 0 24 24"
                                    strokeWidth={1.8}
                                    stroke="currentColor"
                                    className="h-5 w-5 shrink-0 text-slate-400 transition group-hover:translate-x-1 group-hover:text-blue-600"
                                >
                                    <path
                                        strokeLinecap="round"
                                        strokeLinejoin="round"
                                        d="M5 12h14m-7-7 7 7-7 7"
                                    />
                                </svg>
                            </button>

                            {/* Change Password */}
                            <button
                                type="button"
                                onClick={handleChangePassword}
                                className="group flex w-full items-center gap-4 rounded-xl border border-slate-200 bg-white p-4 text-left transition duration-200 hover:border-indigo-200 hover:bg-indigo-50 focus:outline-none focus:ring-4 focus:ring-indigo-500/10"
                            >
                                <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-indigo-100 text-indigo-600 transition group-hover:bg-indigo-600 group-hover:text-white">

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
                                            d="M16.5 10.5V6.75a4.5 4.5 0 0 0-9 0v3.75m-.75 0h10.5c.828 0 1.5.672 1.5 1.5v7.5c0 .828-.672 1.5-1.5 1.5H6.75a1.5 1.5 0 0 1-1.5-1.5V12c0-.828.672-1.5 1.5-1.5Z"
                                        />
                                    </svg>

                                </div>

                                <div className="min-w-0 flex-1">
                                    <p className="font-semibold text-slate-800">
                                        Change Password
                                    </p>

                                    <p className="mt-1 text-xs text-slate-500">
                                        Update your account password
                                    </p>
                                </div>

                                <svg
                                    xmlns="http://www.w3.org/2000/svg"
                                    fill="none"
                                    viewBox="0 0 24 24"
                                    strokeWidth={1.8}
                                    stroke="currentColor"
                                    className="h-5 w-5 shrink-0 text-slate-400 transition group-hover:translate-x-1 group-hover:text-indigo-600"
                                >
                                    <path
                                        strokeLinecap="round"
                                        strokeLinejoin="round"
                                        d="M5 12h14m-7-7 7 7-7 7"
                                    />
                                </svg>
                            </button>

                            {/* Logout */}
                            <button
                                type="button"
                                onClick={handleLogout}
                                className="group flex w-full items-center gap-4 rounded-xl border border-red-100 bg-red-50/50 p-4 text-left transition duration-200 hover:border-red-200 hover:bg-red-50 focus:outline-none focus:ring-4 focus:ring-red-500/10"
                            >
                                <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-red-100 text-red-600 transition group-hover:bg-red-600 group-hover:text-white">

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
                                            d="M15.75 9V5.25A2.25 2.25 0 0 0 13.5 3h-6A2.25 2.25 0 0 0 5.25 5.25v13.5A2.25 2.25 0 0 0 7.5 21h6a2.25 2.25 0 0 0 2.25-2.25V15M18 15l3-3m0 0-3-3m3 3H9"
                                        />
                                    </svg>

                                </div>

                                <div className="min-w-0 flex-1">
                                    <p className="font-semibold text-red-700">
                                        Logout
                                    </p>

                                    <p className="mt-1 text-xs text-red-500">
                                        Sign out of your account
                                    </p>
                                </div>

                                <svg
                                    xmlns="http://www.w3.org/2000/svg"
                                    fill="none"
                                    viewBox="0 0 24 24"
                                    strokeWidth={1.8}
                                    stroke="currentColor"
                                    className="h-5 w-5 shrink-0 text-red-400 transition group-hover:translate-x-1 group-hover:text-red-600"
                                >
                                    <path
                                        strokeLinecap="round"
                                        strokeLinejoin="round"
                                        d="M5 12h14m-7-7 7 7-7 7"
                                    />
                                </svg>
                            </button>

                        </div>
                    </div>

                    {/* Footer */}
                    <div className="border-t border-slate-100 bg-slate-50 px-6 py-4 text-center">
                        <p className="text-xs text-slate-400">
                            Manage your account securely
                        </p>
                    </div>

                </div>

                <p className="mt-5 text-center text-xs text-slate-400">
                    Account Dashboard
                </p>

            </div>
        </div>
    );
};

export default Dashboard;