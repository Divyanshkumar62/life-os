import React, { useEffect, useState, createContext, useContext, useCallback, useMemo } from "react";
import { usePlayerStore } from "../stores/usePlayerStore";

interface SystemStateContextType {
  playerId: string | null;
  statusWindow: any | null;
  jobClass: string | null;
  theme: string;
  loading: boolean;
  error: string | null;
  refreshSystem: () => Promise<void>;
  applyTheme: (jobClass: string) => void;
}

const SystemContext = createContext<SystemStateContextType | undefined>(undefined);

export const useSystemContext = () => {
  const context = useContext(SystemContext);
  if (!context) {
    throw new Error("useSystemContext must be used within a SystemProvider");
  }
  return context;
};

export const SystemProvider: React.FC<{
  children: React.ReactNode;
  playerId: string;
  sandboxActive?: boolean;
  sandboxView?: string | null;
}> = ({ children, playerId }) => {
  const player = usePlayerStore();
  const fetchPlayerState = usePlayerStore((state) => state.fetchPlayerState);
  const [theme, setTheme] = useState<string>("theme-default");

  const applyTheme = useCallback((jobClass: string) => {
    let themeClass = "theme-default";

    if (
      jobClass === "VANGUARD" ||
      jobClass === "Silver Knight" ||
      jobClass === "Berserker"
    ) {
      themeClass = "theme-vanguard";
    } else if (
      jobClass === "SCHOLAR" ||
      jobClass === "Arcane Mage" ||
      jobClass === "Grand Architect"
    ) {
      themeClass = "theme-scholar";
    } else if (
      jobClass === "SHADOW" ||
      jobClass === "Shadow Necromancer" ||
      jobClass === "Monarch"
    ) {
      themeClass = "theme-shadow";
    }

    setTheme(themeClass);
    document.documentElement.setAttribute("data-theme", themeClass);
    document.body.className = themeClass;
  }, []);

  const refreshSystem = useCallback(async () => {
    if (!playerId) return;
    await fetchPlayerState(playerId);
  }, [playerId, fetchPlayerState]);

  useEffect(() => {
    if (player.jobClass) {
      applyTheme(player.jobClass);
    }
  }, [player.jobClass, applyTheme]);

  useEffect(() => {
    if (playerId) {
      refreshSystem();
    }
  }, [playerId, refreshSystem]);

  // Construct backward-compatible statusWindow object mapping to the Zustand store state (wrapped in useMemo to prevent cascading re-renders)
  const statusWindow = useMemo(() => ({
    jobClass: player.jobClass,
    identity: {
      level: player.level,
      rank: player.rank,
      equippedTheme: theme,
      jobChangeStatus: player.jobChangeStatus,
    },
    progression: {
      currentXp: player.xp,
      maxXpForLevel: player.maxXp,
    },
    attributes: player.attributes,
    economy: {
      gold: player.gold,
      debt: player.debt,
    },
    systemState: {
      penaltyActive: player.penaltyActive,
      activeBuffs: player.activeModifiers,
    },
  }), [
    player.jobClass,
    player.level,
    player.rank,
    theme,
    player.jobChangeStatus,
    player.xp,
    player.maxXp,
    player.attributes,
    player.gold,
    player.debt,
    player.penaltyActive,
    player.activeModifiers,
  ]);

  return (
    <SystemContext.Provider
      value={{
        playerId: player.playerId || playerId,
        statusWindow,
        jobClass: player.jobClass,
        theme,
        loading: player.loading,
        error: player.error,
        refreshSystem,
        applyTheme,
      }}
    >
      {children}
    </SystemContext.Provider>
  );
};
