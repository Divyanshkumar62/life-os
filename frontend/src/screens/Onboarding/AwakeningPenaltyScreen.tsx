import React, { useState, useEffect } from 'react';
import { SystemWindow } from '../../components/onboarding/SystemWindow';
import { GlowButton } from '../../components/onboarding/GlowButton';

interface AwakeningPenaltyScreenProps {
    playerId: string | null;
    onCleared: () => void;
    onResetOnboarding: () => void;
}

export const AwakeningPenaltyScreen: React.FC<AwakeningPenaltyScreenProps> = ({
    playerId,
    onCleared,
    onResetOnboarding
}) => {
    const [task, setTask] = useState<{ title: string; description: string; deadlineAt: string } | null>(null);
    const [timeLeft, setTimeLeft] = useState<number>(3600); // 1 hour default
    const [showConfirmModal, setShowConfirmModal] = useState(false);
    const [confirmText, setConfirmText] = useState('');

    useEffect(() => {
        if (!playerId) return;
        fetch(`http://localhost:8080/api/onboarding/${playerId}/penalty/status`)
            .then(res => res.json())
            .then(data => {
                setTask({
                    title: data.taskTitle,
                    description: data.taskDescription,
                    deadlineAt: data.deadlineAt
                });
                
                const deadline = new Date(data.deadlineAt).getTime();
                const now = new Date().getTime();
                const diff = Math.max(0, Math.floor((deadline - now) / 1000));
                setTimeLeft(diff);
            })
            .catch(err => console.error("Failed to load penalty status:", err));
    }, [playerId]);

    useEffect(() => {
        const timer = setInterval(() => {
            setTimeLeft((prev) => {
                if (prev <= 1) {
                    clearInterval(timer);
                    handleAutoFail();
                    return 0;
                }
                return prev - 1;
            });
        }, 1000);
        return () => clearInterval(timer);
    }, []);

    const handleAutoFail = async () => {
        try {
            await fetch(`http://localhost:8080/api/onboarding/${playerId}/penalty/fail`, {
                method: 'POST'
            });
            alert("PENALTY TIME EXPIRED. Purges initiated. Your character record has been wiped.");
            onResetOnboarding();
        } catch (e) {
            console.error(e);
            onResetOnboarding();
        }
    };

    const handleComplete = async () => {
        try {
            await fetch(`http://localhost:8080/api/onboarding/${playerId}/penalty/complete`, {
                method: 'POST'
            });
            onCleared();
        } catch (e) {
            console.error(e);
            onCleared();
        }
    };

    const handleFailConfirm = async () => {
        if (confirmText !== 'DELETE CHARACTER') {
            alert("Please type 'DELETE CHARACTER' to confirm.");
            return;
        }
        try {
            await fetch(`http://localhost:8080/api/onboarding/${playerId}/penalty/fail`, {
                method: 'POST'
            });
            setShowConfirmModal(false);
            onResetOnboarding();
        } catch (e) {
            console.error(e);
            onResetOnboarding();
        }
    };

    const formatTime = (seconds: number) => {
        const m = Math.floor(seconds / 60);
        const s = seconds % 60;
        return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
    };

    return (
        <div className="flex items-center justify-center min-h-[70vh] relative z-10">
            <SystemWindow
                title="AWAKENING PENALTY"
                subtitle="STATUS: PENALTY ACTIVE"
                className="max-w-xl w-full border-red-500/50 shadow-[0_0_20px_rgba(239,68,68,0.15)]"
            >
                <div className="space-y-6">
                    <div className="text-center space-y-2 border-b border-dashed border-red-500/30 pb-4">
                        <h1 className="text-2xl font-bold text-red-500 uppercase tracking-widest animate-pulse">
                            {task?.title || "PHYSICAL PENALTY"}
                        </h1>
                        <p className="text-slate-400 text-xs font-mono uppercase tracking-wider">
                            You have failed the trial. Compliance is mandatory.
                        </p>
                    </div>

                    <div className="space-y-2">
                        <h3 className="text-red-500 font-mono text-xs uppercase tracking-wider">Penalty Task</h3>
                        <div className="bg-red-950/10 p-4 rounded border border-red-900/30 text-red-400 text-sm leading-relaxed font-mono">
                            {task?.description || "Perform 100 push-ups, 100 sit-ups, and a 10km run within 1 hour."}
                        </div>
                    </div>

                    <div className="pt-4 flex flex-col items-center gap-4">
                        <div className="text-5xl font-mono text-red-500 tracking-[0.2em] bg-red-950/20 px-8 py-3 rounded border border-red-500/30 shadow-[0_0_15px_rgba(239,68,68,0.1)]">
                            {formatTime(timeLeft)}
                        </div>

                        <div className="flex gap-4 w-full mt-4">
                            <GlowButton onClick={handleComplete} fullWidth className="bg-red-500 hover:bg-red-600 border-red-500">
                                CONFIRM CLEAR
                            </GlowButton>
                            
                            <button
                                onClick={() => {
                                    setConfirmText('');
                                    setShowConfirmModal(true);
                                }}
                                className="px-6 py-3 border border-red-600/50 hover:bg-red-950/30 text-red-500 font-mono text-xs uppercase tracking-widest transition-all"
                            >
                                I Failed
                            </button>
                        </div>
                    </div>
                </div>
            </SystemWindow>

            {/* Account Wipe Warning Modal */}
            {showConfirmModal && (
                <div className="fixed inset-0 z-50 bg-black/90 backdrop-blur-md flex items-center justify-center p-4">
                    <div className="bg-slate-950 border-2 border-red-600 p-8 max-w-md w-full rounded-none space-y-6">
                        <h2 className="text-2xl font-bold text-red-500 uppercase tracking-widest font-mono text-center">
                            ⚠️ IMMEDIATE WIPE WARNING
                        </h2>
                        
                        <p className="text-slate-300 text-sm leading-relaxed text-center font-mono">
                            Failing the Awakening Penalty triggers a permanent character purge. Your account database record, progression stats, and items will be cascade-deleted.
                        </p>

                        <div className="space-y-2">
                            <p className="text-red-400 text-xs font-mono uppercase tracking-wider text-center">
                                To confirm deletion, type <span className="font-bold text-white select-all">DELETE CHARACTER</span> below:
                            </p>
                            
                            <input
                                type="text"
                                value={confirmText}
                                onChange={(e) => setConfirmText(e.target.value)}
                                placeholder="TYPE HERE..."
                                className="w-full bg-slate-900 border border-red-500/50 text-white font-mono text-center p-3 focus:outline-none focus:border-red-500 uppercase"
                            />
                        </div>

                        <div className="flex gap-4">
                            <button
                                onClick={handleFailConfirm}
                                className="flex-1 py-3 bg-red-600 hover:bg-red-700 text-white font-mono text-sm uppercase tracking-wider transition-colors disabled:opacity-40 disabled:cursor-not-allowed"
                                disabled={confirmText !== 'DELETE CHARACTER'}
                            >
                                WIPE MY ACCOUNT
                            </button>
                            
                            <button
                                onClick={() => setShowConfirmModal(false)}
                                className="flex-1 py-3 border border-slate-700 hover:border-white text-slate-400 hover:text-white font-mono text-sm uppercase tracking-wider transition-colors"
                            >
                                Cancel
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};
