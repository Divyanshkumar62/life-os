import React, { useEffect, useState } from 'react';

const LOGS = [
    "INITIALIZING SYSTEM...",
    "ANALYZING PLAYER DATA...",
    "CALIBRATING ATTRIBUTE BASELINES...",
    "GENERATING PERSONALIZED QUESTS...",
    "SYNCHRONIZING WITH SERVER...",
    "AWAKENING COMPLETE."
];

interface LoadingScreenProps {
    onComplete: () => void;
}

export const LoadingScreen: React.FC<LoadingScreenProps> = ({ onComplete }) => {
    const [logIndex, setLogIndex] = useState(0);

    useEffect(() => {
        if (logIndex < LOGS.length) {
            const timeout = setTimeout(() => {
                setLogIndex(prev => prev + 1);
            }, 800 + Math.random() * 1000); // Random delay for realism
            return () => clearTimeout(timeout);
        } else {
            // Done
            setTimeout(() => {
                onComplete();
            }, 1000);
        }
    }, [logIndex]);

    return (
        <div className="fixed inset-0 bg-[#020617] flex flex-col items-center justify-center z-50">
            {/* Magic Circle / Spinning Loader */}
            <div className="relative w-64 h-64 mb-8">
                <div className="absolute inset-0 border-4 border-[#0ea5e9]/20 rounded-full animate-[spin_10s_linear_infinite]" />
                <div className="absolute inset-4 border-2 border-[#0ea5e9]/40 rounded-full border-dashed animate-[spin_15s_linear_infinite_reverse]" />
                <div className="absolute inset-12 border border-cyan-400/60 rounded-full animate-pulse" />

                <div className="absolute inset-0 flex items-center justify-center">
                    <span className="text-cyan-400 font-mono text-sm animate-pulse tracking-widest">LOADING</span>
                </div>
            </div>

            {/* Terminal Logs */}
            <div className="font-mono text-sm w-80">
                {LOGS.slice(0, logIndex + 1).map((log, i) => (
                    <div key={i} className={`
            mb-1 transition-all duration-300
            ${i === logIndex ? 'text-[#0ea5e9] scale-105 ml-2 font-bold' : 'text-slate-600'}
          `}>
                        {i === logIndex && '> '} {log}
                    </div>
                ))}
            </div>

            {/* Flash Effect on Completion */}
            {logIndex === LOGS.length && (
                <div className="absolute inset-0 bg-white animate-[fadeOut_1s_ease-out_forwards] pointer-events-none" />
            )}
        </div>
    );
};
