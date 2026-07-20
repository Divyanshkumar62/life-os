import React, { useState, useEffect } from "react";
import { motion } from "framer-motion";
import { useNavigate } from "react-router-dom";
import { JobChangeAPI, systemMessageEmitter } from "../../../api/api";
import { useSystemContext } from "../../../context/SystemContext";
import { usePlayerStore } from "../../../stores/usePlayerStore";

interface ClassOption {
  id: string;
  title: string;
  subtitle: string;
  description: string;
  bonuses: string[];
  color: string;
  borderColor: string;
  bgClass: string;
  glowColor: string;
}

const CLASS_OPTIONS: ClassOption[] = [
  {
    id: "Vanguard",
    title: "VANGUARD",
    subtitle: "Steel & Resolve",
    description: "The path of the warrior. Dominate through raw strength and unbreakable fortitude.",
    bonuses: ["+15% Physical Quest XP", "STR & VIT Multiplier Bonuses"],
    color: "text-red-400",
    borderColor: "border-red-500/60",
    bgClass: "from-red-950/30 to-red-900/10",
    glowColor: "rgba(239,68,68,0.3)",
  },
  {
    id: "Scholar",
    title: "SCHOLAR",
    subtitle: "Mind & Wisdom",
    description: "The path of the architect. Master the system through intellect and perception.",
    bonuses: ["+15% Cognitive Quest XP", "INT & SEN Multiplier Bonuses"],
    color: "text-cyan-400",
    borderColor: "border-cyan-500/60",
    bgClass: "from-cyan-950/30 to-cyan-900/10",
    glowColor: "rgba(34,211,238,0.3)",
  },
  {
    id: "Shadow",
    title: "SHADOW",
    subtitle: "Stealth & Balance",
    description: "The path of the monarch. Strike from darkness with balanced precision.",
    bonuses: ["+5% Physical / +5% Cognitive XP", "AGI & Balanced Bonuses", "Loot Box Chance"],
    color: "text-purple-400",
    borderColor: "border-purple-500/60",
    bgClass: "from-purple-950/30 to-purple-900/10",
    glowColor: "rgba(168,85,247,0.3)",
  },
];

export const ClassSelectionScreen: React.FC = () => {
  const navigate = useNavigate();
  const { playerId, refreshSystem, applyTheme } = useSystemContext();
  const fetchPlayerState = usePlayerStore((state) => state.fetchPlayerState);
  
  const [selectedClass, setSelectedClass] = useState<string | null>(null);
  const [recommendedClass, setRecommendedClass] = useState<string | null>(null);
  const [evolving, setEvolving] = useState(false);
  const [statusMessage, setStatusMessage] = useState<string | null>(null);
  const [rewards, setRewards] = useState<any>(null);

  useEffect(() => {
    if (!playerId) return;
    JobChangeAPI.getStatus(playerId).then((data) => {
      if (data.recommendedClass) {
        setRecommendedClass(data.recommendedClass);
      }
    }).catch(() => {});
  }, [playerId]);

  const handleEvolve = async () => {
    if (!playerId || !selectedClass || evolving) return;
    setEvolving(true);
    setStatusMessage(null);
    try {
      const result = await JobChangeAPI.selectClass(playerId, selectedClass);
      setRewards(result.evolutionRewards);
      setStatusMessage(`[SYSTEM] Evolution Complete. Class: ${result.jobClass}.`);
      systemMessageEmitter.emit([`[SYSTEM] Evolution Complete. Class Acquired: ${result.jobClass}.`]);
      applyTheme(result.jobClass);
      await fetchPlayerState(playerId);
      await refreshSystem();
      setTimeout(() => navigate("/dashboard", { replace: true }), 2500);
    } catch (err: any) {
      setStatusMessage(err?.message || "[SYSTEM] Evolution failed. The Architect denies your choice.");
    } finally {
      setEvolving(false);
    }
  };

  const isRecommended = (classId: string) => recommendedClass?.toLowerCase() === classId.toLowerCase();

  return (
    <div className="min-h-screen bg-gradient-to-b from-black via-gray-950 to-black text-white flex flex-col font-mono relative overflow-hidden select-none">
      <div className="absolute inset-0 opacity-[0.03] bg-[radial-gradient(circle_at_50%_50%,rgba(255,255,255,0.1)_0%,transparent_70%)] pointer-events-none" />
      
      <div className="flex-1 max-w-5xl w-full mx-auto px-6 py-12 flex flex-col justify-center relative z-10">
        <motion.div
          initial={{ opacity: 0, y: -20 }}
          animate={{ opacity: 1, y: 0 }}
          className="text-center mb-12"
        >
          <span className="text-[10px] text-gray-500 uppercase tracking-[0.3em]">Level 40 Threshold Crossed</span>
          <h1 className="text-4xl md:text-5xl font-black text-white tracking-[0.15em] mt-2 mb-3 uppercase drop-shadow-[0_0_20px_rgba(255,255,255,0.1)]">
            SECOND AWAKENING
          </h1>
          <p className="text-xs text-gray-400 max-w-lg mx-auto leading-relaxed uppercase tracking-widest">
            Choose your evolutionary path. This decision is permanent.
          </p>
        </motion.div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-10">
          {CLASS_OPTIONS.map((opt, i) => {
            const isSelected = selectedClass === opt.id;
            const recommended = isRecommended(opt.id);

            return (
              <motion.button
                key={opt.id}
                data-testid={`class-card-${opt.id.toLowerCase()}`}
                initial={{ opacity: 0, y: 30 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: i * 0.15 }}
                onClick={() => setSelectedClass(opt.id)}
                className={`relative flex flex-col items-start text-left p-6 border transition-all duration-300 bg-gradient-to-b ${opt.bgClass} ${
                  isSelected ? opt.borderColor + " shadow-[0_0_30px_" + opt.glowColor + "]" : "border-gray-800 hover:border-gray-600"
                }`}
              >
                {recommended && (
                  <div className="absolute -top-2.5 left-4 px-3 py-0.5 bg-yellow-500/20 border border-yellow-500/60 text-yellow-300 text-[9px] font-bold uppercase tracking-widest rounded-none">
                    System Recommended: +5 Stat Points
                  </div>
                )}

                <div className={`text-2xl font-black tracking-[0.15em] mb-0.5 ${isSelected ? opt.color : "text-gray-300"}`}>
                  {opt.title}
                </div>
                <div className={`text-[10px] uppercase tracking-widest mb-3 ${isSelected ? "text-gray-400" : "text-gray-600"}`}>
                  {opt.subtitle}
                </div>
                <div className="text-[11px] text-gray-500 leading-relaxed mb-4 font-sans">
                  {opt.description}
                </div>
                <div className="border-t border-gray-800 pt-3 w-full">
                  {opt.bonuses.map((bonus, j) => (
                    <div key={j} className="text-[9px] text-green-400/70 uppercase tracking-wider mb-0.5">
                      + {bonus}
                    </div>
                  ))}
                </div>

                {isSelected && (
                  <div className={`absolute inset-0 border-2 pointer-events-none ${opt.borderColor}`} />
                )}
              </motion.button>
            );
          })}
        </div>

        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 0.6 }}
          className="flex flex-col items-center gap-4"
        >
          {statusMessage && (
            <div className={`text-xs font-mono tracking-widest uppercase text-center px-4 py-2 border ${
              rewards ? "border-green-500/60 text-green-400 bg-green-950/10" : "border-red-500/60 text-red-400 bg-red-950/10"
            }`}>
              {statusMessage}
            </div>
          )}

          {rewards && (
            <div className="border border-green-500/30 bg-green-950/10 p-4 text-center max-w-md">
              <p className="text-[10px] text-green-400 uppercase tracking-widest mb-2 font-bold">REWARDS</p>
              <p className="text-xs text-green-300">+{rewards.statPointsAwarded} Stat Points</p>
              <p className="text-xs text-green-300">{rewards.goldAwarded?.toLocaleString()} Gold</p>
              {rewards.itemsAwarded?.map((item: string, j: number) => (
                <p key={j} className="text-xs text-purple-300">{item}</p>
              ))}
            </div>
          )}

          {!rewards && (
            <button
              data-testid="evolve-button"
              onClick={handleEvolve}
              disabled={!selectedClass || evolving}
              className="px-12 py-4 bg-gradient-to-r from-solo-red/80 to-solo-red/60 border border-solo-red text-white font-black text-sm tracking-[0.25em] uppercase transition-all duration-300 disabled:opacity-30 disabled:cursor-not-allowed hover:shadow-[0_0_40px_rgba(255,0,60,0.4)] hover:scale-105"
            >
              {evolving ? (
                <span className="flex items-center gap-3">
                  <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                  EVOLVING...
                </span>
              ) : (
                "EVOLVE"
              )}
            </button>
          )}
        </motion.div>
      </div>
    </div>
  );
};
