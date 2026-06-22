import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { Eye, Skull, TrendingUp, Calendar, RefreshCw } from 'lucide-react';
import { ScreenFrame } from '../components/layout/ScreenFrame';
import { AnalyticsAPI } from '../api/api';
import {
  ResponsiveContainer,
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  Legend
} from 'recharts';

interface ObserverScreenProps {
  playerId: string | null;
  onBack: () => void;
}

interface HeatmapEntry {
  date: string;
  status: 'ALL_CLEARED' | 'PARTIAL' | 'FAILED' | 'NO_QUESTS';
}

interface StatDataPoint {
  date: string;
  STR: number;
  INT: number;
  VIT: number;
  AGI: number;
  SEN: number;
}

interface GraveyardEntry {
  id: number;
  text: string;
  accepted: boolean;
  timestamp: string;
  feedback: string;
}

export function ObserverScreen({ playerId, onBack }: ObserverScreenProps) {
  const [heatmap, setHeatmap] = useState<HeatmapEntry[]>([]);
  const [stats, setStats] = useState<StatDataPoint[]>([]);
  const [graveyard, setGraveyard] = useState<GraveyardEntry[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [refreshing, setRefreshing] = useState<boolean>(false);

  const loadData = async () => {
    if (!playerId) return;
    try {
      const [heatmapData, statsData, graveyardData] = await Promise.all([
        AnalyticsAPI.fetchHeatmap(playerId),
        AnalyticsAPI.fetchStatGrowth(playerId),
        AnalyticsAPI.fetchGraveyard(playerId)
      ]);
      setHeatmap(heatmapData || []);
      setStats(statsData || []);
      setGraveyard(graveyardData || []);
    } catch (error) {
      console.error('Failed to fetch analytics data:', error);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [playerId]);

  const handleRefresh = () => {
    setRefreshing(true);
    loadData();
  };

  // Chunk 365 days into 53 weeks (7 days each)
  const weeks: HeatmapEntry[][] = [];
  if (heatmap.length > 0) {
    for (let i = 0; i < heatmap.length; i += 7) {
      weeks.push(heatmap.slice(i, i + 7));
    }
  }

  // Format dates for chart labels
  const formattedStats = stats.map(d => ({
    ...d,
    formattedDate: new Date(d.date).toLocaleDateString([], { month: 'short', day: 'numeric' })
  }));

  // Custom Tooltip for Recharts
  const CustomTooltip = ({ active, payload, label }: any) => {
    if (active && payload && payload.length) {
      return (
        <div className="glass-panel border border-solo-cyan bg-black/95 p-3 font-mono text-xs text-white">
          <p className="border-b border-solo-cyan/30 pb-1 mb-2 font-bold uppercase tracking-wider text-solo-cyan">
            {label}
          </p>
          {payload.map((pld: any) => (
            <div key={pld.name} className="flex justify-between gap-6 py-0.5" style={{ color: pld.color }}>
              <span className="uppercase font-bold tracking-widest">{pld.name}:</span>
              <span className="font-bold">{pld.value}</span>
            </div>
          ))}
        </div>
      );
    }
    return null;
  };

  // Custom Legend Renderer
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

  return (
    <ScreenFrame onBack={onBack} className="bg-[#020617] min-h-screen select-none font-mono">
      {/* Top Header */}
      <div className="flex justify-between items-center mb-6 pl-12">
        <div>
          <h1 className="text-2xl font-bold tracking-[0.2em] text-white uppercase flex items-center gap-2">
            <Eye className="text-solo-cyan animate-pulse" size={24} /> THE SYSTEM OBSERVER
          </h1>
          <p className="text-[10px] text-gray-500 uppercase tracking-widest">
            Authoritative Player Logs & Growth Analytics
          </p>
        </div>
        <button
          onClick={handleRefresh}
          disabled={refreshing}
          className="p-2 border border-solo-cyan/30 text-solo-cyan hover:border-solo-cyan hover:bg-solo-cyan/10 transition-colors uppercase text-xs flex items-center gap-1.5"
        >
          <RefreshCw size={14} className={refreshing ? 'animate-spin' : ''} />
          {refreshing ? 'Syncing...' : 'Sync logs'}
        </button>
      </div>

      {loading ? (
        <div className="flex flex-col items-center justify-center h-96 text-solo-cyan animate-pulse">
          <Eye size={48} className="animate-spin mb-4" />
          <span className="text-sm tracking-widest">INITIATING LINK WITH DATABASE...</span>
        </div>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          
          {/* Main Visualizations (Left and Middle columns) */}
          <div className="lg:col-span-2 space-y-6">
            
            {/* Daily Heatmap Grid */}
            <motion.div
              initial={{ opacity: 0, y: 15 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.4 }}
              className="glass-panel border border-slate-800 bg-[#060c18]/80 p-5 shadow-lg relative"
            >
              <div className="absolute top-0 left-0 w-24 h-[1px] bg-solo-cyan" />
              <div className="absolute top-0 left-0 w-[1px] h-24 bg-solo-cyan" />
              
              <h3 className="text-xs text-solo-cyan font-bold tracking-[0.25em] mb-4 uppercase flex items-center gap-2">
                <Calendar size={14} /> Daily quest completion calendar (365 Days)
              </h3>

              <div className="flex gap-2 p-2 bg-[#030712]/50 border border-slate-900 overflow-x-auto scrollbar-thin">
                {/* Y-Axis Labels */}
                <div className="flex flex-col justify-between py-1 text-[8px] font-bold text-gray-600 select-none h-[98px] shrink-0 font-mono">
                  <span>Mon</span>
                  <span>Wed</span>
                  <span>Fri</span>
                </div>

                {/* Heatmap Grid columns */}
                <div className="flex gap-1">
                  {weeks.map((week, wIdx) => (
                    <div key={wIdx} className="flex flex-col gap-1 shrink-0">
                      {week.map((day, dIdx) => {
                        let colorClass = 'bg-slate-950/80 border border-slate-900/60';
                        let glowClass = '';
                        if (day.status === 'ALL_CLEARED') {
                          colorClass = 'bg-solo-cyan border border-solo-cyan';
                          glowClass = 'shadow-[0_0_5px_rgba(0,229,255,0.7)]';
                        } else if (day.status === 'PARTIAL') {
                          colorClass = 'bg-[#007f99] border border-[#009fb3]';
                        } else if (day.status === 'FAILED') {
                          colorClass = 'bg-solo-red border border-solo-red';
                          glowClass = 'shadow-[0_0_5px_rgba(255,0,60,0.7)]';
                        }
                        return (
                          <div
                            key={dIdx}
                            className={`w-2.5 h-2.5 transition-all duration-300 hover:scale-125 ${colorClass} ${glowClass}`}
                            title={`${day.date}: ${day.status.replace('_', ' ')}`}
                          />
                        );
                      })}
                    </div>
                  ))}
                </div>
              </div>

              {/* Legend */}
              <div className="flex justify-end gap-4 mt-3 text-[9px] text-gray-500 uppercase tracking-wider font-bold">
                <span className="flex items-center gap-1">
                  <span className="w-2 h-2 bg-slate-950/80 border border-slate-900" /> Empty
                </span>
                <span className="flex items-center gap-1">
                  <span className="w-2 h-2 bg-[#007f99] border border-[#009fb3]" /> Partial
                </span>
                <span className="flex items-center gap-1">
                  <span className="w-2 h-2 bg-solo-cyan shadow-[0_0_5px_rgba(0,229,255,0.7)]" /> Clear
                </span>
                <span className="flex items-center gap-1">
                  <span className="w-2 h-2 bg-solo-red shadow-[0_0_5px_rgba(255,0,60,0.7)]" /> Penalty
                </span>
              </div>
            </motion.div>

            {/* Stat Growth Trajectory */}
            <motion.div
              initial={{ opacity: 0, y: 15 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.5 }}
              className="glass-panel border border-slate-800 bg-[#060c18]/80 p-5 shadow-lg relative"
            >
              <div className="absolute top-0 left-0 w-24 h-[1px] bg-solo-cyan" />
              <div className="absolute top-0 left-0 w-[1px] h-24 bg-solo-cyan" />

              <h3 className="text-xs text-solo-cyan font-bold tracking-[0.25em] mb-4 uppercase flex items-center gap-2">
                <TrendingUp size={14} /> RPG Attribute Trajectory (30-Day Growths)
              </h3>

              {formattedStats.length === 0 ? (
                <div className="h-64 flex flex-col items-center justify-center border border-dashed border-slate-800 bg-slate-950/40 text-gray-500 font-mono text-xs uppercase tracking-widest">
                  <TrendingUp size={36} className="mb-2 text-slate-700 animate-pulse" />
                  NO progression data logged in current simulation frame
                </div>
              ) : (
                <div className="h-72 w-full pr-4">
                  <ResponsiveContainer width="100%" height="100%">
                    <LineChart data={formattedStats} margin={{ top: 10, right: 10, left: -25, bottom: 0 }}>
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
          </div>

          {/* Confession Graveyard (Right column) */}
          <div className="lg:col-span-1">
            <motion.div
              initial={{ opacity: 0, x: 15 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ duration: 0.4 }}
              className="glass-panel border border-red-950/80 bg-[#090507]/90 p-5 shadow-lg relative h-full flex flex-col justify-between"
              style={{ minHeight: '445px' }}
            >
              <div className="absolute top-0 left-0 w-24 h-[1px] bg-solo-red" />
              <div className="absolute top-0 left-0 w-[1px] h-24 bg-solo-red" />

              <div>
                <h3 className="text-xs text-solo-red font-bold tracking-[0.25em] mb-4 uppercase flex items-center gap-2">
                  <Skull size={14} /> CONFESSION GRAVEYARD (ARCHITECT'S JUDGMENTS)
                </h3>

                <div className="overflow-y-auto space-y-3 pr-1 scrollbar-thin scrollbar-thumb-red-950 max-h-[350px] font-mono">
                  {graveyard.length === 0 ? (
                    <div className="text-center py-12 text-[10px] text-gray-500 uppercase tracking-widest border border-dashed border-red-950/40 bg-black/40">
                      No confession records found.
                      <p className="mt-2 text-[8px] text-red-900">THE CONSCIENCE IS PURE</p>
                    </div>
                  ) : (
                    graveyard.map(entry => (
                      <div
                        key={entry.id}
                        className="text-[10px] border border-red-950 bg-black/60 p-3 rounded-none relative overflow-hidden"
                      >
                        {/* Diagnostics Title Bar */}
                        <div className="flex justify-between items-center text-gray-500 mb-2 pb-1 border-b border-red-950/40 uppercase">
                          <span>CONFESSION #{entry.id}</span>
                          <span className={entry.accepted ? 'text-solo-cyan' : 'text-solo-red'}>
                            {entry.accepted ? 'CLEANSED' : 'CONDEMNED'}
                          </span>
                        </div>
                        
                        {/* Player text */}
                        <div className="text-gray-300 italic mb-2 break-all bg-red-950/10 p-1.5 border border-red-950/20">
                          "{entry.text}"
                        </div>

                        {/* AI Brutal Diagnostic Feedback */}
                        <div className="text-red-400 pl-2 border-l border-solo-red/50">
                          <span className="font-bold text-solo-red block mb-0.5">[SYSTEM ARCHITECT]</span>
                          {entry.feedback || 'Confession judged. Insufficient conviction. Lockout penalty active.'}
                        </div>

                        {/* Timestamp */}
                        <div className="text-right text-[8px] text-gray-600 mt-2">
                          LOGGED: {new Date(entry.timestamp).toLocaleString()}
                        </div>
                      </div>
                    ))
                  )}
                </div>
              </div>

              {/* Bottom Decorative Element */}
              <div className="border-t border-red-950/50 pt-3 text-center text-[8px] text-red-900 uppercase tracking-widest font-black mt-4">
                "Suffer the penalty or evolve past your limitations."
              </div>
            </motion.div>
          </div>

        </div>
      )}
    </ScreenFrame>
  );
}
