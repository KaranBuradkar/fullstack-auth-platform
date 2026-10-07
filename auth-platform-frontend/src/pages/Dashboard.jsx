import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { AUTH_ROUTES } from "../config/authRoutes";

const Dashboard = () => {
  const { logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate(AUTH_ROUTES.LOGIN);
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100 px-4">
      <div className="w-full max-w-md rounded-2xl bg-white p-8 text-center shadow-lg">

        <h1 className="mb-3 text-3xl font-bold text-gray-800">
          Dashboard
        </h1>

        <p className="mb-6 text-gray-600">
          You are authenticated.
        </p>

        <button
          onClick={handleLogout}
          className="w-full rounded-lg bg-red-600 px-4 py-3 font-semibold text-white 
          transition hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-red-300"
        >
          Logout
        </button>

      </div>
    </div>
  );
};

export default Dashboard;