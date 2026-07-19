import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { useRedGateContext } from '../../../context/RedGateContext';
import { JobChangeAPI, systemMessageEmitter } from '../../../api/api';
import { useSystemAudio } from '../../../hooks/useSystemAudio';
import { useSystemContext } from '../../../context/SystemContext';
import { Gauntlet3DayTracker } from './Gauntlet3DayTracker';

interface JobChangePopupProps {
    isOpen: boolean;
}

export const JobChangePopup: React.FC<JobChangePopupProps> = ({ isOpen }) => {
    const { jobChange, acceptJobChange, delayJobChange, checkJobChangeStatus } = useRedGateContext();
    const { playerId, refreshSystem } = useSystemContext();
    const { playSystemAlert } = useSystemAudio();
    const [quests, setQuests] = useState<any[]>([]);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if (isOpen && jobChange.status === 'IN_PROGRESS') {
            loadQuests();
        }
    }, [isOpen, jobChange.status]);

    const loadQuests = async () => {
        try {
            const data = await JobChangeAPI.getQuests(playerId || '');
            setQuests(data);
        } catch (err) {
            console.error("Failed to load Job Change quests:", err);
        }
    };

    const handleAccept = async () => {
        setLoading(true);
        try {
            await acceptJobChange();
            playSystemAlert();
            await refreshSystem();
        } catch (err) {
            console.error("Failed to accept Job Change:", err);
        } finally {
            setLoading(false);
        }
    };

    const handleDelay = async () => {
        setLoading(true);
        try {
            await delayJobChange();
            await refreshSystem();
        } catch (err) {
            console.error("Failed to delay Job Change:", err);
        } finally {
            setLoading(false);
        }
    };

    const handleCompleteQuest = async (questId: string, day: number) => {
        setLoading(true);
        try {
            await JobChangeAPI.completeQuest(questId);
            await loadQuests();
            await checkJobChangeStatus();
            await refreshSystem();

            if (day === 3) {
                const statusData = await JobChangeAPI.getStatus(playerId || '');
                if ((statusData.status === 'COMPLETED' || statusData.status === 'AWAITING_CLASS_SELECTION') && statusData.jobClass) {
                    systemMessageEmitter.emit([
                        `[SYSTEM] Evolution Complete. Class Acquired: ${statusData.jobClass}.`
                    ]);
                }
            }
        } catch (err) {
            console.error("Failed to complete gauntlet quest:", err);
        } finally {
            setLoading(false);
        }
    };

    const getStatusMessage = () => {
        switch (jobChange.status) {
            case 'AWAITING_ACCEPTANCE':
                return "The System demands your evolution. Do you accept the gauntlet?";
            case 'IN_PROGRESS':
                return "You have accepted the Job Change Gauntlet. Complete all 3 days to evolve.";
            case 'AWAITING_CLASS_SELECTION':
                return "Gauntlet complete. Proceed to class selection to finalize your evolution.";
            case 'COOLDOWN':
                return `The gauntlet has broken you. Cooldown active.`;
            case 'COMPLETED':
                return `You have evolved into: ${jobChange.jobClass}!`;
            default:
                return "";
        }
    };

    return (
        <AnimatePresence>
            {isOpen && jobChange.status && jobChange.status !== 'NOT_TRIGGERED' && jobChange.status !== 'COMPLETED' && jobChange.status !== 'AWAITING_CLASS_SELECTION' && (
                <motion.div
                    initial={{ backdropFilter: "blur(0px)" }}
                    animate={{ backdropFilter: "blur(10px)" }}
                    exit={{ backdropFilter: "blur(0px)" }}
                    className="fixed inset-0 flex flex-col items-center justify-center z-50 bg-black/40"
                >
                    <motion.div
                        initial={{ y: "-100vh", opacity: 0 }}
                        animate={{ y: 0, opacity: 1 }}
                        exit={{ y: "-100vh", opacity: 0 }}
                        transition={{ type: "spring", bounce: 0.4 }}
                        className={`bg-black/95 border-2 border-solo-cyan p-8 rounded-none max-w-2xl w-full mx-4 shadow-glow-cyan ${jobChange.status === 'AWAITING_CLASS_SELECTION' ? 'border-yellow-500 shadow-[0_0_30px_rgba(234,179,8,0.3)]' : ''}`}
                    >
                        <div className="text-center mb-6">
                            <motion.h2 
                                className={`text-4xl font-black tracking-[0.3em] text-solo-cyan mb-2`}
                            >
                                JOB CHANGE
                            </motion.h2>
                            <h3 className="text-xl font-bold tracking-widest text-gray-300 uppercase">
                                {jobChange.status === 'COOLDOWN' ? 'COOLDOWN ACTIVE' :
                                 jobChange.status === 'AWAITING_CLASS_SELECTION' ? 'CLASS SELECTION PENDING' :
                                 'QUEST AVAILABLE'}
                            </h3>
                        </div>

                        <div className="mb-6 p-4 bg-gray-900/50 border border-gray-800 rounded-none">
                            <p className="text-gray-300 text-center leading-relaxed font-mono uppercase text-xs tracking-widest">
                                {getStatusMessage()}
                            </p>
                        </div>

                        {jobChange.status === 'AWAITING_ACCEPTANCE' && (
                            <>
                                <div className="mb-6 p-4 bg-gray-950 border border-gray-900 rounded-none text-xs text-gray-400 font-mono tracking-widest">
                                    <p className="font-bold text-solo-cyan mb-2 text-center uppercase">3-DAY GAUNTLET PATH:</p>
                                    <ul className="list-disc list-inside space-y-2">
                                        <li>Day 1: The Endless Swarm (Volume Test - 6-8 tasks)</li>
                                        <li>Day 2: The Royal Guards (Intensity Test - 3 Deep Work tasks)</li>
                                        <li>Day 3: The Blood-Red Commander (Boss Room - Final Trial)</li>
                                    </ul>
                                </div>

                                <div className="flex gap-4">
                                    <button
                                        onClick={handleAccept}
                                        disabled={loading}
                                        className="flex-1 bg-green-950/40 hover:bg-green-900/60 border border-green-500 text-green-300 font-bold py-3 px-4 rounded-none transition-all tracking-[0.2em] uppercase text-xs disabled:opacity-50"
                                    >
                                        Accept
                                    </button>
                                    <button
                                        onClick={handleDelay}
                                        disabled={loading}
                                        className="flex-1 bg-gray-950/40 hover:bg-gray-800/60 border border-gray-600 text-gray-400 font-bold py-3 px-4 rounded-none transition-all tracking-[0.2em] uppercase text-xs disabled:opacity-50"
                                    >
                                        Delay 24h
                                    </button>
                                </div>
                            </>
                        )}

                        {jobChange.status === 'IN_PROGRESS' && (
                            <div className="space-y-4">
                                <Gauntlet3DayTracker
                                    quests={quests}
                                    loading={loading}
                                    onCompleteQuest={handleCompleteQuest}
                                />
                            </div>
                        )}

                        {jobChange.status === 'AWAITING_CLASS_SELECTION' && (
                            <div className="text-center p-4">
                                <p className="text-yellow-400 mb-4 font-mono text-xs tracking-widest uppercase">
                                    All trials complete. Navigate to class selection to claim your evolution.
                                </p>
                            </div>
                        )}

                        {jobChange.status === 'COOLDOWN' && (
                            <div className="text-center p-4">
                                <p className="text-red-400 mb-4 font-mono text-xs tracking-widest uppercase">
                                    You failed the gauntlet. 7-day cooldown active.
                                </p>
                                <p className="text-gray-500 text-[10px] tracking-widest uppercase font-mono">
                                    Return after cooldown expires to try again.
                                </p>
                            </div>
                        )}
                    </motion.div>
                </motion.div>
            )}
        </AnimatePresence>
    );
};
