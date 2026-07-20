import React from "react";
import { motion, AnimatePresence } from "framer-motion";
import { Award, Zap, Coins, Clock, Star, Gift, Check } from "lucide-react";

export interface LootItemDTO {
  itemCode: string;
  name: string;
  quantity: number;
  rarity: string;
}

export interface SpeedrunResultDTO {
  projectId: string;
  projectTitle: string;
  actualDurationDays: number;
  estimatedDurationDays: number;
  speedrunMultiplier: number;
  baseXp: number;
  bonusXp: number;
  totalXp: number;
  baseGold: number;
  bonusGold: number;
  totalGold: number;
  lootDrops: LootItemDTO[];
}

interface SpeedrunResultsScreenProps {
  result: SpeedrunResultDTO | null;
  onClose: () => void;
}

export const SpeedrunResultsScreen: React.FC<SpeedrunResultsScreenProps> = ({ result, onClose }) => {
  if (!result) return null;

  const {
    projectTitle,
    actualDurationDays,
    estimatedDurationDays,
    speedrunMultiplier,
    baseXp,
    bonusXp,
    totalXp,
    baseGold,
    bonusGold,
    totalGold,
    lootDrops,
  } = result;

  const showBonus = speedrunMultiplier > 1.0;

  return (
    <AnimatePresence>
      <div className="fixed inset-0 z-[100] flex items-center justify-center p-4 font-mono">
        {/* Backdrop */}
        <motion.div
          className="absolute inset-0 bg-black/85 backdrop-blur-sm"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          onClick={onClose}
        />

        {/* Modal Container */}
        <motion.div
          className="w-full max-w-lg bg-black border-2 border-solo-gold/80 shadow-[0_0_40px_rgba(234,179,8,0.25)] overflow-hidden relative z-10"
          initial={{ scale: 0.9, y: 20, opacity: 0 }}
          animate={{ scale: 1, y: 0, opacity: 1 }}
          exit={{ scale: 0.9, y: 20, opacity: 0 }}
          transition={{ type: "spring", duration: 0.5 }}
        >
          {/* Header Bar */}
          <div className="bg-solo-gold/10 border-b border-solo-gold/30 px-6 py-4 flex items-center gap-3">
            <Award className="text-solo-gold animate-bounce" size={24} />
            <h2 className="text-xl font-extrabold text-solo-gold tracking-widest uppercase">
              Dungeon Clear Summary
            </h2>
          </div>

          <div className="p-6 space-y-6">
            {/* Title & Speedrun Badge */}
            <div className="text-center">
              <span className="text-[10px] text-gray-500 uppercase tracking-widest block">Subjugation Target</span>
              <h3 className="text-2xl font-bold text-white uppercase mt-1 tracking-wider">{projectTitle}</h3>

              {showBonus ? (
                <div className="inline-flex items-center gap-1.5 mt-3 px-3 py-1 bg-yellow-950/40 border border-solo-gold/50 text-solo-gold text-xs font-bold uppercase rounded-none animate-pulse">
                  <Zap size={12} />
                  <span>Speedrun Multiplier: {speedrunMultiplier.toFixed(2)}x</span>
                </div>
              ) : (
                <div className="inline-flex items-center gap-1.5 mt-3 px-3 py-1 bg-gray-950/40 border border-gray-800 text-gray-400 text-xs uppercase rounded-none">
                  <span>Standard Clear</span>
                </div>
              )}
            </div>

            {/* Time Stats */}
            <div className="bg-gray-950 border border-gray-900 p-3.5 flex justify-around text-center rounded-none">
              <div>
                <span className="text-[9px] text-gray-500 uppercase tracking-wider block">Duration</span>
                <div className="flex items-center justify-center gap-1 text-white mt-1">
                  <Clock size={12} className="text-solo-gold" />
                  <span className="text-sm font-bold">{actualDurationDays} days</span>
                </div>
              </div>
              <div className="w-px bg-gray-900" />
              <div>
                <span className="text-[9px] text-gray-500 uppercase tracking-wider block">Target Time</span>
                <div className="text-gray-400 text-sm font-bold mt-1">
                  {estimatedDurationDays} days
                </div>
              </div>
            </div>

            {/* Rewards Breakdown */}
            <div className="space-y-3">
              <h4 className="text-xs uppercase text-gray-500 font-bold tracking-widest border-b border-gray-900 pb-1.5">
                Clear Rewards
              </h4>

              {/* XP */}
              <div className="flex justify-between items-center text-sm">
                <span className="text-gray-400 flex items-center gap-1.5">
                  <Star size={14} className="text-solo-cyan" /> Experience
                </span>
                <div className="text-right">
                  <span className="font-bold text-solo-cyan">{totalXp} XP</span>
                  {showBonus && (
                    <span className="text-[10px] text-green-400 block font-normal">
                      (Base: {baseXp} + Bonus: {bonusXp})
                    </span>
                  )}
                </div>
              </div>

              {/* Gold */}
              <div className="flex justify-between items-center text-sm">
                <span className="text-gray-400 flex items-center gap-1.5">
                  <Coins size={14} className="text-yellow-500" /> Gold Payout
                </span>
                <div className="text-right">
                  <span className="font-bold text-yellow-500">{totalGold} G</span>
                  {showBonus && (
                    <span className="text-[10px] text-green-400 block font-normal">
                      (Base: {baseGold} + Bonus: {bonusGold})
                    </span>
                  )}
                </div>
              </div>
            </div>

            {/* Loot Drops */}
            {lootDrops && lootDrops.length > 0 && (
              <div className="space-y-3">
                <h4 className="text-xs uppercase text-gray-500 font-bold tracking-widest border-b border-gray-900 pb-1.5">
                  Loot Drops Acquisition
                </h4>
                <div className="grid grid-cols-2 gap-2">
                  {lootDrops.map((loot, idx) => (
                    <div
                      key={idx}
                      className="border border-gray-900 bg-gray-950/40 p-2.5 flex items-center gap-2.5 rounded-none"
                    >
                      <Gift size={16} className="text-purple-400" />
                      <div>
                        <span className="text-xs font-bold text-white block">{loot.name}</span>
                        <span className="text-[9px] text-gray-500 block uppercase">
                          Qty: {loot.quantity} | {loot.rarity}
                        </span>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Action */}
            <button
              onClick={onClose}
              className="w-full py-3.5 bg-solo-gold/20 hover:bg-solo-gold/30 border border-solo-gold/50 hover:border-solo-gold text-solo-gold hover:text-white font-bold uppercase text-xs tracking-widest transition-all duration-300 flex items-center justify-center gap-1.5"
            >
              <Check size={14} />
              <span>Conclude Subjugation Report</span>
            </button>
          </div>
        </motion.div>
      </div>
    </AnimatePresence>
  );
};
