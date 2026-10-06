import { Routes, Route, Navigate } from "react-router-dom";

import Login from "../pages/auth//Login";
import Register from "../pages/auth//Register";
import Dashboard from "../pages/Dashboard";

import { useAuth } from "../context/AuthContext";
import { AUTH_ROUTES } from "../config/authRoutes";

const ProtectedRoute = ({ children }) => {
  const { isAuthenticated } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to={AUTH_ROUTES.LOGIN} replace />;
  }

  return children;
};

const PublicRoute = ({ children }) => {
  const { isAuthenticated } = useAuth();

  if (isAuthenticated) {
    return <Navigate to={AUTH_ROUTES.DASHBOARD} replace />;
  }

  return children;
};

const AppRoutes = () => {
  return (
    <Routes>
      <Route
        path={AUTH_ROUTES.LOGIN}
        element={
          <PublicRoute>
            <Login />
          </PublicRoute>
        }
      />

      <Route
        path={AUTH_ROUTES.REGISTER}
        element={
          <PublicRoute>
            <Register />
          </PublicRoute>
        }
      />

      <Route
        path={AUTH_ROUTES.DASHBOARD}
        element={
          <ProtectedRoute>
            <Dashboard />
          </ProtectedRoute>
        }
      />

      <Route
        path="*"
        element={<Navigate to={AUTH_ROUTES.LOGIN} replace />}
      />
    </Routes>
  );
};

export default AppRoutes;