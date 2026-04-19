import React, { useState, useEffect } from 'react';
import { SystemWindow } from '../../components/onboarding/SystemWindow';
import { GlowButton } from '../../components/onboarding/GlowButton';

interface TrialQuestScreenProps {
    onNext: () => void;
    playerId: string | null;
}

export const TrialQuestScreen: React.FC<TrialQuestScreenProps> = ({ onNext, playerId }) => {
    // Mock data for now, ideally fetched from /api/onboarding/start
    const [timeLeft, setTimeLeft] = useState(24 * 60 * 60);

    useEffect(() => {
        const timer = setInterval(() => {
            setTimeLeft((prev) => (prev > 0 ? prev - 1 : 0));
        }, 1000);
        return () => clearInterval(timer);
    }, []);

    const formatTime = (seconds: number) => {
        const h = Math.floor(seconds / 3600);
        const m = Math.floor((seconds % 3600) / 60);
        const s = seconds % 60;
        return `${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
    };

    const handleComplete = async () => {
        if (!playerId) {
            console.error('No playerId available for trial completion');
            return;
        }

        try {
            await fetch(`http://localhost:8080/api/onboarding/${playerId}/trial/complete`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' }
            });
            onNext();
        } catch (error) {
            console.error('Failed to complete trial quest:', error);
            // Still proceed to avoid blocking user
            onNext();
        }
    };

    return (
        <div className="flex items-center justify-center min-h-[70vh]">
            <SystemWindow
                title="QUEST INFO"
                subtitle="DIFFICULTY: E-RANK"
                className="max-w-xl w-full"
            >
                <div className="space-y-6">
                    {/* Header Section */}
                    <div className="text-center space-y-2 border-b border-dashed border-slate-700 pb-4">
                        <h1 className="text-2xl font-bold text-amber-500 uppercase glow-text-gold">
                            Courage of the Weak
                        </h1>
                        <p className="text-slate-400 text-sm">
                            The System has detected potential in you. To awaken your true abilities, you must prove your resolve.
                        </p>
                    </div>

                    {/* Goal Section */}
                    <div className="space-y-2">
                        <h3 className="text-[#0ea5e9] font-mono text-xs uppercase">Goal</h3>
                        <div className="bg-slate-950/50 p-4 rounded border border-slate-800">
                            <ul className="space-y-3">
                                <li className="flex items-center gap-3 text-slate-300">
                                    <div className="w-4 h-4 rounded-sm border border-slate-600 bg-transparent flex items-center justify-center">
                                        <div className="w-2 h-2 bg-slate-500 rounded-sm opacity-50"></div>
                                    </div>
                                    <span>Complete 3 focused work sessions (30m each)</span>
                                </li>
                            </ul>
                        </div>
                    </div>

                    {/* Rewards & Failure */}
                    <div className="grid grid-cols-2 gap-4">
                        <div className="space-y-2">
                            <h3 className="text-[#0ea5e9] font-mono text-xs uppercase">Rewards</h3>
                            <div className="bg-slate-950/50 p-3 rounded border border-slate-800 text-sm text-green-400 font-mono">
                                <div>+ Status Recovery</div>
                                <div>+ Player Rights</div>
                            </div>
                        </div>
                        <div className="space-y-2">
                            <h3 className="text-red-500 font-mono text-xs uppercase">Failure</h3>
                            <div className="bg-slate-950/50 p-3 rounded border border-red-900/30 text-sm text-red-500 font-mono">
                                <div>PENALTY ZONE</div>
                                <div>(4 hours survival)</div>
                            </div>
                        </div>
                    </div>

                    {/* Timer Footer */}
                    <div className="pt-4 flex flex-col items-center gap-4">
                        <div className="text-4xl font-mono text-white tracking-widest bg-black/40 px-6 py-2 rounded border border-white/10">
                            {formatTime(timeLeft)}
                        </div>

                        <GlowButton onClick={handleComplete} fullWidth pulsating>
                            CONFIRM COMPLETION
                        </GlowButton>
                    </div>
                </div>
            </SystemWindow>
        </div>
    );
};
