import { useEffect, useState } from "react";
import { useUser } from '../context/UserContext';

const UserProfile = () => {

    const { fetchUserProfile } = useUser();

    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        const loadProfile = async () => {
            try {
                const response = await fetchUserProfile();
                setUser(response.data);

            } catch (error) {
                setError(error.response?.data?.message || "Unable to load user profile.");
            } finally {
                setLoading(false);
            }
        };

        loadProfile();
    }, [fetchUserProfile]);

    return (
        <div>
            {/* User Profile */}
            <div className="border-t border-slate-100 px-6 py-7 sm:px-8">
                <h2 className="mb-5 text-sm font-semibold uppercase tracking-wider text-slate-400">
                    User Profile
                </h2>

                {loading ? (
                    <div className="flex items-center gap-3 py-6 text-sm text-slate-500">
                        <div className="h-5 w-5 animate-spin rounded-full border-2 border-blue-200 border-t-blue-600" />
                        Loading profile...
                    </div>
                ) : user ? (
                    <div className="space-y-5">

                        {/* Profile Header */}
                        <div className="flex items-center gap-4">
                            {user.profilePictureUrl ? (
                                <img
                                    src={user.profilePictureUrl}
                                    alt={user.fullName || "Profile"}
                                    className="h-16 w-16 rounded-full object-cover ring-4 ring-blue-50"
                                />
                            ) : (
                                <div className="flex h-16 w-16 shrink-0 items-center justify-center rounded-full bg-blue-100 text-2xl font-bold text-blue-600 ring-4 ring-blue-50">
                                    {user.fullName?.charAt(0)?.toUpperCase() || "U"}
                                </div>
                            )}

                            <div className="min-w-0">
                                <h3 className="break-words text-lg font-bold text-slate-900">
                                    {user.fullName || "Name not available"}
                                </h3>

                                <p className="break-all text-sm text-slate-500">
                                    {user.email || "Email not available"}
                                </p>

                                <span
                                    className={`mt-2 inline-flex rounded-full px-2.5 py-1 text-xs font-semibold ${
                                        user.status === "ACTIVE"
                                            ? "bg-emerald-100 text-emerald-700"
                                            : "bg-red-100 text-red-700"
                                    }`}
                                >
                                    {user.status || "UNKNOWN"}
                                </span>
                            </div>
                        </div>

                        {/* Profile Details */}
                        <div className="overflow-hidden rounded-xl border border-slate-200 divide-y divide-slate-100">

                            <ProfileField
                                label="User ID"
                                value={user.id}
                            />

                            <ProfileField
                                label="Full Name"
                                value={user.fullName}
                            />

                            <ProfileField
                                label="Email"
                                value={user.email}
                            />

                            <ProfileField
                                label="Role"
                                value={user.role}
                            />

                            <ProfileField
                                label="Authentication Provider"
                                value={user.provider}
                            />

                            <ProfileField
                                label="Email Verified"
                                value={user.emailVerified ? "Verified" : "Not Verified"}
                                badge={user.emailVerified}
                            />

                            <ProfileField
                                label="Account Status"
                                value={user.status}
                                badge={user.status === "ACTIVE"}
                            />

                            <ProfileField
                                label="Created At"
                                value={
                                    user.createdAt
                                        ? new Date(user.createdAt).toLocaleString()
                                        : null
                                }
                            />

                            <ProfileField
                                label="Last Updated"
                                value={
                                    user.updatedAt
                                        ? new Date(user.updatedAt).toLocaleString()
                                        : null
                                }
                            />

                        </div>
                    </div>
                ) : (
                    <div className="rounded-xl border border-amber-200 bg-amber-50 p-4">
                        <p className="text-sm font-semibold text-amber-800">
                            Profile unavailable
                        </p>

                        <p className="mt-1 text-sm text-amber-700">
                            {error || "Your profile could not be loaded. You can still use the dashboard actions."}
                        </p>
                    </div>
                )}
            </div>
        </div>
    );
};

const ProfileField = ({ label, value, badge }) => {
    return (
        <div className="flex flex-col gap-1 px-4 py-3 sm:flex-row sm:items-center sm:justify-between">
            <span className="text-sm text-slate-500">
                {label}
            </span>

            {badge !== undefined ? (
                <span
                    className={`inline-flex w-fit rounded-full px-2.5 py-1 text-xs font-semibold ${
                        badge
                            ? "bg-emerald-100 text-emerald-700"
                            : "bg-amber-100 text-amber-700"
                    }`}
                >
                    {value ?? "Unknown"}
                </span>
            ) : (
                <span className="break-all text-sm font-medium text-slate-800 sm:text-right">
                    {value ?? "Not available"}
                </span>
            )}
        </div>
    );
};

export default UserProfile;