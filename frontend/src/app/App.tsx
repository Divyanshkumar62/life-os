import { useState, useEffect } from "react";
import { motion } from "framer-motion";
import { OnboardingFlow } from "../screens/Onboarding/OnboardingFlow";
import { DashboardView } from "../screens/Dashboard/DashboardView";
import { SystemLogView } from "../screens/SystemLog/SystemLogView";
import { DiagnosticView } from "../screens/Diagnostic/DiagnosticView";
import { HunterProfileView } from "../screens/Profile/HunterProfileView";
import { ActiveMissionsView } from "../screens/Missions/ActiveMissionsView";
import { StoreScreen } from "../screens/StoreScreen";
import { InventoryScreen } from "../screens/InventoryScreen";
import { SystemGateView } from "../screens/SystemGate/SystemGateView";

import { SystemProvider, useSystemContext } from "../context/SystemContext";
import { RedGateProvider, useRedGateContext } from "../context/RedGateContext";
import { PenaltyPopup } from "../components/features/PenaltyZone/PenaltyPopup";
import { RedGatePopup } from "../components/features/RedGate/RedGatePopup";
import { JobChangePopup } from "../components/features/JobChange/JobChangePopup";
import { useSystemAudio } from "../hooks/useSystemAudio";

type Screen =
  | "dashboard"
  | "system_log"
  | "diagnostic"
  | "profile"
  | "missions"
  | "onboarding"
  | "store"
  | "inventory"
  | "system_gate";

function AppContent({
  playerId,
  setPlayerId,
}: {
  playerId: string | null;
  setPlayerId: (id: string) => void;
}) {
  const [currentScreen, setCurrentScreen] = useState<Screen>("onboarding");
  const { statusWindow, jobClass, theme } = useSystemContext();
  const { redGate, jobChange, isShopLocked, isInventoryLocked } = useRedGateContext();
  const { playRedGateAlarm } = useSystemAudio();

  const isPenaltyActive = !!statusWindow?.systemState?.penaltyActive;

  useEffect(() => {
    if (jobClass) {
      document.body.className = theme;
    }

    // Red Gate takes priority over everything
    if (redGate.isActive) {
      document.body.classList.add("theme-red");
      playRedGateAlarm();
    } else if (isPenaltyActive) {
      document.body.classList.add("theme-red");
      playRedGateAlarm();
    } else {
      document.body.classList.remove("theme-red");
    }
  }, [isPenaltyActive, jobClass, theme, redGate.isActive, playRedGateAlarm]);

  // Red Gate Override - Lock out entire Dashboard with Glitch Physics
  if (redGate.isActive) {
    return (
      <motion.div
        className="min-h-screen bg-[#0f0404]"
        animate={{ x: [0, -20, 20, -10, 10, 0] }}
        transition={{ duration: 0.3, repeat: Infinity, repeatDelay: 3 }}
      >
        <RedGatePopup isOpen={true} />
      </motion.div>
    );
  }

  // Penalty Override (fallback)
  if (isPenaltyActive) {
    return (
      <motion.div
        className="min-h-screen bg-[#0f0404]"
        animate={{ x: [0, -20, 20, -10, 10, 0] }}
        transition={{ duration: 0.3 }}
      >
        <PenaltyPopup isOpen={true} onClose={() => {}} />
      </motion.div>
    );
  }

  // Job Change Popup (when awaiting acceptance or in progress)
  const showJobChangePopup = jobChange.status === 'AWAITING_ACCEPTANCE' || 
                             jobChange.status === 'IN_PROGRESS' || 
                             jobChange.status === 'COOLDOWN';

  if (currentScreen === "onboarding") {
    return (
      <OnboardingFlow
        onComplete={(id) => {
          setPlayerId(id);
          setCurrentScreen("dashboard");
        }}
      />
    );
  }

  if (currentScreen === "system_log") {
    return <SystemLogView onBack={() => setCurrentScreen("dashboard")} />;
  }

  if (currentScreen === "diagnostic") {
    return <DiagnosticView onBack={() => setCurrentScreen("dashboard")} />;
  }

  if (currentScreen === "profile") {
    return <HunterProfileView onBack={() => setCurrentScreen("dashboard")} />;
  }

  if (currentScreen === "missions") {
    console.log("Rendering ActiveMissionsView");
    return <ActiveMissionsView onBack={() => setCurrentScreen("dashboard")} />;
  }

  if (currentScreen === "store") {
    if (isShopLocked) {
      return (
        <motion.div
          className="min-h-screen bg-black flex items-center justify-center"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
        >
          <div className="text-center">
            <h2 className="text-2xl text-red-500 font-bold mb-4">SYSTEM INTERFERENCE</h2>
            <p className="text-gray-400">Store is locked during Red Gate survival.</p>
            <button
              onClick={() => setCurrentScreen("dashboard")}
              className="mt-4 px-4 py-2 bg-gray-800 text-white rounded"
            >
              Return to Dashboard
            </button>
          </div>
        </motion.div>
      );
    }
    return (
      <div className="relative">
        <button
          onClick={() => setCurrentScreen("dashboard")}
          className="absolute top-4 left-4 z-50 bg-black/50 text-white p-2 rounded-full hover:bg-solo-blue-900/50"
        >
          Back
        </button>
        <StoreScreen playerId={playerId} />
      </div>
    );
  }

  if (currentScreen === "inventory") {
    if (isInventoryLocked) {
      return (
        <motion.div
          className="min-h-screen bg-black flex items-center justify-center"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
        >
          <div className="text-center">
            <h2 className="text-2xl text-red-500 font-bold mb-4">SYSTEM INTERFERENCE</h2>
            <p className="text-gray-400">Inventory is locked during Red Gate survival.</p>
            <button
              onClick={() => setCurrentScreen("dashboard")}
              className="mt-4 px-4 py-2 bg-gray-800 text-white rounded"
            >
              Return to Dashboard
            </button>
          </div>
        </motion.div>
      );
    }
    return (
      <div className="relative">
        <button
          onClick={() => setCurrentScreen("dashboard")}
          className="absolute top-4 left-4 z-50 bg-black/50 text-white p-2 rounded-full hover:bg-solo-blue-900/50"
        >
          Back
        </button>
        <InventoryScreen playerId={playerId} />
      </div>
    );
  }

  if (currentScreen === "system_gate") {
    return (
      <SystemGateView
        playerId={playerId}
        onBack={() => setCurrentScreen("dashboard")}
      />
    );
  }

  return (
    <>
      <DashboardView
        playerId={playerId}
        onViewSystemLog={() => setCurrentScreen("system_log")}
        onViewDiagnostic={() => setCurrentScreen("diagnostic")}
        onViewProfile={() => setCurrentScreen("profile")}
        onViewMissions={() => {
          console.log("App: Setting screen to missions");
          setCurrentScreen("missions");
        }}
        onViewStore={() => setCurrentScreen("store")}
        onViewInventory={() => setCurrentScreen("inventory")}
        onViewGate={() => setCurrentScreen("system_gate")}
      />
      <JobChangePopup isOpen={showJobChangePopup && currentScreen === "dashboard"} />
    </>
  );
}

function App() {
  const [playerId, setPlayerId] = useState<string | null>(null);

  return (
    <SystemProvider playerId={playerId || ""}>
      <RedGateProvider playerId={playerId || ""}>
        <AppContent playerId={playerId} setPlayerId={setPlayerId} />
      </RedGateProvider>
    </SystemProvider>
  );
}

export default App;
