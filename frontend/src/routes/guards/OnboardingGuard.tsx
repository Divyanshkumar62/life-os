import React from "react";
import { Navigate, Outlet } from "react-router-dom";
import { usePlayerStore } from "../../stores/usePlayerStore";

export const OnboardingGuard: React.FC = () => {
  const { onboardingCompleted, playerId } = usePlayerStore();

  // If there is no active playerId or onboarding is explicitly marked as not completed,
  // redirect the player to the onboarding registration flow.
  if (!playerId || !onboardingCompleted) {
    return <Navigate to="/onboarding" replace />;
  }

  return <Outlet />;
};
