import React from 'react';
import { Plus } from 'lucide-react';

interface Stat {
    key: string;
    label: string;
    value: number;
}

interface StatAllocationPanelProps {
    level: number;
    currentXp: number;
    maxXp: number;
    freePoints: number;
    stats: Stat[];
    onAllocate: (statKey: string, amount: number) => void;
}

export const StatAllocationPanel: React.FC<StatAllocationPanelProps> = ({
    level,
    currentXp,
    maxXp,
    freePoints,
    stats,
    onAllocate
}) => {
    const xpPercentage = Math.min(100, Math.max(0, (currentXp / maxXp) * 100));

    return (
        <div className="bg-gray-900/95 backdrop-blur border border-solo-blue-800 rounded-lg p-6 w-full max-w-sm shadow-card">
            {/* Header: Level & XP */}
            <div className="mb-6">
                <div className="flex justify-between items-end mb-2">
                    <h2 className="text-2xl font-black text-white italic tracking-wider">LEVEL {level}</h2>
                    <span className="text-xs text-solo-blue-400 font-mono mb-1">
                        {currentXp} / {maxXp} XP
                    </span>
                </div>

                {/* XP Bar */}
                <div className="h-2 bg-gray-800 rounded-full overflow-hidden border border-gray-700">
                    <div
                        className="h-full bg-gradient-to-r from-solo-blue-600 to-cyan-400 shadow-glow-cyan transition-all duration-500"
                        style={{ width: `${xpPercentage}%` }}
                    />
                </div>
            </div>

            {/* Free Points Indicator */}
            <div className="flex justify-between items-center bg-solo-blue-900/30 border border-solo-blue-500/30 rounded px-4 py-2 mb-4">
                <span className="text-sm text-solo-blue-200">AVAILABLE POINTS</span>
                <span className="text-xl font-bold text-white text-shadow-glow">{freePoints}</span>
            </div>

            {/* Stats List */}
            <div className="space-y-3">
                {stats.map((stat) => (
                    <div key={stat.key} className="flex items-center justify-between group">
                        <div className="flex flex-col">
                            <span className="text-gray-400 text-xs font-bold uppercase tracking-widest group-hover:text-solo-blue-300 transition-colors">
                                {stat.label}
                            </span>
                            <span className="text-lg font-mono text-white">{stat.value}</span>
                        </div>

                        {/* Allocation Controls */}
                        {freePoints > 0 && (
                            <button
                                onClick={() => onAllocate(stat.key, 1)}
                                className="w-8 h-8 flex items-center justify-center rounded bg-gray-800 border border-gray-600 text-gray-400 hover:bg-solo-blue-600 hover:border-solo-blue-400 hover:text-white transition-all active:scale-95"
                            >
                                <Plus size={16} />
                            </button>
                        )}
                    </div>
                ))}
            </div>

            {/* Footer / Context */}
            <div className="mt-6 pt-4 border-t border-gray-800 text-center">
                <p className="text-[10px] text-gray-600 italic">
                    "Strength is the only thing that matters in this world."
                </p>
            </div>
        </div>
    );
};
