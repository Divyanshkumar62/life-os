import { useState, useEffect } from "react";
import { motion } from "framer-motion";
import { BrowserRouter, Routes, Route, useNavigate, useParams, Navigate } from "react-router-dom";
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

import { SystemProvider, useSystemContext } from "../providers/SystemProvider";
import { PenaltyZoneScreen } from "../screens/PenaltyZone/PenaltyZoneScreen";
import { RedGateProvider, useRedGateContext } from "../context/RedGateContext";
import { JobChangePopup } from "../components/features/JobChange/JobChangePopup";
import { useSystemAudio } from "../hooks/useSystemAudio";
import { SystemToast } from "../components/system";
import { DungeonView } from "../screens/Dungeon/DungeonView";
import { QASandboxScreen } from "../screens/QA/QASandboxScreen";
import { setSandboxView as setApiSandboxView } from "../api/api";
import { X, Sliders } from "lucide-react";

// Route Guards & Screen Overrides
import { RedGateGuard } from "../routes/guards/RedGateGuard";
import { PenaltyGuard } from "../routes/guards/PenaltyGuard";
import { OnboardingGuard } from "../routes/guards/OnboardingGuard";
import { RedGateOverrideScreen } from "../features/events/components/RedGateOverrideScreen";
import { usePlayerStore } from "../stores/usePlayerStore";

function DungeonRouteWrapper({ playerId, navigate }: { playerId: string | null; navigate: any }) {
  const { activeDungeonId } = useParams();
  return (
    <DungeonView 
      playerId={playerId} 
      projectId={activeDungeonId || ""} 
      onBack={() => navigate("/gate")} 
    />
  );
}

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
  const navigate = useNavigate();
  const { statusWindow, theme } = useSystemContext();
  const { redGate, jobChange, isShopLocked, isInventoryLocked } = useRedGateContext();
  const { playRedGateAlarm } = useSystemAudio();

  const fetchPlayerState = usePlayerStore((state) => state.fetchPlayerState);
  const setOnboardingCompleted = usePlayerStore((state) => state.setOnboardingCompleted);

  const isPenaltyActive = !!statusWindow?.systemState?.penaltyActive;

  // Sync route and Zustand player state
  useEffect(() => {
    if (playerId) {
      fetchPlayerState(playerId);
      setOnboardingCompleted(true);
    }
  }, [playerId, fetchPlayerState, setOnboardingCompleted]);

  useEffect(() => {
    if (redGate.isActive && !sandboxActive) {
      document.body.classList.add("theme-red");
      playRedGateAlarm();
    } else if (isPenaltyActive && !sandboxActive) {
      document.body.classList.add("theme-red");
      playRedGateAlarm();
    } else {
      document.body.classList.remove("theme-red");
    }
  }, [isPenaltyActive, theme, redGate.isActive, playRedGateAlarm, sandboxActive]);

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

    if (sandboxView === "onboarding") {
      return (
        <>
          <OnboardingFlow
            onComplete={() => {
              setSandboxView(null);
              setApiSandboxView(null);
            }}
          />
          {renderBackToSandboxBtn()}
        </>
      );
    }

    if (sandboxView === "profile") {
      return (
        <>
          <HunterProfileView
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

  // Job Change Popup condition
  const showJobChangePopup = jobChange.status === 'AWAITING_ACCEPTANCE' || 
                             jobChange.status === 'IN_PROGRESS' || 
                             jobChange.status === 'COOLDOWN' ||
                             statusWindow?.identity?.jobChangeStatus === 'AWAITING_ACCEPTANCE' ||
                             statusWindow?.identity?.jobChangeStatus === 'IN_PROGRESS' ||
                             statusWindow?.identity?.jobChangeStatus === 'COOLDOWN';

  return (
    <>
      <Routes>
        {/* Onboarding registration flow is outside OnboardingGuard */}
        <Route 
          path="/onboarding" 
          element={
            <OnboardingFlow
              onComplete={(id) => {
                setPlayerId(id);
                setOnboardingCompleted(true);
                navigate("/dashboard");
              }}
            />
          } 
        />

        {/* OnboardingGuard blocks all gameplay routes if onboarding is not completed */}
        <Route element={<OnboardingGuard />}>
          {/* Red Gate Overrides everything else */}
          <Route path="/red-gate" element={<RedGateOverrideScreen />} />

          {/* Hierarchical Priority: RedGateGuard -> PenaltyGuard */}
          <Route element={<RedGateGuard />}>
            <Route path="/penalty" element={<PenaltyZoneScreen playerId={playerId} />} />
            
            <Route element={<PenaltyGuard />}>
              <Route path="/" element={<Navigate to="/dashboard" replace />} />
              <Route 
                path="/dashboard" 
                element={
                  <>
                    <DashboardView
                      playerId={playerId}
                      onViewSystemLog={() => navigate("/system_log")}
                      onViewDiagnostic={() => navigate("/diagnostic")}
                      onViewProfile={() => navigate("/profile")}
                      onViewMissions={() => navigate("/missions")}
                      onViewStore={() => navigate("/store")}
                      onViewInventory={() => navigate("/inventory")}
                      onViewGate={() => navigate("/gate")}
                      onViewObserver={() => navigate("/observer")}
                    />
                    <JobChangePopup isOpen={showJobChangePopup} />
                  </>
                } 
              />
              <Route path="/system_log" element={<SystemLogView onBack={() => navigate("/dashboard")} />} />
              <Route path="/diagnostic" element={<DiagnosticView onBack={() => navigate("/dashboard")} />} />
              <Route path="/profile" element={<HunterProfileView onBack={() => navigate("/dashboard")} />} />
              <Route path="/missions" element={<ActiveMissionsView playerId={playerId} onBack={() => navigate("/dashboard")} />} />
              
              <Route 
                path="/store" 
                element={
                  isShopLocked ? (
                    <motion.div
                      className="min-h-screen bg-black flex items-center justify-center"
                      initial={{ opacity: 0 }}
                      animate={{ opacity: 1 }}
                    >
                      <div className="text-center">
                        <h2 className="text-2xl text-red-500 font-bold mb-4">SYSTEM INTERFERENCE</h2>
                        <p className="text-gray-400">Store is locked during Red Gate survival.</p>
                        <button
                          onClick={() => navigate("/dashboard")}
                          className="mt-4 px-4 py-2 bg-gray-800 text-white rounded shadow-glow-red hover:bg-gray-700 transition"
                        >
                          Return to Dashboard
                        </button>
                      </div>
                    </motion.div>
                  ) : (
                    <div className="relative">
                      <button
                        onClick={() => navigate("/dashboard")}
                        className="absolute top-4 left-4 z-50 bg-black/50 text-white p-2 rounded-full hover:bg-solo-blue-900/50"
                      >
                        Back
                      </button>
                      <StoreScreen playerId={playerId} />
                    </div>
                  )
                } 
              />

              <Route 
                path="/inventory" 
                element={
                  isInventoryLocked ? (
                    <motion.div
                      className="min-h-screen bg-black flex items-center justify-center"
                      initial={{ opacity: 0 }}
                      animate={{ opacity: 1 }}
                    >
                      <div className="text-center">
                        <h2 className="text-2xl text-red-500 font-bold mb-4">SYSTEM INTERFERENCE</h2>
                        <p className="text-gray-400">Inventory is locked during Red Gate survival.</p>
                        <button
                          onClick={() => navigate("/dashboard")}
                          className="mt-4 px-4 py-2 bg-gray-800 text-white rounded shadow-glow-red hover:bg-gray-700 transition"
                        >
                          Return to Dashboard
                        </button>
                      </div>
                    </motion.div>
                  ) : (
                    <div className="relative">
                      <button
                        onClick={() => navigate("/dashboard")}
                        className="absolute top-4 left-4 z-50 bg-black/50 text-white p-2 rounded-full hover:bg-solo-blue-900/50"
                      >
                        Back
                      </button>
                      <InventoryScreen playerId={playerId} />
                    </div>
                  )
                } 
              />

              <Route 
                path="/gate" 
                element={
                  <SystemGateView
                    playerId={playerId}
                    onBack={() => navigate("/dashboard")}
                    onEnterDungeon={(id) => navigate(`/dungeon/${id}`)}
                  />
                } 
              />

              <Route 
                path="/observer" 
                element={
                  <ObserverScreen playerId={playerId} onBack={() => navigate("/dashboard")} />
                } 
              />

              <Route 
                path="/dungeon/:activeDungeonId" 
                element={<DungeonRouteWrapper playerId={playerId} navigate={navigate} />} 
              />
            </Route>
          </Route>
        </Route>
      </Routes>

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
    <BrowserRouter>
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
    </BrowserRouter>
  );
}

export default App;
