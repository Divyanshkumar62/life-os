import React from "react";
import { Navigate, Outlet, useLocation } from "react-router-dom";
import { usePlayerStore } from "../../stores/usePlayerStore";

export const DungeonBreakGuard: React.FC = () => {
  const { dungeonBreakActive } = usePlayerStore();
  const location = useLocation();

  // If a Dungeon Break event is active and unacknowledged, redirect to /dungeon-break.
  if (dungeonBreakActive && location.pathname !== "/dungeon-break") {
    return <Navigate to="/dungeon-break" replace />;
  }

  // If Dungeon Break is NOT active and user tries to access /dungeon-break manually, redirect them to dashboard.
  if (!dungeonBreakActive && location.pathname === "/dungeon-break") {
    return <Navigate to="/" replace />;
  }

  return <Outlet />;
};
