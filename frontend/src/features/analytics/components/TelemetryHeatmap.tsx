import React, { useEffect, useState } from "react";
import { Shield, Info } from "lucide-react";
import { api } from "../../../api/api";

interface HeatmapCell {
  date: string;
  status: "ALL_CLEARED" | "PARTIAL_CLEARED" | "FAILED" | "STEALTH_PAUSED" | "EVENT_FROZEN" | "PENALTY_LOCKED" | "NO_QUESTS";
}

export const TelemetryHeatmap: React.FC<{ playerId: string }> = ({ playerId }) => {
  const [data, setData] = useState<HeatmapCell[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchHeatmapData = async () => {
      if (!playerId) return;
      setLoading(true);
      setError(null);
      try {
        const json = await api.get(`/analytics/heatmap?playerId=${playerId}`);
        setData(json || []);
      } catch (err: any) {
        console.error("Heatmap Fetch Error, fallback to mock data:", err);
        // Fallback placeholder data generation (365 days of activity)
        const mockCells: HeatmapCell[] = [];
        const states: HeatmapCell["status"][] = [
          "ALL_CLEARED",
          "PARTIAL_CLEARED",
          "FAILED",
          "STEALTH_PAUSED",
          "EVENT_FROZEN",
          "PENALTY_LOCKED",
          "NO_QUESTS",
        ];

        const now = new Date();
        for (let i = 365; i >= 0; i--) {
          const date = new Date(now.getTime() - i * 24 * 60 * 60 * 1000);
          const dateString = date.toISOString().split("T")[0];
          
          // Seed deterministic mock statuses
          let status: HeatmapCell["status"] = "NO_QUESTS";
          if (i % 7 === 0) {
            status = states[Math.floor((i / 7) % states.length)];
          } else if (i % 3 === 0) {
            status = "ALL_CLEARED";
          } else if (i % 5 === 0) {
            status = "PARTIAL_CLEARED";
          }
          
          mockCells.push({ date: dateString, status });
        }
        setData(mockCells);
      } finally {
        setLoading(false);
      }
    };

    fetchHeatmapData();
  }, [playerId]);

  const getColorClass = (status: HeatmapCell["status"]) => {
    switch (status) {
      case "ALL_CLEARED":
        return "bg-cyan-500 hover:bg-cyan-400 border border-cyan-400/50 shadow-glow-cyan";
      case "PARTIAL_CLEARED":
        return "bg-yellow-500 hover:bg-yellow-400 border border-yellow-400/50";
      case "FAILED":
        return "bg-red-500 hover:bg-red-400 border border-red-400/50 shadow-glow-red";
      case "STEALTH_PAUSED":
        return "bg-zinc-800/40 border border-zinc-700/50 hover:bg-zinc-700/50";
      case "EVENT_FROZEN":
        return "bg-blue-400 hover:bg-blue-300 border border-blue-300/50 shadow-glow-blue";
      case "PENALTY_LOCKED":
        return "bg-red-950/80 hover:bg-red-900 border border-red-900/50";
      default:
        return "bg-slate-950/80 border border-slate-900/60 hover:bg-slate-900/40";
    }
  };

  const getStatusLabel = (status: HeatmapCell["status"]) => {
    switch (status) {
      case "ALL_CLEARED": return "All Cleared";
      case "PARTIAL_CLEARED": return "Partial Cleared";
      case "FAILED": return "Failed";
      case "STEALTH_PAUSED": return "Stealth Runestone Pause";
      case "EVENT_FROZEN": return "Red Gate Reality Frozen";
      case "PENALTY_LOCKED": return "Penalty Lockout";
      default: return "No Active Quests";
    }
  };

  if (loading) {
    return (
      <div className="w-full h-48 bg-[#0a0a0f] border border-gray-900 flex items-center justify-center font-mono text-xs text-cyan-400">
        <span className="animate-pulse tracking-widest uppercase">SCANNING SYSTEM CHRONOLOGY...</span>
      </div>
    );
  }

  return (
    <div className="w-full bg-[#0a0a0f] border border-gray-900 p-6 flex flex-col font-sans text-white">
      <div className="flex justify-between items-center mb-6">
        <div className="flex items-center gap-2">
          <Shield className="text-cyan-400" size={18} />
          <h3 className="text-lg font-black tracking-widest uppercase font-display text-cyan-400">
            SYSTEM HEATMAP (365D)
          </h3>
        </div>
        <div className="flex items-center gap-1.5 text-xs text-gray-500 font-mono">
          <Info size={12} />
          <span>Excluded Intel Quests</span>
        </div>
      </div>

      {/* Grid Container */}
      <div className="w-full overflow-x-auto pb-4 scrollbar-thin scrollbar-thumb-gray-800 scrollbar-track-transparent">
        <div className="grid grid-flow-col grid-rows-7 gap-1.5 min-w-[760px] h-[100px]">
          {data.map((cell, idx) => (
            <div
              key={cell.date + idx}
              data-testid="heatmap-cell"
              className={`w-[10px] h-[10px] rounded-[1px] transition-all duration-300 cursor-pointer ${getColorClass(
                cell.status
              )}`}
              title={`${cell.date}: ${getStatusLabel(cell.status)}`}
            />
          ))}
        </div>
      </div>

      {/* Legend Block */}
      <div className="mt-6 pt-4 border-t border-zinc-900 flex flex-wrap gap-x-6 gap-y-2 justify-center font-mono text-[10px] uppercase tracking-wider text-gray-400">
        <div className="flex items-center gap-2">
          <div className="w-3 h-3 rounded-[1px] bg-cyan-500 border border-cyan-400" />
          <span>All Cleared</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-3 h-3 rounded-[1px] bg-yellow-500 border border-yellow-400" />
          <span>Partial</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-3 h-3 rounded-[1px] bg-red-500 border border-red-400" />
          <span>Failed</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-3 h-3 rounded-[1px] bg-slate-800/40 border border-slate-700/50" />
          <span>Stealth</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-3 h-3 rounded-[1px] bg-blue-400 border border-blue-300" />
          <span>Red Gate</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-3 h-3 rounded-[1px] bg-red-950/80 border border-red-900" />
          <span>Penalty</span>
        </div>
      </div>
    </div>
  );
};
export default TelemetryHeatmap;
