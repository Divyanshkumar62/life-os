import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { useRedGateContext } from '../../../context/RedGateContext';
import { JobChangeAPI } from '../../../api/api';
import { useSystemAudio } from '../../../hooks/useSystemAudio';

interface JobChangePopupProps {
    isOpen: boolean;
}

export const JobChangePopup: React.FC<JobChangePopupProps> = ({ isOpen }) => {
    const { jobChange, acceptJobChange, delayJobChange } = useRedGateContext();
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
            const data = await JobChangeAPI.getQuests(jobChange.jobClass || '');
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
        } catch (err) {
            console.error("Failed to delay Job Change:", err);
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
            case 'COOLDOWN':
                return `The gauntlet has broken you. Cooldown until: ${jobChange.cooldownUntil}`;
            case 'COMPLETED':
                return `You have evolved into: ${jobChange.jobClass}!`;
            default:
                return "";
        }
    };

    const getThemeColor = () => {
        if (!jobChange.jobClass) return "purple";
        if (jobChange.jobClass.includes("Silver Knight") || jobChange.jobClass.includes("Berserker")) return "red";
        if (jobChange.jobClass.includes("Arcane Mage") || jobChange.jobClass.includes("Grand Architect")) return "blue";
        return "purple";
    };

    const themeColor = getThemeColor();

    return (
        <AnimatePresence>
            {isOpen && jobChange.status && jobChange.status !== 'NOT_TRIGGERED' && jobChange.status !== 'COMPLETED' && (
                <motion.div
                    initial={{ backdropFilter: "blur(0px)" }}
                    animate={{ backdropFilter: "blur(10px)" }}
                    exit={{ backdropFilter: "blur(0px)" }}
                    className="fixed inset-0 flex flex-col items-center justify-center z-50"
                >
                    <motion.div
                        initial={{ y: "-100vh", opacity: 0 }}
                        animate={{ y: 0, opacity: 1 }}
                        exit={{ y: "-100vh", opacity: 0 }}
                        transition={{ type: "spring", bounce: 0.4 }}
                        className={`bg-black/90 border-2 border-${themeColor}-600 p-8 rounded-lg max-w-2xl w-full mx-4`}
                    >
                        <div className="text-center mb-6">
                            <motion.h2 
                                animate={{ 
                                    textShadow: [`0 0 10px ${themeColor === 'blue' ? '#3b82f6' : themeColor === 'red' ? '#ef4444' : '#a855f7'}`]
                                }}
                                className={`text-4xl font-black tracking-[0.3em] text-${themeColor}-500 mb-2`}
                            >
                                JOB CHANGE
                            </motion.h2>
                            <h3 className="text-xl font-bold tracking-widest text-gray-300">
                                {jobChange.status === 'COOLDOWN' ? 'COOLDOWN ACTIVE' : 'QUEST AVAILABLE'}
                            </h3>
                        </div>

                        <div className="mb-6 p-4 bg-gray-900/50 rounded">
                            <p className="text-gray-300 text-center leading-relaxed">
                                {getStatusMessage()}
                            </p>
                        </div>

                        {jobChange.status === 'AWAITING_ACCEPTANCE' && (
                            <>
                                <div className="mb-6 p-3 bg-gray-800/50 rounded text-xs text-gray-400">
                                    <p className="font-bold mb-2">3-DAY GAUNTLET:</p>
                                    <ul className="list-disc list-inside space-y-1">
                                        <li>Day 1: The Endless Swarm (Volume Test - 6-8 tasks)</li>
                                        <li>Day 2: The Royal Guards (Intensity Test - 3 Deep Work tasks)</li>
                                        <li>Day 3: The Blood-Red Commander (Boss Room)</li>
                                    </ul>
                                </div>

                                <div className="flex gap-4">
                                    <button
                                        onClick={handleAccept}
                                        disabled={loading}
                                        className="flex-1 bg-green-900/30 hover:bg-green-600/50 border border-green-500 text-green-100 font-bold py-3 px-4 rounded transition-all tracking-widest uppercase disabled:opacity-50"
                                    >
                                        Accept
                                    </button>
                                    <button
                                        onClick={handleDelay}
                                        disabled={loading}
                                        className="flex-1 bg-gray-700/50 hover:bg-gray-600/50 border border-gray-500 text-gray-300 font-bold py-3 px-4 rounded transition-all tracking-widest uppercase disabled:opacity-50"
                                    >
                                        Delay 24h
                                    </button>
                                </div>
                            </>
                        )}

                        {jobChange.status === 'IN_PROGRESS' && (
                            <div className="space-y-4">
                                <div className="p-4 bg-gray-800/50 rounded">
                                    <h4 className="font-bold text-gray-200 mb-3">Gauntlet Progress</h4>
                                    {quests.map((quest: any, index: number) => (
                                        <div key={quest.questId || index} className="flex justify-between items-center py-2 border-b border-gray-700 last:border-0">
                                            <span className="text-gray-300">Day {quest.day}: {quest.title}</span>
                                            <span className={`px-2 py-1 text-xs rounded ${
                                                quest.state === 'COMPLETED' ? 'bg-green-900 text-green-200' :
                                                quest.state === 'FAILED' ? 'bg-red-900 text-red-200' :
                                                'bg-yellow-900 text-yellow-200'
                                            }`}>
                                                {quest.state || 'PENDING'}
                                            </span>
                                        </div>
                                    ))}
                                </div>
                                <p className="text-center text-gray-400 text-sm">
                                    Complete all quests in the Job Change screen.
                                </p>
                            </div>
                        )}

                        {jobChange.status === 'COOLDOWN' && (
                            <div className="text-center">
                                <p className="text-red-400 mb-4">
                                    You failed the gauntlet. 7-day cooldown active.
                                </p>
                                <p className="text-gray-400 text-sm">
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
