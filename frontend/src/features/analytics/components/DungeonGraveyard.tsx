import React, { useEffect, useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { Skull, RefreshCw, Star } from "lucide-react";
import { api } from "../../../api/api";

interface GraveyardDungeon {
  dungeonId: string;
  title: string;
  description: string;
  dungeonRank: string;
  dungeonStatus: "COMPLETED" | "FAILED" | "SHADOW" | "PERMADEATH" | "ABANDONED";
  totalFloors: number;
  completedFloors: number;
  failedAt?: string;
  completedAt?: string;
}

export const DungeonGraveyard: React.FC<{ playerId: string }> = ({ playerId }) => {
  const [dungeons, setDungeons] = useState<GraveyardDungeon[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [, setError] = useState<string | null>(null);
  const [ariseAnimationId, setAriseAnimationId] = useState<string | null>(null);

  const fetchGraveyard = async () => {
    if (!playerId) return;
    setLoading(true);
    setError(null);
    try {
      const response = await api.get(`/analytics/graveyard?playerId=${playerId}`);
      setDungeons(response || []);
    } catch (err: any) {
      console.error("Graveyard Fetch Error, using mock backup:", err);
      // Fallback mocks
      setDungeons([
        {
          dungeonId: "goblin-lair-failed",
          title: "Goblin Lair Raid",
          description: "Attempted cleanout of Goblin Cave. Interrupted by Vanguard Vanguard.",
          dungeonRank: "C",
          dungeonStatus: "FAILED",
          totalFloors: 3,
          completedFloors: 1,
          failedAt: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000).toISOString(),
        },
        {
          dungeonId: "demon-castle-completed",
          title: "Demon Castle Lower Floor",
          description: "Defeated Hell's gatekeeper Cerberus.",
          dungeonRank: "A",
          dungeonStatus: "COMPLETED",
          totalFloors: 5,
          completedFloors: 5,
          completedAt: new Date(Date.now() - 5 * 24 * 60 * 60 * 1000).toISOString(),
        },
        {
          dungeonId: "spider-nest-abandoned",
          title: "Spider Nest Outpost",
          description: "Arachnid infestation inside mines.",
          dungeonRank: "D",
          dungeonStatus: "ABANDONED",
          totalFloors: 4,
          completedFloors: 0,
          failedAt: new Date(Date.now() - 10 * 24 * 60 * 60 * 1000).toISOString(),
        },
      ]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchGraveyard();
  }, [playerId]);

  const handleArise = async (dungeonId: string) => {
    setAriseAnimationId(dungeonId);
    try {
      // Execute the ARISE POST endpoint request
      await api.post(`/dungeons/${dungeonId}/arise?playerId=${playerId}`);

      // Small delay to let extraction animation play out
      setTimeout(() => {
        setDungeons((prev) =>
          prev.map((d) =>
            d.dungeonId === dungeonId
              ? { ...d, dungeonStatus: "SHADOW" as const }
              : d
          )
        );
        setAriseAnimationId(null);
      }, 2000);
    } catch (err: any) {
      console.error("COMMAND ARISE Failed:", err);
      alert("COMMAND ARISE Failed: Requires active COMMAND_ARISE consumable key.");
      setAriseAnimationId(null);
    }
  };

  const getStatusColor = (status: GraveyardDungeon["dungeonStatus"]): string => {
    switch (status) {
      case "COMPLETED":
        return "text-green-400 border-green-950 bg-green-950/10";
      case "FAILED":
        return "text-red-400 border-red-950 bg-red-950/10";
      case "SHADOW":
        return "text-violet-400 border-violet-900 bg-violet-950/20 shadow-[0_0_15px_rgba(124,58,237,0.4)]";
      case "PERMADEATH":
        return "text-zinc-600 border-zinc-950 bg-zinc-950/10";
      case "ABANDONED":
      default:
        return "text-yellow-500 border-yellow-950/30 bg-yellow-950/10";
    }
  };

  const getBorderColor = (dungeon: GraveyardDungeon): string => {
    if (ariseAnimationId === dungeon.dungeonId) {
      return "border-violet-500 shadow-[0_0_20px_rgba(139,92,246,0.6)] animate-pulse";
    }
    if (dungeon.dungeonStatus === "SHADOW") {
      return "border-violet-800 shadow-[0_0_15px_rgba(124,58,237,0.35)]";
    }
    if (dungeon.dungeonStatus === "COMPLETED") {
      return "border-green-900/50 hover:border-green-500/50";
    }
    if (dungeon.dungeonStatus === "FAILED") {
      return "border-red-900/50 hover:border-red-500/50";
    }
    return "border-zinc-900 hover:border-zinc-700";
  };

  if (loading) {
    return (
      <div className="w-full h-48 bg-[#0a0a0f] border border-gray-900 flex items-center justify-center font-mono text-xs text-violet-400">
        <span className="animate-pulse tracking-widest uppercase">CONJURING SHADOW GRAVEYARD...</span>
      </div>
    );
  }

  return (
    <div className="w-full bg-[#0a0a0f] border border-gray-900 p-6 flex flex-col font-sans text-white relative">
      {/* Title */}
      <div className="flex justify-between items-center mb-6">
        <div className="flex items-center gap-2">
          <Skull className="text-violet-500" size={18} />
          <h3 className="text-lg font-black tracking-widest uppercase font-display text-violet-400">
            DUNGEON GRAVEYARD (SHADOW RECURSIONS)
          </h3>
        </div>
        <button
          onClick={fetchGraveyard}
          className="text-xs font-mono text-gray-500 hover:text-cyan-400 transition-colors flex items-center gap-1"
        >
          <RefreshCw size={12} />
          Refresh
        </button>
      </div>

      {/* Grid of cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {dungeons.length === 0 ? (
          <div className="col-span-2 py-8 text-center text-gray-500 font-mono text-xs">
            No historical dungeon incursions detected.
          </div>
        ) : (
          dungeons.map((d) => (
            <motion.div
              key={d.dungeonId}
              layout
              className={`p-5 bg-black/90 border transition-all duration-500 rounded-none relative flex flex-col justify-between overflow-hidden ${getBorderColor(
                d
              )}`}
            >
              {/* Extraction Overlay Animation */}
              <AnimatePresence>
                {ariseAnimationId === d.dungeonId && (
                  <motion.div
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    exit={{ opacity: 0 }}
                    className="absolute inset-0 bg-violet-950/80 backdrop-blur-sm z-10 flex flex-col items-center justify-center"
                  >
                    <motion.div
                      animate={{ scale: [1, 1.2, 1], rotate: [0, 5, -5, 0] }}
                      transition={{ duration: 0.5, repeat: Infinity }}
                      className="text-3xl font-black text-violet-300 tracking-[0.3em] font-display"
                    >
                      ARISE
                    </motion.div>
                    <span className="text-[10px] font-mono text-violet-400 mt-2 animate-pulse uppercase tracking-widest">
                      Extracting Shadow Remnants...
                    </span>
                  </motion.div>
                )}
              </AnimatePresence>

              <div>
                {/* Header card info */}
                <div className="flex justify-between items-start mb-2">
                  <h4 className="text-md font-bold tracking-wide font-display text-white">
                    {d.title}
                  </h4>
                  <div className="flex gap-2">
                    <span className="px-1.5 py-0.5 bg-zinc-900 border border-zinc-800 text-gray-300 font-mono text-[9px] uppercase tracking-wider">
                      Rank {d.dungeonRank}
                    </span>
                    <span
                      className={`px-1.5 py-0.5 border font-mono text-[9px] uppercase tracking-wider ${getStatusColor(
                        d.dungeonStatus
                      )}`}
                    >
                      {d.dungeonStatus}
                    </span>
                  </div>
                </div>

                <p className="text-xs text-gray-400 font-mono leading-relaxed mb-4">
                  {d.description}
                </p>

                {/* Progress Stats */}
                <div className="flex justify-between items-center text-[10px] font-mono text-gray-500 mb-4">
                  <span>Clearence Floors: {d.completedFloors} / {d.totalFloors}</span>
                  <span>
                    {d.failedAt && `FAILED: ${new Date(d.failedAt).toLocaleDateString()}`}
                    {d.completedAt && `CLEARED: ${new Date(d.completedAt).toLocaleDateString()}`}
                  </span>
                </div>
              </div>

              {/* Action buttons */}
              {d.dungeonStatus === "FAILED" && (
                <button
                  onClick={() => handleArise(d.dungeonId)}
                  className="w-full py-2 bg-violet-950/20 hover:bg-violet-900/40 border border-violet-700 hover:border-violet-500 text-violet-300 font-mono text-xs uppercase tracking-widest transition-all duration-300 flex items-center justify-center gap-1.5 hover:shadow-[0_0_10px_rgba(139,92,246,0.3)]"
                >
                  <Skull size={12} className="animate-pulse" />
                  COMMAND: ARISE
                </button>
              )}

              {d.dungeonStatus === "SHADOW" && (
                <div className="w-full py-2 bg-violet-950/10 border border-violet-900/30 text-violet-400/60 font-mono text-xs uppercase tracking-widest text-center flex items-center justify-center gap-1.5 cursor-not-allowed">
                  <Star size={12} className="text-violet-500" />
                  Shadow Extracted
                </div>
              )}
            </motion.div>
          ))
        )}
      </div>
    </div>
  );
};
export default DungeonGraveyard;
