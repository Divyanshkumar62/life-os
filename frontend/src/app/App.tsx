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
import { ObserverScreen } from "../screens/ObserverScreen";

import { SystemProvider, useSystemContext } from "../context/SystemContext";
import { PenaltyZoneScreen } from "../screens/PenaltyZone/PenaltyZoneScreen";
import { RedGateProvider, useRedGateContext } from "../context/RedGateContext";
import { RedGatePopup } from "../components/features/RedGate/RedGatePopup";
import { JobChangePopup } from "../components/features/JobChange/JobChangePopup";
import { useSystemAudio } from "../hooks/useSystemAudio";
import { SystemToast } from "../components/system";
import { DungeonView } from "../screens/Dungeon/DungeonView";
import { QASandboxScreen } from "../screens/QA/QASandboxScreen";
import { setSandboxView as setApiSandboxView } from "../api/api";
import { X, Sliders } from "lucide-react";

type Screen =
  | "dashboard"
  | "system_log"
  | "diagnostic"
  | "profile"
  | "missions"
  | "onboarding"
  | "store"
  | "inventory"
  | "system_gate"
  | "dungeon"
  | "observer";

function AppContent({
  playerId,
  setPlayerId,
  sandboxActive,
  setSandboxActive,
  sandboxView,
  setSandboxView,
}: {
  playerId: string | null;
  setPlayerId: (id: string) => void;
  sandboxActive: boolean;
  setSandboxActive: (active: boolean) => void;
  sandboxView: string | null;
  setSandboxView: (view: string | null) => void;
}) {
  const [currentScreen, setCurrentScreen] = useState<Screen>("onboarding");
  const [activeDungeonId, setActiveDungeonId] = useState<string | null>(null);
  const { statusWindow, jobClass, theme } = useSystemContext();
  const { redGate, jobChange, isShopLocked, isInventoryLocked } = useRedGateContext();
  const { playRedGateAlarm } = useSystemAudio();

  const isPenaltyActive = !!statusWindow?.systemState?.penaltyActive;

  useEffect(() => {
    if (jobClass) {
      document.body.className = theme;
    }

    // Red Gate takes priority over everything
    if (redGate.isActive && !sandboxActive) {
      document.body.classList.add("theme-red");
      playRedGateAlarm();
    } else if (isPenaltyActive && !sandboxActive) {
      document.body.classList.add("theme-red");
      playRedGateAlarm();
    } else {
      document.body.classList.remove("theme-red");
    }
  }, [isPenaltyActive, jobClass, theme, redGate.isActive, playRedGateAlarm, sandboxActive]);

  // Render floating back to sandbox button
  const renderBackToSandboxBtn = () => (
    <button
      onClick={() => {
        setSandboxView(null);
        setApiSandboxView(null);
      }}
      className="fixed top-4 right-4 z-[9999] bg-black/85 hover:bg-solo-red/20 border border-gray-800 hover:border-solo-red text-white p-2.5 rounded-none font-mono text-xs tracking-widest uppercase transition-all duration-300 flex items-center gap-1.5 shadow-[0_0_15px_rgba(0,0,0,0.5)]"
      title="Back to Sandbox"
    >
      <X size={14} className="text-solo-red" />
      <span>Exit Mock</span>
    </button>
  );

  // QA Sandbox Layout Interception
  if (sandboxActive) {
    if (!sandboxView) {
      return (
        <QASandboxScreen
          onSelectView={(view) => {
            setSandboxView(view);
            setApiSandboxView(view);
          }}
          onClose={() => {
            setSandboxActive(false);
            setSandboxView(null);
            setApiSandboxView(null);
          }}
        />
      );
    }

    if (sandboxView === "dashboard") {
      return (
        <>
          <DashboardView
            playerId={playerId}
            onViewSystemLog={() => {}}
            onViewDiagnostic={() => {}}
            onViewProfile={() => {}}
            onViewMissions={() => {}}
            onViewStore={() => {}}
            onViewInventory={() => {}}
            onViewGate={() => {}}
            onViewObserver={() => {}}
          />
          {renderBackToSandboxBtn()}
        </>
      );
    }

    if (sandboxView === "dungeon") {
      return (
        <>
          <DungeonView
            playerId={playerId}
            projectId="dungeon-goblin-lair"
            onBack={() => {
              setSandboxView(null);
              setApiSandboxView(null);
            }}
          />
          {renderBackToSandboxBtn()}
        </>
      );
    }

    if (sandboxView === "penalty") {
      return (
        <>
          <PenaltyZoneScreen playerId={playerId} />
          {renderBackToSandboxBtn()}
        </>
      );
    }

    if (sandboxView === "store") {
      return (
        <>
          <div className="relative">
            <StoreScreen playerId={playerId} />
          </div>
          {renderBackToSandboxBtn()}
        </>
      );
    }

    if (sandboxView === "job_change") {
      return (
        <>
          <div className="min-h-screen bg-black relative flex items-center justify-center">
            <JobChangePopup isOpen={true} />
          </div>
          {renderBackToSandboxBtn()}
        </>
      );
    }

    if (sandboxView === "observer") {
      return (
        <>
          <ObserverScreen
            playerId={playerId}
            onBack={() => {
              setSandboxView(null);
              setApiSandboxView(null);
            }}
          />
          {renderBackToSandboxBtn()}
        </>
      );
    }
  }

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

  // Penalty Override (render full confession screen)
  if (isPenaltyActive) {
    return (
      <motion.div
        className="min-h-screen bg-[#0f0404]"
        animate={{ x: [0, -20, 20, -10, 10, 0] }}
        transition={{ duration: 0.3 }}
      >
        <PenaltyZoneScreen playerId={playerId} />
      </motion.div>
    );
  }

  // Job Change Popup (when awaiting acceptance or in progress)
  const showJobChangePopup = jobChange.status === 'AWAITING_ACCEPTANCE' || 
                             jobChange.status === 'IN_PROGRESS' || 
                             jobChange.status === 'COOLDOWN' ||
                             statusWindow?.identity?.jobChangeStatus === 'AWAITING_ACCEPTANCE' ||
                             statusWindow?.identity?.jobChangeStatus === 'IN_PROGRESS' ||
                             statusWindow?.identity?.jobChangeStatus === 'COOLDOWN';

  let viewToRender;

  if (currentScreen === "onboarding") {
    viewToRender = (
      <OnboardingFlow
        onComplete={(id) => {
          setPlayerId(id);
          setCurrentScreen("dashboard");
        }}
      />
    );
  } else if (currentScreen === "system_log") {
    viewToRender = <SystemLogView onBack={() => setCurrentScreen("dashboard")} />;
  } else if (currentScreen === "diagnostic") {
    viewToRender = <DiagnosticView onBack={() => setCurrentScreen("dashboard")} />;
  } else if (currentScreen === "profile") {
    viewToRender = <HunterProfileView onBack={() => setCurrentScreen("dashboard")} />;
  } else if (currentScreen === "missions") {
    console.log("Rendering ActiveMissionsView");
    viewToRender = <ActiveMissionsView playerId={playerId} onBack={() => setCurrentScreen("dashboard")} />;
  } else if (currentScreen === "store") {
    if (isShopLocked) {
      viewToRender = (
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
    } else {
      viewToRender = (
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
  } else if (currentScreen === "inventory") {
    if (isInventoryLocked) {
      viewToRender = (
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
    } else {
      viewToRender = (
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
  } else if (currentScreen === "system_gate") {
    viewToRender = (
      <SystemGateView
        playerId={playerId}
        onBack={() => setCurrentScreen("dashboard")}
        onEnterDungeon={(id) => {
          setActiveDungeonId(id);
          setCurrentScreen("dungeon");
        }}
      />
    );
  } else if (currentScreen === "observer") {
    viewToRender = <ObserverScreen playerId={playerId} onBack={() => setCurrentScreen("dashboard")} />;
  } else if (currentScreen === "dungeon" && activeDungeonId) {
    viewToRender = (
      <DungeonView 
        playerId={playerId} 
        projectId={activeDungeonId} 
        onBack={() => {
          setActiveDungeonId(null);
          setCurrentScreen("system_gate");
        }} 
      />
    );
  } else {
    viewToRender = (
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
          onViewObserver={() => setCurrentScreen("observer")}
        />
        <JobChangePopup isOpen={showJobChangePopup && currentScreen === "dashboard"} />
      </>
    );
  }

  return (
    <>
      {viewToRender}
      {!sandboxActive && (
        <button
          onClick={() => {
            setSandboxActive(true);
            setSandboxView(null);
            setApiSandboxView(null);
          }}
          className="fixed bottom-4 right-4 z-[9999] bg-black/90 hover:bg-solo-blue-900/30 border border-solo-cyan/40 hover:border-solo-cyan text-solo-cyan hover:text-white px-3 py-1.5 font-mono text-[9px] tracking-widest uppercase transition-all duration-300 flex items-center gap-1.5 shadow-glow-cyan animate-pulse"
        >
          <Sliders size={10} />
          <span>QA Sandbox (`)</span>
        </button>
      )}
    </>
  );
}

function App() {
  const [playerId, setPlayerId] = useState<string | null>(null);
  const [sandboxActive, setSandboxActive] = useState(false);
  const [sandboxView, setSandboxView] = useState<string | null>(null);

  // Bind keydown event to backtick ` key
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === "`") {
        setSandboxActive((prev) => {
          const next = !prev;
          if (!next) {
            setSandboxView(null);
            setApiSandboxView(null);
          }
          return next;
        });
      }
    };
    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, []);

  return (
    <SystemProvider 
      playerId={playerId || ""}
      sandboxActive={sandboxActive}
      sandboxView={sandboxView}
    >
      <RedGateProvider 
        playerId={playerId || ""}
        sandboxActive={sandboxActive}
        sandboxView={sandboxView}
      >
        <AppContent 
          playerId={playerId} 
          setPlayerId={setPlayerId}
          sandboxActive={sandboxActive}
          setSandboxActive={setSandboxActive}
          sandboxView={sandboxView}
          setSandboxView={setSandboxView}
        />
        <SystemToast />
      </RedGateProvider>
    </SystemProvider>
  );
}

export default App;
