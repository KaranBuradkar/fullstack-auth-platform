import { Routes, Route, Navigate } from "react-router-dom";

import Login from "../pages/auth/Login";
import Register from "../pages/auth/Register";
import Dashboard from "../pages/Dashboard";
import ChangePassword from "../pages/auth/ChangePassword";
import VerifyEmail from "../pages/auth/VerifyEmail";

import { useAuth } from "../hooks/useAuth";
import { APP_ROUTES } from "../config/AppRouteEndpoints";
import { UserProvider } from "../context/UserContext";
import UserProfile from "../pages/UserProfile";

const ProtectedRoute = ({ children }) => {
  const { isAuthenticated } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to={APP_ROUTES.AUTH.LOGIN} replace />;
  }

  return children;
};

const PublicRoute = ({ children }) => {
  const { isAuthenticated } = useAuth();

  // if (isAuthenticated) {
  //   return <Navigate to={APP_ROUTES.USER.DASHBOARD} replace />;
  // }

  return children;
};

const AppRoutes = () => {
  return (
    <UserProvider>
      <Routes>
        <Route
          path={APP_ROUTES.AUTH.LOGIN}
          element={
            <PublicRoute>
              <Login />
            </PublicRoute>
          }
        />

        <Route
          path={APP_ROUTES.AUTH.REGISTER}
          element={
            <PublicRoute>
              <Register />
            </PublicRoute>
          }
        />

        <Route
          path={APP_ROUTES.USER.DASHBOARD}
          element={
            <ProtectedRoute>
                <Dashboard />
              </ProtectedRoute>
          }
        />

        <Route
          path={APP_ROUTES.USER.PROFILE}
          element={
            <ProtectedRoute>
              <UserProfile />
            </ProtectedRoute>
          }
        />

        <Route
          path={APP_ROUTES.AUTH.CHANGE_PASSWORD}
          element={
            <ProtectedRoute>
              <ChangePassword />
            </ProtectedRoute>
          }
        />

        <Route
          path={APP_ROUTES.AUTH.VERIFY_EMAIL}
          element={
            <ProtectedRoute>
              <VerifyEmail />
            </ProtectedRoute>
          }
        />

        <Route
          path="*"
          element={<Navigate to={APP_ROUTES.AUTH.LOGIN} replace />}
        />
      </Routes>
    </UserProvider>
  );
};

export default AppRoutes;