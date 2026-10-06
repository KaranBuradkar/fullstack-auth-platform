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
    <div>
      <h1>Dashboard</h1>

      <p>You are authenticated.</p>

      <button onClick={handleLogout}>
        Logout
      </button>
    </div>
  );
};

export default Dashboard;