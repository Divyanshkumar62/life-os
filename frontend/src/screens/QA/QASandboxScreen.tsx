import React from "react";
import { motion } from "framer-motion";
import { 
  LayoutDashboard, 
  Skull, 
  AlertTriangle, 
  ShoppingBag, 
  ChevronRight,
  Eye,
  Sliders,
  X
} from "lucide-react";
import { ScreenFrame } from "../../components/layout";

interface SandboxCard {
  key: string;
  title: string;
  description: string;
  icon: React.ReactNode;
  badge: string;
  badgeColor: string;
  details: string[];
}

interface QASandboxScreenProps {
  onSelectView: (view: string) => void;
  onClose: () => void;
}

export const QASandboxScreen: React.FC<QASandboxScreenProps> = ({ onSelectView, onClose }) => {
  const cards: SandboxCard[] = [
    {
      key: "dashboard",
      title: "Main Dashboard",
      description: "Visualizes player progression, daily active quest progress, and equipped theme state.",
      icon: <LayoutDashboard size={24} className="text-solo-cyan" />,
      badge: "Level 24",
      badgeColor: "border-solo-cyan text-solo-cyan",
      details: ["C-Rank authoritative state", "3 active daily quests", "Vanguard display theme"]
    },
    {
      key: "dungeon",
      title: "Dungeon View",
      description: "Renders the multi-floor quest gate, showcasing cleared, active, and locked floor progression.",
      icon: <Skull size={24} className="text-solo-cyan" />,
      badge: "Goblin Lair",
      badgeColor: "border-solo-gold text-solo-gold",
      details: ["C-Rank difficulty", "3 floors structured quests", "Floor 1 complete, Floor 2 active"]
    },
    {
      key: "penalty",
      title: "Penalty Zone",
      description: "Simulates the lockout eviction phase. User must submit a sincere reflection or suffer lockout countdown.",
      icon: <AlertTriangle size={24} className="text-solo-red" />,
      badge: "Eviction Lockout",
      badgeColor: "border-solo-red text-solo-red animate-pulse",
      details: ["4-Hour remaining lockout timer", "3 failed confession attempts", "Oppressive Crimson UI override"]
    },
    {
      key: "store",
      title: "System Store",
      description: "Displays available shop consumables and equipment items, checking level threshold requirements.",
      icon: <ShoppingBag size={24} className="text-solo-cyan" />,
      badge: "5,000 Gold",
      badgeColor: "border-green-500 text-green-400",
      details: ["Level 10 shop unlocked", "Displays Monarch's Exemption", "Stock limits verification"]
    },
    {
      key: "job_change",
      title: "Job Change Gauntlet",
      description: "Renders the legendary popup overlay forcing hunter class evolution and Day 3 Boss Room trials.",
      icon: <Sliders size={24} className="text-purple-400" />,
      badge: "Level 40",
      badgeColor: "border-purple-500 text-purple-400",
      details: ["Day 1 & Day 2 completed", "Day 3 Boss Trial active", "Shadow Necromancer evolution path"]
    },
    {
      key: "observer",
      title: "Observer Screen",
      description: "Aggregates visual heatmap logs, stat growth trajectories, and graveyard records of past lockouts.",
      icon: <Eye size={24} className="text-solo-cyan" />,
      badge: "365-Day Metrics",
      badgeColor: "border-solo-cyan text-solo-cyan",
      details: ["Populated daily heatmap grid", "STR/INT/VIT/AGI/SEN progression chart", "Lockout failure records list"]
    }
  ];

  return (
    <ScreenFrame className="min-h-screen bg-solo-bg text-white font-mono flex flex-col relative select-none">
      {/* Background Grid Pattern Overlay */}
      <div className="absolute inset-0 bg-[linear-gradient(to_right,#1f29370a_1px,transparent_1px),linear-gradient(to_bottom,#1f29370a_1px,transparent_1px)] bg-[size:24px_24px] pointer-events-none" />
      <div className="absolute top-[30%] left-[50%] -translate-x-[50%] -translate-y-[30%] w-[600px] h-[600px] bg-solo-blue-900/10 rounded-full blur-[150px] pointer-events-none" />

      {/* Top Header Navigation */}
      <div className="relative z-10 flex justify-between items-center border-b border-gray-800 pb-4 mb-8">
        <div>
          <h1 className="text-2xl md:text-3xl font-black text-white tracking-[0.2em] uppercase flex items-center gap-3">
            <span className="text-solo-cyan">[</span>
            SYSTEM QA SANDBOX
            <span className="text-solo-cyan">]</span>
          </h1>
          <p className="text-[10px] md:text-xs text-gray-400 uppercase tracking-widest mt-2 leading-relaxed">
            Developer workspace to isolate components and mock backend authorized DTO payloads.
          </p>
        </div>
        <button
          onClick={onClose}
          className="p-2 border border-gray-800 hover:border-solo-red hover:text-solo-red transition-all duration-300 text-gray-400"
          title="Exit Sandbox"
        >
          <X size={18} />
        </button>
      </div>

      {/* Main Sandbox Grid */}
      <div className="relative z-10 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 flex-1 overflow-y-auto pr-1">
        {cards.map((card) => (
          <motion.div
            key={card.key}
            whileHover={{ y: -4 }}
            onClick={() => onSelectView(card.key)}
            className="group cursor-pointer border border-gray-800 glass-panel p-6 rounded-none flex flex-col justify-between transition-all duration-300 hover:border-solo-cyan hover:shadow-glow-cyan min-h-[220px]"
          >
            <div>
              {/* Header inside card */}
              <div className="flex justify-between items-start mb-4">
                <div className="p-2 bg-gray-900/80 border border-gray-800 group-hover:border-solo-cyan transition-colors duration-300">
                  {card.icon}
                </div>
                <span className={`text-[9px] font-bold px-2 py-0.5 border rounded-none uppercase tracking-widest ${card.badgeColor}`}>
                  {card.badge}
                </span>
              </div>

              {/* Title & Description */}
              <h2 className="text-sm font-bold text-white tracking-widest uppercase group-hover:text-solo-cyan transition-colors duration-300 mb-2">
                {card.title}
              </h2>
              <p className="text-[11px] text-gray-400 uppercase leading-relaxed tracking-wider mb-4">
                {card.description}
              </p>
            </div>

            {/* Simulated Details Checklist */}
            <div className="border-t border-gray-800/80 pt-3 mt-auto">
              <ul className="space-y-1">
                {card.details.map((detail, index) => (
                  <li key={index} className="text-[9px] text-gray-500 uppercase tracking-widest flex items-center gap-1.5">
                    <span className="w-1.5 h-1.5 bg-solo-cyan/30 rounded-full inline-block" />
                    {detail}
                  </li>
                ))}
              </ul>
              <div className="flex items-center justify-end text-solo-cyan text-[10px] font-bold tracking-widest uppercase mt-4 opacity-0 group-hover:opacity-100 transition-opacity duration-300">
                INJECT PAYLOAD <ChevronRight size={10} className="ml-1 group-hover:translate-x-1 transition-transform" />
              </div>
            </div>
          </motion.div>
        ))}
      </div>

      {/* Sticky Bottom Status bar */}
      <div className="relative z-10 mt-8 pt-4 border-t border-gray-800 flex justify-between items-center text-[10px] text-gray-500 uppercase tracking-widest">
        <span>STATUS: SIMULATION_READY</span>
        <span>VER: 1.0.0-DEV</span>
      </div>
    </ScreenFrame>
  );
};
