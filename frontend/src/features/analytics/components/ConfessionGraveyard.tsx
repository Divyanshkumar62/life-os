import React, { useEffect, useState, useCallback } from "react";
import { motion } from "framer-motion";
import { Skull } from "lucide-react";
import { AnalyticsAPI } from "../api/AnalyticsAPI";
import type { ConfessionEntry } from "../api/AnalyticsAPI";

export const ConfessionGraveyard: React.FC<{ playerId: string }> = ({ playerId }) => {
  const [graveyard, setGraveyard] = useState<ConfessionEntry[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const fetchConfessions = useCallback(async () => {
    if (!playerId) return;
    setLoading(true);
    setError(null);
    try {
      const response = await AnalyticsAPI.fetchConfessions(playerId);
      setGraveyard(response || []);
    } catch (err: any) {
      console.error("Confession Graveyard Fetch Error:", err);
      setError(err.message || "Failed to load confession records");
      setGraveyard([]);
    } finally {
      setLoading(false);
    }
  }, [playerId]);

  useEffect(() => {
    fetchConfessions();
  }, [fetchConfessions]);

  if (loading) {
    return (
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        className="glass-panel border border-red-950/80 bg-[#090507]/90 p-5 shadow-lg relative"
        style={{ minHeight: '445px' }}
      >
        <div className="flex flex-col items-center justify-center h-full text-solo-red animate-pulse font-mono text-xs tracking-widest uppercase">
          <Skull size={24} className="mb-4" />
          <span>EXHUMING CONFESSION LOGS...</span>
        </div>
      </motion.div>
    );
  }

  if (error) {
    return (
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        className="glass-panel border border-red-950/80 bg-[#090507]/90 p-5 shadow-lg relative"
        style={{ minHeight: '445px' }}
      >
        <div className="flex flex-col items-center justify-center h-full text-red-400 font-mono text-xs">
          <Skull size={24} className="mb-2" />
          <span className="tracking-widest uppercase">CONFESSION VAULT CORRUPTED</span>
          <p className="text-gray-500 mt-1">{error}</p>
          <button
            onClick={fetchConfessions}
            className="mt-3 px-3 py-1 border border-red-900 text-red-400 hover:bg-red-950/30 transition-colors uppercase tracking-widest text-[10px]"
          >
            Retry
          </button>
        </div>
      </motion.div>
    );
  }

  return (
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

                {/* Entry type badge */}
                <div className="flex gap-1 mb-2">
                  <span className="px-1 py-0.5 bg-red-950/30 border border-red-950/50 text-[8px] uppercase tracking-widest text-gray-400">
                    {entry.entryType || "CONFESSION"}
                  </span>
                  {entry.strikeCount > 0 && (
                    <span className="px-1 py-0.5 bg-yellow-950/30 border border-yellow-950/50 text-[8px] uppercase tracking-widest text-yellow-500">
                      STRIKE {entry.strikeCount}/3
                    </span>
                  )}
                  {entry.lockoutDurationHours != null && entry.lockoutDurationHours > 0 && (
                    <span className="px-1 py-0.5 bg-red-950/30 border border-red-950/50 text-[8px] uppercase tracking-widest text-red-400">
                      LOCKOUT {entry.lockoutDurationHours}h
                    </span>
                  )}
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
  );
};
export default ConfessionGraveyard;