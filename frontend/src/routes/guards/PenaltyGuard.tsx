import React from "react";
import { Navigate, Outlet, useLocation } from "react-router-dom";
import { usePlayerStore } from "../../stores/usePlayerStore";

export const PenaltyGuard: React.FC = () => {
  const { penaltyActive, activeModifiers } = usePlayerStore();
  const location = useLocation();

  const isPenaltyZoneActive = penaltyActive || activeModifiers.includes("PENALTY_ZONE");

  // Force redirect to /penalty if penalty zone is active and user is trying to access other screens.
  if (isPenaltyZoneActive && location.pathname !== "/penalty") {
    return <Navigate to="/penalty" replace />;
  }

  // If penalty zone is NOT active and user tries to access /penalty manually, redirect them to dashboard.
  if (!isPenaltyZoneActive && location.pathname === "/penalty") {
    return <Navigate to="/" replace />;
  }

  return <Outlet />;
};
