import React, { useEffect, useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { useRedGateContext } from '../../../context/RedGateContext';
import { useSystemAudio } from '../../../hooks/useSystemAudio';

interface RedGatePopupProps {
    isOpen: boolean;
}

export const RedGatePopup: React.FC<RedGatePopupProps> = ({ isOpen }) => {
    const { redGate, completeRedGate, failRedGate } = useRedGateContext();
    const { playRedGateAlarm } = useSystemAudio();
    const [showCompleteConfirm, setShowCompleteConfirm] = useState(false);
    const [showFailConfirm, setShowFailConfirm] = useState(false);

    useEffect(() => {
        if (isOpen) {
            playRedGateAlarm();
        }
    }, [isOpen, playRedGateAlarm]);

    const formatTime = (seconds: number): string => {
        const hrs = Math.floor(seconds / 3600);
        const mins = Math.floor((seconds % 3600) / 60);
        const secs = seconds % 60;
        return `${hrs.toString().padStart(2, '0')}:${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
    };

    const handleComplete = async () => {
        try {
            await completeRedGate();
            setShowCompleteConfirm(false);
        } catch (err) {
            console.error("Failed to complete Red Gate:", err);
        }
    };

    const handleFail = async () => {
        try {
            await failRedGate();
            setShowFailConfirm(false);
        } catch (err) {
            console.error("Failed to fail Red Gate:", err);
        }
    };

    return (
        <AnimatePresence>
            {isOpen && redGate.isActive && (
                <motion.div
                    initial={{ backdropFilter: "blur(0px)", backgroundColor: "rgba(139,0,0,0)" }}
                    animate={{ backdropFilter: "blur(10px)", backgroundColor: "rgba(139,0,0,0.95)" }}
                    exit={{ backdropFilter: "blur(0px)", backgroundColor: "rgba(139,0,0,0)" }}
                    className="fixed inset-0 flex flex-col items-center justify-center z-50"
                >
                    <motion.div
                        initial={{ scale: 0.8, opacity: 0 }}
                        animate={{ scale: 1, opacity: 1 }}
                        className="bg-black/90 border-2 border-red-600 p-8 rounded-lg max-w-2xl w-full mx-4"
                    >
                        <div className="text-center mb-6">
                            <motion.h2 
                                animate={{ 
                                    textShadow: ["0 0 10px red", "0 0 20px red", "0 0 10px red"]
                                }}
                                transition={{ repeat: Infinity, duration: 1 }}
                                className="text-5xl font-black tracking-[0.3em] text-red-500 mb-2"
                            >
                                RED GATE
                            </motion.h2>
                            <h3 className="text-xl font-bold tracking-widest text-red-300">SEALED REALITY</h3>
                        </div>

                        <div className="mb-6 p-4 bg-red-900/20 rounded border border-red-700">
                            <div className="text-center mb-4">
                                <p className="text-red-200 text-sm mb-2">TEMPORAL COUNTDOWN</p>
                                <motion.div 
                                    animate={{ scale: [1, 1.05, 1] }}
                                    transition={{ repeat: Infinity, duration: 1 }}
                                    className="text-6xl font-mono font-bold text-red-400"
                                >
                                    {redGate.remainingSeconds !== null ? formatTime(redGate.remainingSeconds) : "--:--:--"}
                                </motion.div>
                            </div>
                        </div>

                        {redGate.quest && (
                            <div className="mb-6 p-4 bg-gray-900/50 rounded border border-red-500/50">
                                <h4 className="text-lg font-bold text-red-300 mb-2">{redGate.quest.title}</h4>
                                <p className="text-gray-300 text-sm">{redGate.quest.description}</p>
                                <div className="mt-3 flex gap-2">
                                    <span className="px-2 py-1 bg-red-900/50 text-red-200 text-xs rounded">
                                        {redGate.quest.difficultyTier}
                                    </span>
                                    <span className="px-2 py-1 bg-red-900/50 text-red-200 text-xs rounded">
                                        {redGate.quest.priority}
                                    </span>
                                </div>
                            </div>
                        )}

                        <div className="mb-6 p-3 bg-red-950/30 rounded text-xs text-red-200">
                            <p className="font-bold mb-1">⚠️ WARNING:</p>
                            <p>You cannot leave until the mission is complete or time expires.</p>
                            <p>Failure will result in DOUBLE PENALTY: Streak reset + 10% gold drain.</p>
                        </div>

                        <div className="flex gap-4">
                            {!showCompleteConfirm && !showFailConfirm ? (
                                <>
                                    <button
                                        onClick={() => setShowCompleteConfirm(true)}
                                        className="flex-1 bg-green-900/30 hover:bg-green-600/50 border border-green-500 text-green-100 font-bold py-3 px-4 rounded transition-all tracking-widest uppercase"
                                    >
                                        Mission Complete
                                    </button>
                                    <button
                                        onClick={() => setShowFailConfirm(true)}
                                        className="flex-1 bg-red-900/30 hover:bg-red-600/50 border border-red-500 text-red-100 font-bold py-3 px-4 rounded transition-all tracking-widest uppercase"
                                    >
                                        Abort (Penalty)
                                    </button>
                                </>
                            ) : showCompleteConfirm ? (
                                <>
                                    <button
                                        onClick={handleComplete}
                                        className="flex-1 bg-green-600 hover:bg-green-500 text-white font-bold py-3 px-4 rounded transition-all"
                                    >
                                        CONFIRM COMPLETE
                                    </button>
                                    <button
                                        onClick={() => setShowCompleteConfirm(false)}
                                        className="flex-1 bg-gray-700 hover:bg-gray-600 text-white font-bold py-3 px-4 rounded transition-all"
                                    >
                                        Cancel
                                    </button>
                                </>
                            ) : showFailConfirm ? (
                                <>
                                    <button
                                        onClick={handleFail}
                                        className="flex-1 bg-red-600 hover:bg-red-500 text-white font-bold py-3 px-4 rounded transition-all"
                                    >
                                        CONFIRM ABORT
                                    </button>
                                    <button
                                        onClick={() => setShowFailConfirm(false)}
                                        className="flex-1 bg-gray-700 hover:bg-gray-600 text-white font-bold py-3 px-4 rounded transition-all"
                                    >
                                        Cancel
                                    </button>
                                </>
                            ) : null}
                        </div>
                    </motion.div>
                </motion.div>
            )}
        </AnimatePresence>
    );
};
