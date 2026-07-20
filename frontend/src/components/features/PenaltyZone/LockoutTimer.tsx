import React from 'react';
import { motion } from 'framer-motion';
import { Clock } from 'lucide-react';

interface LockoutTimerProps {
    timeLeft: number;
    formatTime: (ms: number) => string;
}

export const LockoutTimer: React.FC<LockoutTimerProps> = ({ timeLeft, formatTime }) => {
    return (
        <motion.div
            key="lockout"
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            exit={{ opacity: 0, scale: 0.95 }}
            className="border border-solo-red glass-panel p-8 rounded-none text-center flex flex-col items-center gap-6 shadow-[0_0_40px_rgba(255,0,60,0.3)] min-h-[350px] justify-center"
        >
            <div className="w-16 h-16 rounded-full border border-solo-red flex items-center justify-center text-solo-red animate-pulse shadow-glow-red">
                <Clock size={32} />
            </div>
            <div>
                <h3 className="text-lg font-black text-solo-red uppercase tracking-widest mb-2">
                    SYSTEM LOCKOUT ACTIVE
                </h3>
                <p className="text-xs text-gray-500 max-w-sm mx-auto uppercase leading-relaxed font-mono">
                    The Architect rejects your shallow excuses. Submissions are temporarily blocked. Suffer the penalty.
                </p>
            </div>
            <div className="text-4xl md:text-5xl font-black text-solo-red font-mono tracking-widest select-text bg-solo-red/20 px-6 py-3 border border-solo-red/20 rounded-none">
                {formatTime(timeLeft)}
            </div>
        </motion.div>
    );
};
