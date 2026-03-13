import React, { useEffect, useState } from 'react';
import { SystemWindow } from '../../components/onboarding/SystemWindow';
import { GlowButton } from '../../components/onboarding/GlowButton';

// As requested, starting everyone flat at 10
const INITIAL_STATS = {
    'DISCIPLINE': 10,
    'STRENGTH': 10,
    'INTELLECT': 10,
    'VITALITY': 10,
    'SENSE': 10
};

interface AwakeningScreenProps {
    onNext: (stats: Record<string, number>) => void;
}

export const AwakeningScreen: React.FC<AwakeningScreenProps> = ({ onNext }) => {
    const [revealed, setRevealed] = useState(false);

    useEffect(() => {
        // Simple reveal animation trigger
        const timer = setTimeout(() => setRevealed(true), 500);
        return () => clearTimeout(timer);
    }, []);

    const handleComplete = () => {
        onNext(INITIAL_STATS);
    };

    return (
        <div className="flex items-center justify-center min-h-[70vh] gap-8 animate-[fadeIn_1s_ease-out]">

            {/* Avatar Silhouette - Left Side */}
            <div className={`
        hidden md:flex flex-col items-center justify-center w-72 h-[500px] 
        border border-[#0ea5e9]/30 bg-slate-900/50 backdrop-blur rounded relative overflow-hidden transition-all duration-1000
        ${revealed ? 'opacity-100 translate-x-0' : 'opacity-0 -translate-x-10'}
      `}>
                <div className="absolute inset-0 bg-[radial-gradient(circle_at_top,_var(--tw-gradient-stops))] from-blue-500/10 to-transparent" />

                {/* Placeholder Silhouette */}
                <div className="w-40 h-80 bg-slate-800 rounded-full opacity-40 blur-md mb-8" />

                <div className="absolute bottom-0 inset-x-0 p-6 bg-gradient-to-t from-black/80 to-transparent">
                    <div className="text-center space-y-1">
                        <div className="text-[#0ea5e9] font-bold text-2xl tracking-widest glow-text-blue">PLAYER</div>
                        <div className="text-slate-500 text-xs font-mono">CLASS: NONE</div>
                        <div className="text-white font-mono text-sm mt-2 border px-2 py-1 rounded border-slate-700 inline-block">LVL. 1</div>
                    </div>
                </div>
            </div>

            {/* Stats Panel - Right Side */}
            <SystemWindow
                title="STATUS RECOVERY"
                subtitle="PLAYER_PROFILING_COMPLETE"
                className={`max-w-lg w-full transition-all duration-1000 delay-300 ${revealed ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-10'}`}
            >
                <div className="space-y-6">
                    <div className="border-b border-white/10 pb-4 mb-4">
                        <h3 className="text-white text-lg font-light">
                            Welcome, <span className="text-[#0ea5e9] font-bold">Player.</span>
                        </h3>
                        <p className="text-slate-400 text-sm mt-2 leading-relaxed">
                            The System has accepted your resolve. Your physiological and psychological data has been quantified.
                        </p>
                    </div>

                    <div className="space-y-3">
                        {Object.entries(INITIAL_STATS).map(([key, val], idx) => (
                            <div
                                key={key}
                                className="flex items-center justify-between group"
                                style={{ animationDelay: `${idx * 100 + 500}ms` }}
                            >
                                <div className="text-slate-400 font-mono text-sm uppercase w-24 group-hover:text-white transition-colors">
                                    {key.substring(0, 3)}
                                </div>
                                <div className="flex-1 mx-4 h-2 bg-slate-800 rounded-full overflow-hidden relative">
                                    <div
                                        className="h-full bg-[#0ea5e9] shadow-[0_0_10px_#0ea5e9]"
                                        style={{ width: `${(val / 20) * 100}%` }}
                                    />
                                </div>
                                <div className="font-mono text-white text-lg w-8 text-right font-bold">
                                    {val}
                                </div>
                            </div>
                        ))}
                    </div>

                    <div className="pt-8 border-t border-white/10 flex justify-end">
                        <GlowButton onClick={handleComplete} pulsating>
                            CONFIRM STATUS
                        </GlowButton>
                    </div>
                </div>
            </SystemWindow>
        </div>
    );
};
