import React from "react";
import { Navigate, Outlet, useLocation } from "react-router-dom";
import { usePlayerStore } from "../../stores/usePlayerStore";

export const RedGateGuard: React.FC = () => {
  const { activeModifiers } = usePlayerStore();
  const location = useLocation();

  const isRedGateActive = activeModifiers.includes("EVENT_FROZEN") || activeModifiers.includes("RED_GATE_ACTIVE");

  // Red Gate is the highest priority guard. If active, intercept all routes and redirect to /red-gate.
  if (isRedGateActive && location.pathname !== "/red-gate") {
    return <Navigate to="/red-gate" replace />;
  }

  // If Red Gate is NOT active and user tries to access /red-gate manually, redirect them to dashboard.
  if (!isRedGateActive && location.pathname === "/red-gate") {
    return <Navigate to="/" replace />;
  }

  return <Outlet />;
};
