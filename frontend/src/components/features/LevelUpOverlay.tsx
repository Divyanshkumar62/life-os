import React from 'react';
import ReactDOM from 'react-dom';
import { motion } from 'framer-motion';
import { Shield, Coins, Sparkles, AlertTriangle } from 'lucide-react';

interface LevelUpOverlayProps {
    isOpen: boolean;
    data: {
        newLevel: number;
        previousLevel: number;
        statPointsAwarded: number;
        goldAwarded: number;
        debuffsCleansed: boolean;
        xpFrozen: boolean;
        rankCapLevel: number;
    } | null;
    onClose: () => void;
    onAllocate: () => void;
}

export const LevelUpOverlay: React.FC<LevelUpOverlayProps> = ({ isOpen, data, onClose, onAllocate }) => {
    if (!isOpen || !data) return null;

    const overlayContent = (
        <div data-testid="level-up-overlay" className="fixed inset-0 z-[99999] bg-black/95 backdrop-blur-md flex items-center justify-center p-4 overflow-hidden">
            <div className="absolute inset-0 bg-[radial-gradient(circle_at_center,rgba(234,179,8,0.08)_0%,transparent_70%)] pointer-events-none" />

            <motion.div 
                initial={{ opacity: 0, scale: 0.85, y: 30 }}
                animate={{ opacity: 1, scale: 1, y: 0 }}
                exit={{ opacity: 0, scale: 0.9, y: -20 }}
                transition={{ type: "spring", duration: 0.6 }}
                className="max-w-md w-full border-2 border-yellow-500 bg-slate-950 p-8 shadow-[0_0_50px_rgba(234,179,8,0.25)] relative text-center"
            >
                <div className="absolute top-0 left-0 w-4 h-4 border-t-2 border-l-2 border-yellow-400" />
                <div className="absolute top-0 right-0 w-4 h-4 border-t-2 border-r-2 border-yellow-400" />
                <div className="absolute bottom-0 left-0 w-4 h-4 border-b-2 border-l-2 border-yellow-400" />
                <div className="absolute bottom-0 right-0 w-4 h-4 border-b-2 border-r-2 border-yellow-400" />

                <div className="space-y-1 mb-8">
                    <div className="flex justify-center gap-1 text-yellow-500 animate-bounce">
                        <Sparkles size={24} />
                    </div>
                    <h1 className="text-4xl font-extrabold font-mono tracking-[0.25em] text-yellow-500 glow-text-gold uppercase">
                        LEVEL UP
                    </h1>
                    <p className="text-gray-400 font-mono text-xs uppercase tracking-widest mt-1">
                        The System has acknowledged your growth
                    </p>
                </div>

                <div className="bg-slate-900/60 border border-yellow-500/20 py-4 mb-6 relative overflow-hidden">
                    <div className="text-xs text-gray-500 font-mono tracking-widest uppercase mb-1">Hunter Level Transition</div>
                    <div className="flex items-center justify-center gap-6 font-mono">
                        <span className="text-xl text-gray-500">LV.{data.previousLevel}</span>
                        <span className="text-2xl text-yellow-500">➔</span>
                        <span className="text-4xl font-black text-yellow-400 glow-text-gold">LV.{data.newLevel}</span>
                    </div>
                </div>

                <div className="space-y-3 mb-8 text-left font-mono">
                    <h3 className="text-xs text-gray-400 uppercase tracking-widest border-b border-slate-800 pb-1 mb-2">Rewards Obtained</h3>
                    
                    <div className="flex items-center justify-between p-3 border border-yellow-500/10 bg-yellow-500/5 rounded-sm">
                        <div className="flex items-center gap-2 text-yellow-400">
                            <Sparkles size={16} />
                            <span className="text-xs uppercase tracking-wider">Free Stat Points</span>
                        </div>
                        <span className="text-sm font-bold text-yellow-400">+{data.statPointsAwarded}</span>
                    </div>

                    <div className="flex items-center justify-between p-3 border border-amber-500/10 bg-amber-500/5 rounded-sm">
                        <div className="flex items-center gap-2 text-amber-400">
                            <Coins size={16} />
                            <span className="text-xs uppercase tracking-wider">Gold Awarded</span>
                        </div>
                        <span className="text-sm font-bold text-amber-400">+{data.goldAwarded}</span>
                    </div>

                    {data.debuffsCleansed && (
                        <div className="flex items-center justify-between p-3 border border-green-500/10 bg-green-500/5 rounded-sm">
                            <div className="flex items-center gap-2 text-green-400">
                                <Shield size={16} />
                                <span className="text-xs uppercase tracking-wider">Condition</span>
                            </div>
                            <span className="text-xs font-bold text-green-400 uppercase tracking-wider">DEBUFFS CLEANSED</span>
                        </div>
                    )}

                    {data.xpFrozen && (
                        <div className="flex items-center gap-2 p-3 border border-red-500/20 bg-red-950/20 text-red-400 rounded-sm">
                            <AlertTriangle size={16} className="shrink-0 animate-pulse" />
                            <div className="text-[10px] leading-relaxed uppercase tracking-wider">
                                LEVEL CAP BREACHED (LV.{data.rankCapLevel}). XP accumulators frozen. Clear the Promotion Exam to unlock further growth.
                            </div>
                        </div>
                    )}
                </div>

                <div className="flex flex-col gap-3">
                    <button
                        onClick={onAllocate}
                        className="w-full py-3 bg-yellow-500 hover:bg-yellow-600 text-black font-mono font-bold text-sm tracking-widest uppercase transition-colors shadow-[0_0_15px_rgba(234,179,8,0.2)] hover:shadow-[0_0_20px_rgba(234,179,8,0.35)]"
                    >
                        ALLOCATE POINTS
                    </button>
                    <button
                        onClick={onClose}
                        className="w-full py-2 border border-slate-700 hover:border-slate-500 text-slate-400 hover:text-white font-mono text-xs tracking-widest uppercase transition-colors"
                    >
                        Close
                    </button>
                </div>
            </motion.div>
        </div>
    );

    return ReactDOM.createPortal(overlayContent, document.body);
};
