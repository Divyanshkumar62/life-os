import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { AlertTriangle, Send } from 'lucide-react';
import { PenaltyAPI } from '../../../api/api';

interface ConfessionFormProps {
    playerId: string | null;
    onAccepted: (survivalTaskId?: string) => void;
    onRejected: (attemptsRemaining: number, lockoutUntil?: string) => void;
    onLockout: (lockoutUntil: string) => void;
    strikesUsed: number;
    submitting: boolean;
    setSubmitting: (v: boolean) => void;
}

export const ConfessionForm: React.FC<ConfessionFormProps> = ({
    playerId, onAccepted, onRejected, onLockout, strikesUsed, submitting, setSubmitting
}) => {
    const [confessionText, setConfessionText] = useState('');
    const [feedback, setFeedback] = useState<string | null>(null);
    const [shake, setShake] = useState(false);
    const [showWarning, setShowWarning] = useState(false);

    const getWordCount = (text: string) => {
        if (!text.trim()) return 0;
        return text.trim().split(/\s+/).length;
    };

    const handleTextChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
        const val = e.target.value;
        const words = getWordCount(val);
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
                setTimeout(() => onAccepted(res.survivalTaskId), 1500);
            } else {
                setShake(true);
                setTimeout(() => setShake(false), 500);
                setFeedback(res.feedback);
                setShowWarning(true);
                const remaining = res.attemptsRemaining;
                if (remaining <= 0 && res.lockoutUntil) {
                    onLockout(res.lockoutUntil);
                } else {
                    onRejected(remaining, res.lockoutUntil);
                }
            }
        } catch {
            setFeedback("[SYSTEM] Connection error. Suffer in silence until link re-establishes.");
            setShake(true);
            setTimeout(() => setShake(false), 500);
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <motion.form
            key="confession-form"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onSubmit={handleSubmit}
            className="flex flex-col gap-4"
        >
            <motion.div
                animate={shake ? { x: [-20, 20, -10, 10, 0] } : { x: 0 }}
                transition={shake ? { type: 'spring', stiffness: 700, damping: 10 } : {}}
                className="w-full flex flex-col gap-4"
            >
                <div className="glass-panel border border-gray-800 p-4 rounded-none text-xs text-gray-400 leading-relaxed font-mono">
                    <span className="text-solo-red font-bold uppercase block mb-1">THE ARCHITECT'S DEMAND:</span>
                    Submit a written reflection explaining your failure. The System will analyze your sincerity and plan to improve. Troll responses or shallow excuses will trigger a lockout.
                </div>

                <div className="relative">
                    <textarea
                        value={confessionText}
                        onChange={handleTextChange}
                        disabled={submitting}
                        className="w-full h-48 glass-panel border border-solo-red/20 focus:border-solo-red focus:shadow-[0_0_15px_rgba(255,0,60,0.2)] rounded-none p-4 text-xs md:text-sm text-white font-mono placeholder-solo-red/40 focus:outline-none resize-none transition-smooth disabled:opacity-50"
                        placeholder="Begin your confession... Describe the source of your failure and your plan for tomorrow..."
                    />
                    <div data-testid="confession-word-count" className="absolute bottom-3 right-3 text-[10px] font-mono tracking-widest text-solo-red/60 glass-panel px-2 py-0.5 rounded-none border border-solo-red/10">
                        {getWordCount(confessionText)} / 600 WORDS
                    </div>
                </div>

                <AnimatePresence>
                    {feedback && (
                        <motion.div
                            initial={{ opacity: 0, y: -10 }}
                            animate={{ opacity: 1, y: 0 }}
                            className={`border p-4 rounded text-xs leading-relaxed font-mono ${
                                feedback.includes('accepted') || feedback.includes('restored') || feedback.includes('sincere') || feedback.includes('Sincerity')
                                    ? 'border-green-500 bg-green-950/10 text-green-300'
                                    : 'border-red-500 bg-red-950/15 text-red-400'
                            }`}
                        >
                            <div className="font-bold mb-1 uppercase tracking-wider">
                                {feedback.includes('accepted') || feedback.includes('restored') || feedback.includes('sincere') || feedback.includes('Sincerity')
                                    ? '[SYSTEM RESTORED]'
                                    : '[ARCHITECT DECISION]'}
                            </div>
                            <p>{feedback}</p>
                        </motion.div>
                    )}
                </AnimatePresence>

                {showWarning && strikesUsed > 0 && (
                    <div className="border border-solo-red bg-solo-red/10 p-3 rounded-none flex items-center gap-3 text-solo-red font-bold text-[10px] md:text-xs tracking-widest uppercase animate-pulse">
                        <AlertTriangle size={16} />
                        <span>WARNING: [{strikesUsed}]/3 Attempts Used. Strike 3 triggers a 4-hour system lockout.</span>
                    </div>
                )}

                <button
                    type="submit"
                    disabled={submitting || !confessionText.trim()}
                    className="w-full bg-solo-red/30 hover:bg-solo-red/50 border border-solo-red text-white font-bold py-3.5 px-6 rounded-none transition-all tracking-widest uppercase hover:shadow-[0_0_15px_rgba(255,0,60,0.3)] disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2 text-xs md:text-sm"
                >
                    {submitting ? (
                        <div className="w-4 h-4 border-2 border-solo-red border-t-transparent rounded-full animate-spin" />
                    ) : (
                        <>
                            <Send size={14} />
                            <span>Submit Confession</span>
                        </>
                    )}
                </button>
            </motion.div>
        </motion.form>
    );
};
