import React from "react";
import { Navigate, Outlet, useLocation } from "react-router-dom";
import { usePlayerStore } from "../../stores/usePlayerStore";

export const JobChangeSelectGuard: React.FC = () => {
  const { jobChangeStatus } = usePlayerStore();
  const location = useLocation();

  const needsClassSelection = jobChangeStatus === "AWAITING_CLASS_SELECTION";

  if (needsClassSelection && location.pathname !== "/job-change/select-class") {
    if (location.pathname === "/red-gate") {
      return <Outlet />;
    }
    return <Navigate to="/job-change/select-class" replace />;
  }

  return <Outlet />;
};
