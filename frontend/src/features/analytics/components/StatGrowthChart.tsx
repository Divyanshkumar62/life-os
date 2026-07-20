import React, { useEffect, useState, useCallback } from "react";
import { motion } from "framer-motion";
import { TrendingUp } from "lucide-react";
import {
  ResponsiveContainer,
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  Legend,
  ReferenceDot,
} from "recharts";
import { AnalyticsAPI } from "../api/AnalyticsAPI";
import type { StatDataPoint } from "../api/AnalyticsAPI";

const CustomTooltip = ({ active, payload, label }: any) => {
  if (active && payload && payload.length) {
    const milestoneData = payload[0]?.payload;
    return (
      <div className="glass-panel border border-solo-cyan bg-black/95 p-3 font-mono text-xs text-white min-w-[180px]">
        <p className="border-b border-solo-cyan/30 pb-1 mb-2 font-bold uppercase tracking-wider text-solo-cyan">
          {label}
        </p>
        {milestoneData?.isMilestone && milestoneData?.milestoneLabel && (
          <p className="text-yellow-400 font-bold text-[10px] uppercase tracking-widest mb-1 border-b border-yellow-400/30 pb-1">
            ⬟ {milestoneData.milestoneLabel}
          </p>
        )}
        {payload.map((pld: any) => (
          <div key={pld.name} className="flex justify-between gap-6 py-0.5" style={{ color: pld.color }}>
            <span className="uppercase font-bold tracking-widest">{pld.name}:</span>
            <span className="font-bold">{pld.value}</span>
          </div>
        ))}
        {milestoneData?.level && (
          <p className="text-gray-500 text-[9px] mt-1 border-t border-gray-800 pt-1">
            Lv.{milestoneData.level} · {milestoneData.rank}-Rank
          </p>
        )}
      </div>
    );
  }
  return null;
};

const renderLegend = (props: any) => {
  const { payload } = props;
  return (
    <div className="flex flex-wrap justify-center gap-4 mt-2 font-mono text-xs">
      {payload.map((entry: any, index: number) => (
        <span key={`item-${index}`} className="flex items-center gap-1.5 cursor-pointer hover:brightness-125 transition-all" style={{ color: entry.color }}>
          <span className="w-2.5 h-2.5 inline-block rounded-none" style={{ backgroundColor: entry.color }} />
          <span className="uppercase tracking-widest font-black">{entry.value}</span>
        </span>
      ))}
    </div>
  );
};

export const StatGrowthChart: React.FC<{ playerId: string }> = ({ playerId }) => {
  const [stats, setStats] = useState<StatDataPoint[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const fetchStats = useCallback(async () => {
    if (!playerId) return;
    setLoading(true);
    setError(null);
    try {
      const response = await AnalyticsAPI.fetchStatGrowth(playerId);
      setStats(response || []);
    } catch (err: any) {
      console.error("Stat Growth Fetch Error:", err);
      setError(err.message || "Failed to load stat growth data");
      setStats([]);
    } finally {
      setLoading(false);
    }
  }, [playerId]);

  useEffect(() => {
    fetchStats();
  }, [fetchStats]);

  const formattedStats = stats.map(d => ({
    ...d,
    formattedDate: new Date(d.date).toLocaleDateString([], { month: 'short', day: 'numeric' }),
  }));

  const milestonePoints = formattedStats.filter(d => d.isMilestone);

  if (loading) {
    return (
      <div className="w-full h-64 bg-[#0a0a0f] border border-gray-900 flex items-center justify-center font-mono text-xs text-cyan-400">
        <span className="animate-pulse tracking-widest uppercase">LOADING ATTRIBUTE TRAJECTORY...</span>
      </div>
    );
  }

  if (error) {
    return (
      <div className="w-full bg-[#0a0a0f] border border-red-900 p-6 flex flex-col items-center justify-center font-mono text-xs text-red-400">
        <TrendingUp size={24} className="mb-2" />
        <span className="tracking-widest uppercase">TRAJECTORY CORRUPTED</span>
        <p className="text-gray-500 mt-1">{error}</p>
        <button
          onClick={fetchStats}
          className="mt-3 px-3 py-1 border border-red-900 text-red-400 hover:bg-red-950/30 transition-colors uppercase tracking-widest text-[10px]"
        >
          Retry
        </button>
      </div>
    );
  }

  return (
    <motion.div
      initial={{ opacity: 0, y: 15 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5 }}
      className="glass-panel border border-slate-800 bg-[#060c18]/80 p-5 shadow-lg relative"
    >
      <div className="absolute top-0 left-0 w-24 h-[1px] bg-solo-cyan" />
      <div className="absolute top-0 left-0 w-[1px] h-24 bg-solo-cyan" />

      <h3 className="text-xs text-solo-cyan font-bold tracking-[0.25em] mb-4 uppercase flex items-center gap-2">
        <TrendingUp size={14} /> RPG Attribute Trajectory (Lifetime Growth)
      </h3>

      {formattedStats.length === 0 ? (
        <div className="h-64 flex flex-col items-center justify-center border border-dashed border-slate-800 bg-slate-950/40 text-gray-500 font-mono text-xs uppercase tracking-widest">
          <TrendingUp size={36} className="mb-2 text-slate-700 animate-pulse" />
          NO progression data logged in current simulation frame
        </div>
      ) : (
        <div className="h-72 w-full pr-4">
          <ResponsiveContainer width="100%" height="100%">
            <LineChart data={formattedStats} margin={{ top: 20, right: 10, left: -25, bottom: 0 }}>
              <XAxis
                dataKey="formattedDate"
                stroke="#475569"
                fontSize={8}
                tickLine={false}
                fontFamily="monospace"
                dy={8}
              />
              <YAxis
                stroke="#475569"
                fontSize={8}
                tickLine={false}
                fontFamily="monospace"
                domain={['auto', 'auto']}
              />
              <Tooltip content={<CustomTooltip />} />
              <Legend content={renderLegend} />

              {/* Milestone reference dots */}
              {milestonePoints.map((point, idx) => (
                <ReferenceDot
                  key={`milestone-${idx}`}
                  x={point.formattedDate}
                  y={point.STR}
                  r={6}
                  fill="#ffd700"
                  stroke="#000"
                  strokeWidth={1}
                />
              ))}

              <Line
                type="monotone"
                dataKey="STR"
                stroke="#00e5ff"
                strokeWidth={2}
                dot={false}
                activeDot={{ r: 4, strokeWidth: 0 }}
                name="Strength"
              />
              <Line
                type="monotone"
                dataKey="INT"
                stroke="#ffd700"
                strokeWidth={2}
                dot={false}
                activeDot={{ r: 4, strokeWidth: 0 }}
                name="Intelligence"
              />
              <Line
                type="monotone"
                dataKey="VIT"
                stroke="#00ff87"
                strokeWidth={2}
                dot={false}
                activeDot={{ r: 4, strokeWidth: 0 }}
                name="Vitality"
              />
              <Line
                type="monotone"
                dataKey="AGI"
                stroke="#ff007f"
                strokeWidth={2}
                dot={false}
                activeDot={{ r: 4, strokeWidth: 0 }}
                name="Agility"
              />
              <Line
                type="monotone"
                dataKey="SEN"
                stroke="#ff003c"
                strokeWidth={2}
                dot={false}
                activeDot={{ r: 4, strokeWidth: 0 }}
                name="Perception"
              />
            </LineChart>
          </ResponsiveContainer>
        </div>
      )}
    </motion.div>
  );
};
export default StatGrowthChart;