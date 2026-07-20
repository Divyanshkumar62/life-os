import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { motion } from "framer-motion";
import { AlertOctagon, Shield, Coins, Activity, ArrowRight } from "lucide-react";
import { usePlayerStore } from "../../../stores/usePlayerStore";
import { DungeonBreakAPI } from "../../../api/api";

export const DungeonBreakScreen: React.FC = () => {
  const navigate = useNavigate();
  const { playerId, activeDungeonBreakEvent, setDungeonBreakActive, setActiveDungeonBreakEvent, fetchPlayerState } = usePlayerStore();
  const [loading, setLoading] = useState(false);

  if (!activeDungeonBreakEvent) {
    return (
      <div className="min-h-screen bg-black text-white flex items-center justify-center font-mono">
        <div className="text-center">
          <AlertOctagon size={48} className="mx-auto text-solo-red animate-pulse mb-4" />
          <p className="text-gray-400">Loading dungeon break telemetry...</p>
        </div>
      </div>
    );
  }

  const {
    projectId,
    projectTitle,
    dungeonRank,
    goldBefore,
    goldPenaltyAmount,
    goldAfter,
    vitMitigationPercent,
    debuffsApplied,
    debuffDurationHours,
    doublePenaltyResolution,
  } = activeDungeonBreakEvent;

  const handleAcknowledge = async () => {
    if (loading) return;
    setLoading(true);
    try {
      await DungeonBreakAPI.acknowledge(projectId, playerId);
      setDungeonBreakActive(false);
      setActiveDungeonBreakEvent(null);
      await fetchPlayerState(playerId);
      navigate("/penalty");
    } catch (err) {
      console.error("Failed to acknowledge dungeon break", err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-black text-white flex flex-col justify-center items-center p-6 font-mono relative overflow-hidden">
      {/* Red alarm glow overlay */}
      <div className="absolute inset-0 bg-red-950/20 pointer-events-none animate-pulse" />

      {/* Cyber Grid background */}
      <div className="absolute inset-0 bg-[linear-gradient(rgba(18,16,16,0)_50%,rgba(0,0,0,0.25)_50%),linear-gradient(90deg,rgba(255,0,0,0.06),rgba(0,255,0,0),rgba(0,0,255,0.06))] bg-[size:100%_4px,3%_100%] pointer-events-none" />

      <motion.div
        className="w-full max-w-2xl bg-black border-2 border-red-600/80 shadow-[0_0_30px_rgba(220,38,38,0.3)] p-8 relative z-10"
        initial={{ scale: 0.9, opacity: 0 }}
        animate={{ scale: 1, opacity: 1 }}
        transition={{ duration: 0.5 }}
      >
        {/* Warning Header */}
        <div className="border-b border-red-600/50 pb-6 mb-6 text-center">
          <AlertOctagon className="mx-auto text-red-500 animate-bounce mb-3" size={48} />
          <h1 className="text-3xl font-extrabold tracking-widest text-red-500 uppercase animate-pulse">
            Dungeon Break Active
          </h1>
          <p className="text-gray-400 text-xs mt-2 uppercase tracking-wider">
            Telecommunication Protocol &mdash; Gate Stability Broken
          </p>
        </div>

        {/* Telemetry Box */}
        <div className="space-y-6">
          <div className="bg-red-950/10 border border-red-900/40 p-4 rounded-none">
            <span className="text-red-400 text-xs block uppercase tracking-widest">Broken Gate</span>
            <div className="flex justify-between items-center mt-1">
              <span className="text-lg font-bold text-white uppercase">{projectTitle}</span>
              <span className="px-2.5 py-0.5 bg-red-950/80 border border-red-500 text-red-500 font-extrabold text-sm rounded-none">
                RANK {dungeonRank}
              </span>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {/* Gold Penalty */}
            <div className="bg-black border border-gray-800 p-4 relative overflow-hidden">
              <div className="flex items-center gap-2 text-yellow-500 mb-2">
                <Coins size={16} />
                <span className="text-xs uppercase tracking-wider font-bold">Economy Impact</span>
              </div>
              <div className="flex items-baseline gap-2 mt-1">
                <span className="text-gray-500 line-through text-sm">{goldBefore}G</span>
                <ArrowRight size={12} className="text-gray-400" />
                <span className="text-xl font-bold text-white">{goldAfter}G</span>
              </div>
              <p className="text-[10px] text-red-400 mt-2">
                Deduction: -{goldPenaltyAmount} Gold
              </p>
              <div className="flex items-center gap-1 text-[10px] text-green-400 mt-1">
                <Shield size={10} />
                <span>VIT Mitigation: {vitMitigationPercent.toFixed(1)}% applied</span>
              </div>
            </div>

            {/* Stat Debuffs */}
            <div className="bg-black border border-gray-800 p-4">
              <div className="flex items-center gap-2 text-blue-400 mb-2">
                <Activity size={16} />
                <span className="text-xs uppercase tracking-wider font-bold">Stat Debuffs</span>
              </div>
              <div className="space-y-1 mt-1.5">
                {debuffsApplied && debuffsApplied.length > 0 ? (
                  debuffsApplied.map((debuff, idx) => (
                    <div key={idx} className="text-[10px] text-red-400 flex items-center gap-1.5 uppercase">
                      <span className="w-1.5 h-1.5 bg-red-500 rounded-none inline-block" />
                      <span>{debuff.replace(/_/g, " ")} ({debuffDurationHours}H)</span>
                    </div>
                  ))
                ) : (
                  <span className="text-xs text-gray-500">None</span>
                )}
              </div>
            </div>
          </div>

          {/* Double Penalty Status */}
          {doublePenaltyResolution && (
            <div className="bg-amber-950/20 border border-amber-800/40 p-4 flex items-start gap-3">
              <Shield className="text-amber-500 mt-0.5 shrink-0" size={16} />
              <div>
                <span className="text-amber-500 font-bold text-xs uppercase tracking-wider block">
                  Double Penalty Resolution Active
                </span>
                <p className="text-[10px] text-gray-400 mt-1 uppercase leading-relaxed">
                  System detected player was already inside the penalty zone. Remaining gold drained to debt. Active survival task has been preserved.
                </p>
              </div>
            </div>
          )}

          {/* Warning Message */}
          <div className="text-center text-xs text-gray-500 border-t border-gray-900 pt-6">
            The System has locked all standard floor interfaces. To restore balance, you must acknowledge this break and complete the active survival task immediately.
          </div>

          {/* Action button */}
          <button
            data-testid="ack-dungeon-break"
            onClick={handleAcknowledge}
            disabled={loading}
            className="w-full py-4 bg-red-600 hover:bg-red-700 text-white font-bold uppercase tracking-widest text-sm shadow-[0_0_15px_rgba(220,38,38,0.4)] hover:shadow-[0_0_25px_rgba(220,38,38,0.6)] border border-red-500 hover:border-red-400 transition-all duration-300 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {loading ? "PROCESSING..." : "Acknowledge Break & Enter Penalty Zone"}
          </button>
        </div>
      </motion.div>
    </div>
  );
};
