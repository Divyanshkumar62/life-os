import React, { useEffect, useState } from "react";
import { Navigate } from "react-router-dom";
import { motion, AnimatePresence } from "framer-motion";
import { useRedGateContext } from "../../../context/RedGateContext";
import { useSystemAudio } from "../../../hooks/useSystemAudio";
import { ShieldAlert, Hourglass, Trophy, Lock } from "lucide-react";

export const RedGateOverrideScreen: React.FC = () => {
  const { redGate, completeRedGate, failRedGate } = useRedGateContext();
  const { playRedGateAlarm } = useSystemAudio();
  const [showConfirm, setShowConfirm] = useState<'complete' | 'fail' | null>(null);

  if (!redGate.isActive) {
    return <Navigate replace to="/dashboard" />;
  }

  useEffect(() => {
    playRedGateAlarm();
  }, [playRedGateAlarm]);

  const formatTime = (seconds: number): string => {
    const hrs = Math.floor(seconds / 3600);
    const mins = Math.floor((seconds % 3600) / 60);
    const secs = Math.floor(seconds % 60);
    return `${hrs.toString().padStart(2, '0')}:${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  };

  const remainingHours = redGate.remainingSeconds !== null ? redGate.remainingSeconds / 3600 : 0;
  const shakeIntensity = Math.max(0, Math.min(10, (1 - remainingHours / 12) * 12));
  const isUrgent = remainingHours < 2;

  const handleAction = async (action: 'complete' | 'fail') => {
    try {
      if (action === 'complete') {
        await completeRedGate();
      } else {
        await failRedGate();
      }
      setShowConfirm(null);
    } catch (err) {
      console.error(`Failed to execute Red Gate action: ${action}`, err);
    }
  };

  return (
    <div className="fixed inset-0 z-[9999] overflow-hidden bg-[#040406] flex items-center justify-center font-sans select-none">
      <div className="absolute inset-0 opacity-10 bg-[radial-gradient(circle_at_center,rgba(239,68,68,0.2)_0%,rgba(0,0,0,0)_100%)] pointer-events-none" />
      <div className="absolute inset-0 bg-cover bg-center mix-blend-color-dodge opacity-20 pointer-events-none" />

      <motion.div
        animate={{
          x: isUrgent
            ? [-shakeIntensity, shakeIntensity, -shakeIntensity, shakeIntensity, -shakeIntensity/2, shakeIntensity/2, 0]
            : [-4, 4, -3, 3, -1, 1, 0],
          y: isUrgent
            ? [-shakeIntensity/2, shakeIntensity/2, -shakeIntensity/2, shakeIntensity/2, -shakeIntensity/4, shakeIntensity/4, 0]
            : [-2, 2, -1, 1, 0],
        }}
        transition={{
          duration: isUrgent ? 0.3 : 0.8,
          repeat: Infinity,
          repeatType: "mirror",
          repeatDelay: isUrgent ? 0.2 : 1.5,
        }}
        className="relative max-w-2xl w-full mx-4 px-6 py-10 bg-black/90 border-2 border-red-600/80 shadow-[0_0_50px_rgba(239,68,68,0.3)] backdrop-blur-md rounded-none text-white flex flex-col items-center"
      >
        <div className="absolute top-0 left-0 w-8 h-8 border-t-2 border-l-2 border-red-500" />
        <div className="absolute top-0 right-0 w-8 h-8 border-t-2 border-r-2 border-red-500" />
        <div className="absolute bottom-0 left-0 w-8 h-8 border-b-2 border-l-2 border-red-500" />
        <div className="absolute bottom-0 right-0 w-8 h-8 border-b-2 border-r-2 border-red-500" />

        <div className="flex items-center gap-2 px-4 py-1.5 bg-red-950/60 border border-red-500/50 rounded-none mb-6 animate-pulse">
          <ShieldAlert className="text-red-500" size={18} />
          <span className="font-mono text-xs tracking-[0.25em] text-red-400 uppercase font-semibold">
            WARNING: Sealed Reality Override
          </span>
        </div>

        <motion.h1
          animate={{
            textShadow: [
              "0 0 8px rgba(239,68,68,0.8)",
              "0 0 25px rgba(239,68,68,0.9)",
              "0 0 8px rgba(239,68,68,0.8)",
            ],
          }}
          transition={{ repeat: Infinity, duration: isUrgent ? 0.5 : 1.5 }}
          className="text-5xl md:text-6xl font-black text-red-500 tracking-[0.2em] text-center mb-1 font-display"
        >
          RED GATE
        </motion.h1>
        <p className="text-gray-400 font-mono text-xs tracking-[0.4em] uppercase mb-8">
          Reality Interference Detected
        </p>

        <div className="w-full bg-[#0a0a0f] border border-red-900/50 p-6 mb-8 text-center relative overflow-hidden">
          <div className="absolute inset-0 bg-red-950/5 opacity-40 pointer-events-none" />
          <span className="text-red-400/80 font-mono text-xs tracking-widest uppercase flex items-center justify-center gap-1.5 mb-2">
            <Hourglass size={12} className={isUrgent ? "animate-spin" : ""} />
            TEMPORAL EXILE COOLDOWN
          </span>

          <motion.div
            animate={isUrgent ? { scale: [1, 1.03, 1] } : { scale: [1, 1.01, 1] }}
            transition={{ repeat: Infinity, duration: isUrgent ? 0.4 : 1.5 }}
            className={`text-5xl md:text-6xl font-mono font-black tracking-wider filter drop-shadow-[0_0_10px_rgba(239,68,68,0.4)] ${
              isUrgent ? "text-red-400 animate-pulse" : "text-red-500"
            }`}
          >
            {redGate.remainingSeconds !== null ? formatTime(redGate.remainingSeconds) : "00:00:00"}
          </motion.div>

          {isUrgent && (
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: [0, 1, 0] }}
              transition={{ repeat: Infinity, duration: 0.8 }}
              className="mt-2 text-[10px] text-red-400 font-bold uppercase tracking-widest"
            >
              ⚠ CRITICAL: Time nearly expired ⚠
            </motion.div>
          )}
        </div>

        {redGate.quest ? (
          <div className="w-full bg-red-950/10 border border-red-600/30 p-5 mb-8 text-left">
            <div className="flex justify-between items-start mb-3 border-b border-red-900/30 pb-2">
              <span className="text-red-400 font-mono text-[10px] tracking-widest uppercase">
                Active Directive
              </span>
              <span className="px-2 py-0.5 bg-red-900/40 border border-red-700/60 text-red-200 font-mono text-[9px] uppercase tracking-wider">
                Rank {redGate.quest.difficulty || "S"}
              </span>
            </div>
            <h4 className="text-lg font-bold text-red-200 mb-1 tracking-wide font-display">
              {redGate.quest.title}
            </h4>
            <p className="text-gray-400 text-xs leading-relaxed font-mono">
              {redGate.quest.description}
            </p>
          </div>
        ) : (
          <div className="w-full bg-red-950/10 border border-red-600/30 p-5 mb-8 text-center">
            <h4 className="text-base font-bold text-red-300 mb-1 font-display">
              Loading Sealed Gate Mandate...
            </h4>
            <p className="text-gray-400 text-xs font-mono">
              Synchronizing dimensional metrics with the Architect engine.
            </p>
          </div>
        )}

        <div className="w-full bg-[#0a0a0f] border-2 border-red-900/60 p-4 mb-8 text-xs text-gray-400 font-mono leading-relaxed flex items-start gap-3">
          <Lock className="text-red-500 shrink-0 mt-0.5" size={14} />
          <div>
            <span className="text-red-400 font-bold">SYSTEM LOCKOUT ACTIVE: </span>
            Time flow has frozen in the normal dimension. Standard dashboards, projects, shops, inventory, and daily quests are completely offline. You must fulfill the directive or perish.
          </div>
        </div>

        <div className="w-full flex flex-col md:flex-row gap-4">
          <AnimatePresence mode="wait">
            {showConfirm === null ? (
              <>
                <button
                  onClick={() => setShowConfirm('complete')}
                  className="flex-1 py-3.5 bg-green-950/30 hover:bg-green-900/50 border border-green-500 text-green-300 hover:text-green-100 font-mono text-sm tracking-widest uppercase transition-all duration-300 flex items-center justify-center gap-2 hover:shadow-[0_0_15px_rgba(34,197,94,0.2)]"
                >
                  <Trophy size={16} />
                  Validate Clearance
                </button>
                <button
                  onClick={() => setShowConfirm('fail')}
                  className="flex-1 py-3.5 bg-red-950/30 hover:bg-red-900/50 border border-red-500 text-red-400 hover:text-red-200 font-mono text-sm tracking-widest uppercase transition-all duration-300 flex items-center justify-center gap-2 hover:shadow-[0_0_15px_rgba(239,68,68,0.2)]"
                >
                  <ShieldAlert size={16} />
                  Acknowledge Defeat
                </button>
              </>
            ) : (
              <motion.div
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: -10 }}
                className="w-full flex gap-4"
              >
                <button
                  onClick={() => handleAction(showConfirm)}
                  className={`flex-1 py-3.5 font-mono text-sm tracking-widest uppercase transition-all duration-300 text-white ${
                    showConfirm === 'complete'
                      ? 'bg-green-600 hover:bg-green-500 shadow-[0_0_20px_rgba(34,197,94,0.4)]'
                      : 'bg-red-600 hover:bg-red-500 shadow-[0_0_20px_rgba(239,68,68,0.4)]'
                  }`}
                >
                  Confirm {showConfirm}
                </button>
                <button
                  onClick={() => setShowConfirm(null)}
                  className="flex-1 py-3.5 bg-gray-800 hover:bg-gray-700 text-gray-200 font-mono text-sm tracking-widest uppercase transition-all duration-300"
                >
                  Cancel
                </button>
              </motion.div>
            )}
          </AnimatePresence>
        </div>
      </motion.div>
    </div>
  );
};
export default RedGateOverrideScreen;
