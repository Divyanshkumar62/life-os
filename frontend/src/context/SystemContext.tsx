import React, {
  createContext,
  useContext,
  useState,
  useEffect,
  useCallback,
} from "react";
import { DashboardAPI } from "../api/api";

interface SystemStateContextType {
  playerId: string | null;
  statusWindow: any | null; // Will type properly based on backend schema
  jobClass: string | null;
  theme: string;
  loading: boolean;
  error: string | null;
  refreshSystem: () => Promise<void>;
  applyTheme: (jobClass: string) => void;
}

const SystemContext = createContext<SystemStateContextType | undefined>(
  undefined,
);

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
}> = ({ children, playerId }) => {
  const [statusWindow, setStatusWindow] = useState<any | null>(null);
  const [jobClass, setJobClass] = useState<string | null>(null);
  const [theme, setTheme] = useState<string>("theme-default");
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const applyTheme = useCallback((jobClass: string) => {
    let themeClass = "theme-default";

    if (jobClass === "Silver Knight" || jobClass === "Berserker") {
      themeClass = "theme-vanguard";
    } else if (jobClass === "Arcane Mage" || jobClass === "Grand Architect") {
      themeClass = "theme-scholar";
    } else if (jobClass === "Shadow Necromancer" || jobClass === "Monarch") {
      themeClass = "theme-shadow";
    }

    setTheme(themeClass);
    document.documentElement.setAttribute("data-theme", themeClass);
  }, []);

  const refreshSystem = useCallback(async () => {
    if (!playerId) return;
    setLoading(true);
    setError(null);
    try {
      const data = await DashboardAPI.getStatusWindow(playerId);
      setStatusWindow(data);

      // Update job class if present
      if (data?.jobClass) {
        setJobClass(data.jobClass);
        applyTheme(data.jobClass);
      }
    } catch (err: any) {
      console.error("Failed to sync with System authoritative state:", err);
      setError(err.message || "System Sync Failure");
    } finally {
      setLoading(false);
    }
  }, [playerId, applyTheme]);

  // Initial sync
  useEffect(() => {
    refreshSystem();
  }, [refreshSystem]);

  return (
    <SystemContext.Provider
      value={{
        playerId,
        statusWindow,
        jobClass,
        theme,
        loading,
        error,
        refreshSystem,
        applyTheme,
      }}
    >
      {children}
    </SystemContext.Provider>
  );
};
