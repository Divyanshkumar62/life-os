import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { AlertTriangle, Clock, Skull, Send } from 'lucide-react';
import { PenaltyAPI } from '../../api/api';
import { useSystemContext } from '../../context/SystemContext';

interface PenaltyZoneScreenProps {
    playerId: string | null;
}

export const PenaltyZoneScreen: React.FC<PenaltyZoneScreenProps> = ({ playerId }) => {
    const { statusWindow, refreshSystem } = useSystemContext();
    const [confessionText, setConfessionText] = useState('');
    const [submitting, setSubmitting] = useState(false);
    const [feedback, setFeedback] = useState<string | null>(null);
    const [shake, setShake] = useState(false);
    const [showWarning, setShowWarning] = useState(false);

    // Component-level state overrides for instant responses before next statusWindow poll
    const [localAttempts, setLocalAttempts] = useState<number | null>(null);
    const [localLockoutUntil, setLocalLockoutUntil] = useState<string | null>(null);
    const [timeLeft, setTimeLeft] = useState<number>(0);

    // Track lockout timer
    useEffect(() => {
        const lockoutTimeStr = localLockoutUntil || statusWindow?.temporalState?.penaltyLockoutUntil;
        if (!lockoutTimeStr) {
            setTimeLeft(0);
            return;
        }

        const lockoutTime = new Date(lockoutTimeStr).getTime();
        const updateTimer = () => {
            const now = Date.now();
            const diff = lockoutTime - now;
            if (diff <= 0) {
                setTimeLeft(0);
                setLocalLockoutUntil(null);
                refreshSystem(); // Refresh system status when lockout ends
            } else {
                setTimeLeft(diff);
            }
        };

        updateTimer();
        const interval = setInterval(updateTimer, 1000);
        return () => clearInterval(interval);
    }, [localLockoutUntil, statusWindow?.temporalState?.penaltyLockoutUntil, refreshSystem]);

    // Helpers
    const getWordCount = (text: string) => {
        if (!text.trim()) return 0;
        return text.trim().split(/\s+/).length;
    };

    const handleTextChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
        const val = e.target.value;
        const words = getWordCount(val);
        // If the new input exceeds 600 words, don't update (only allow if it's less or equal)
        if (words <= 600) {
            setConfessionText(val);
        }
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!playerId || submitting || !confessionText.trim()) return;

        setSubmitting(true);
        setFeedback(null);
        setShowWarning(false);

        try {
            const res = await PenaltyAPI.submitConfession(playerId, confessionText);
            
            if (res.accepted) {
                setFeedback(res.feedback);
                setConfessionText('');
                // Wait briefly for user to read success feedback before refresh
                setTimeout(async () => {
                    await refreshSystem();
                }, 1500);
            } else {
                // Trigger screen shake
                setShake(true);
                setTimeout(() => setShake(false), 500);
                
                setFeedback(res.feedback);
                setShowWarning(true);
                
                // Update local state overrides
                const remaining = res.attemptsRemaining;
                const attemptsUsed = 3 - remaining;
                setLocalAttempts(attemptsUsed);
                if (res.lockoutUntil) {
                    setLocalLockoutUntil(res.lockoutUntil);
                }
                
                // Refresh authoritative status window in the background
                refreshSystem();
            }
        } catch (err: any) {
            console.error("Submission error:", err);
            setFeedback("[SYSTEM] Connection error. Suffer in silence until link re-establishes.");
            setShake(true);
            setTimeout(() => setShake(false), 500);
        } finally {
            setSubmitting(false);
        }
    };

    const formatTime = (ms: number) => {
        const totalSecs = Math.floor(ms / 1000);
        const hours = Math.floor(totalSecs / 3600);
        const minutes = Math.floor((totalSecs % 3600) / 60);
        const seconds = totalSecs % 60;
        return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
    };

    // Calculate current strike counter
    const strikesUsed = localAttempts !== null 
        ? localAttempts 
        : (statusWindow?.temporalState?.failedConfessionAttempts || 0);

    const isLockedOut = timeLeft > 0;

    return (
        <div className="min-h-screen bg-gradient-to-b from-[#1c0707] via-[#0d0202] to-black text-white flex flex-col font-mono relative overflow-hidden select-none">
            {/* Ambient Red Glow Effects */}
            <div className="absolute top-1/4 left-1/2 -translate-x-1/2 w-[500px] h-[500px] bg-red-950/20 rounded-full blur-[120px] pointer-events-none" />

            {/* oppresive scrollbar container */}
            <div className="flex-1 max-w-3xl w-full mx-auto px-6 py-12 flex flex-col justify-center relative z-10">
                <motion.div
                    animate={shake ? { x: [0, -15, 15, -15, 15, -10, 10, -5, 5, 0] } : {}}
                    transition={{ duration: 0.5 }}
                    className="w-full flex flex-col gap-6"
                >
                    {/* Header: oppressive Solo Leveling warning panel */}
                    <div className="border border-red-500/30 bg-[#0f0404]/90 p-6 rounded-lg text-center shadow-[0_0_30px_rgba(239,68,68,0.15)] relative">
                        <div className="absolute top-2 right-2 flex items-center gap-1.5 text-[10px] text-red-500/70">
                            <Skull size={10} className="animate-pulse" />
                            <span>ZONE_ID: PENALTY_01</span>
                        </div>
                        <h1 className="text-3xl md:text-4xl font-black text-red-500 tracking-[0.25em] mb-2 uppercase drop-shadow-[0_0_10px_rgba(239,68,68,0.5)]">
                            PENALTY ZONE
                        </h1>
                        <p className="text-[10px] md:text-xs text-gray-400 uppercase tracking-widest leading-relaxed max-w-md mx-auto mt-2">
                            You have failed the daily training requirements. Access to the standard system is restricted.
                        </p>
                    </div>

                    {/* Death Toll Receipt Panel */}
                    <div className="border border-dashed border-red-500/40 bg-red-950/10 p-4 rounded flex flex-col items-center gap-1.5 text-center">
                        <span className="text-[10px] font-bold text-red-500/60 uppercase tracking-[0.2em]">[ DEATH TOLL ASSESSMENT ]</span>
                        <h2 className="text-sm md:text-base font-black text-red-400 uppercase tracking-widest">
                            PENALTY ASSESSED: -25% EXP, -20% GOLD
                        </h2>
                    </div>

                    <AnimatePresence mode="wait">
                        {isLockedOut ? (
                            /* Lockout Screen: massive unskippable countdown */
                            <motion.div
                                key="lockout"
                                initial={{ opacity: 0, scale: 0.95 }}
                                animate={{ opacity: 1, scale: 1 }}
                                exit={{ opacity: 0, scale: 0.95 }}
                                className="border border-red-600 bg-black/95 p-8 rounded-lg text-center flex flex-col items-center gap-6 shadow-[0_0_40px_rgba(239,68,68,0.3)] min-h-[350px] justify-center"
                            >
                                <div className="w-16 h-16 rounded-full border border-red-500 flex items-center justify-center text-red-500 animate-pulse shadow-glow-red">
                                    <Clock size={32} />
                                </div>
                                <div>
                                    <h3 className="text-lg font-black text-red-500 uppercase tracking-[0.2em] mb-2">
                                        SYSTEM LOCKOUT ACTIVE
                                    </h3>
                                    <p className="text-xs text-gray-500 max-w-sm mx-auto uppercase leading-relaxed font-mono">
                                        The Architect rejects your shallow excuses. Submissions are temporarily blocked. Suffer the penalty.
                                    </p>
                                </div>
                                <div className="text-4xl md:text-5xl font-black text-red-500 font-mono tracking-widest select-text bg-red-950/20 px-6 py-3 border border-red-500/20 rounded">
                                    {formatTime(timeLeft)}
                                </div>
                            </motion.div>
                        ) : (
                            /* Confession Input Form */
                            <motion.form
                                key="input-form"
                                initial={{ opacity: 0 }}
                                animate={{ opacity: 1 }}
                                exit={{ opacity: 0 }}
                                onSubmit={handleSubmit}
                                className="flex flex-col gap-4"
                            >
                                {/* Instruction note */}
                                <div className="bg-black/40 border border-gray-800 p-4 rounded text-xs text-gray-400 leading-relaxed font-mono">
                                    <span className="text-red-500 font-bold uppercase block mb-1">THE ARCHITECT'S DEMAND:</span>
                                    Submit a written reflection explaining your failure. The System will analyze your sincerity and plan to improve. Troll responses or shallow excuses will trigger a lockout.
                                </div>

                                {/* Text Area */}
                                <div className="relative">
                                    <textarea
                                        value={confessionText}
                                        onChange={handleTextChange}
                                        disabled={submitting}
                                        className="w-full h-48 bg-black/80 border border-red-500/20 focus:border-red-500 focus:shadow-[0_0_15px_rgba(239,68,68,0.2)] rounded p-4 text-xs md:text-sm text-red-100 font-mono placeholder-red-900/40 focus:outline-none resize-none transition-smooth disabled:opacity-50"
                                        placeholder="Begin your confession... Describe the source of your failure and your plan for tomorrow..."
                                    />
                                    <div className="absolute bottom-3 right-3 text-[10px] font-mono tracking-widest text-red-500/60 bg-black/60 px-2 py-0.5 rounded border border-red-900/10">
                                        {getWordCount(confessionText)} / 600 WORDS
                                    </div>
                                </div>

                                {/* AI feedback display */}
                                {feedback && (
                                    <div className={`border p-4 rounded text-xs leading-relaxed font-mono ${
                                        feedback.includes('accepted') || feedback.includes('restored') || feedback.includes('sincere')
                                            ? 'border-green-500 bg-green-950/10 text-green-300'
                                            : 'border-red-500 bg-red-950/15 text-red-400'
                                    }`}>
                                        <div className="font-bold mb-1 uppercase tracking-wider">
                                            {feedback.includes('accepted') || feedback.includes('restored') || feedback.includes('sincere') ? '[SYSTEM RESTORED]' : '[ARCHITECT DECISION]'}
                                        </div>
                                        <p>{feedback}</p>
                                    </div>
                                )}

                                {/* Strike alert counters (reveal after 1st rejection) */}
                                {showWarning && strikesUsed > 0 && (
                                    <div className="border border-red-500 bg-red-950/10 p-3 rounded flex items-center gap-3 text-red-500 font-bold text-[10px] md:text-xs tracking-widest uppercase animate-pulse">
                                        <AlertTriangle size={16} />
                                        <span>WARNING: [{strikesUsed}]/3 Attempts Used. Strike 3 triggers a 4-hour system lockout.</span>
                                    </div>
                                )}

                                {/* Action Button */}
                                <button
                                    type="submit"
                                    disabled={submitting || !confessionText.trim()}
                                    className="w-full bg-red-950/30 hover:bg-red-600/30 border border-red-500 text-red-100 font-bold py-3.5 px-6 rounded transition-all tracking-[0.2em] uppercase hover:shadow-[0_0_15px_rgba(239,68,68,0.3)] disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2 text-xs md:text-sm"
                                >
                                    {submitting ? (
                                        <div className="w-4 h-4 border-2 border-red-500 border-t-transparent rounded-full animate-spin" />
                                    ) : (
                                        <>
                                            <Send size={14} />
                                            <span>Submit Confession</span>
                                        </>
                                    )}
                                </button>
                            </motion.form>
                        )}
                    </AnimatePresence>
                </motion.div>
            </div>
        </div>
    );
};
