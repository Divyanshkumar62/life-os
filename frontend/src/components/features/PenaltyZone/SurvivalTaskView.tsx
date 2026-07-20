import React, { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { Shield, CheckCircle, RefreshCw, AlertTriangle } from 'lucide-react';
import { PenaltyAPI } from '../../../api/api';

interface SurvivalTask {
    questId: string;
    type: string;
    title: string;
    description: string;
    requiredCount: number;
    completedCount: number;
    progress: number;
    status: string;
}

interface SurvivalTaskViewProps {
    playerId: string | null;
    questId: string | null;
    onCompleted: () => void;
    onBackToConfession?: () => void;
}

export const SurvivalTaskView: React.FC<SurvivalTaskViewProps> = ({ playerId, questId, onCompleted, onBackToConfession }) => {
    const [task, setTask] = useState<SurvivalTask | null>(null);
    const [fetching, setFetching] = useState(true);
    const [reporting, setReporting] = useState(false);
    const [completing, setCompleting] = useState(false);
    const [rerolling, setRerolling] = useState(false);
    const [reportUnits, setReportUnits] = useState(1);
    const [showRerollConfirm, setShowRerollConfirm] = useState(false);

    useEffect(() => {
        fetchTask();
    }, [questId]);

    const fetchTask = async () => {
        if (!playerId) return;
        setFetching(true);
        try {
            const data = await PenaltyAPI.fetchActiveTask(playerId);
            setTask(data);
        } catch {
            console.error('Failed to fetch survival task');
        } finally {
            setFetching(false);
        }
    };

    const handleReportProgress = async () => {
        if (!playerId || !task) return;
        setReporting(true);
        try {
            const updated = await PenaltyAPI.reportTaskProgress(task.questId, playerId, reportUnits);
            setTask(updated);
        } catch {
            console.error('Failed to report progress');
        } finally {
            setReporting(false);
        }
    };

    const handleComplete = async () => {
        if (!playerId || !task) return;
        setCompleting(true);
        try {
            const result = await PenaltyAPI.completeTask(task.questId, playerId);
            setTask(prev => prev ? { ...prev, status: 'COMPLETED', progress: 1.0, completedCount: prev.requiredCount } : prev);
            if (result.escaped) {
                setTimeout(onCompleted, 1500);
            }
        } catch {
            console.error('Failed to complete task');
        } finally {
            setCompleting(false);
        }
    };

    const handleReroll = async () => {
        if (!playerId || !task) return;
        setRerolling(true);
        setShowRerollConfirm(false);
        try {
            const newTask = await PenaltyAPI.rerollTask(task.questId, playerId);
            setTask(newTask);
        } catch {
            console.error('Failed to reroll task');
        } finally {
            setRerolling(false);
        }
    };

    const progressPercent = task ? Math.round(task.progress * 100) : 0;
    const isComplete = task?.status === 'COMPLETED' || progressPercent >= 100;

    if (fetching) {
        return (
            <div className="flex flex-col items-center justify-center min-h-[300px] text-solo-cyan animate-pulse">
                <Shield size={48} className="mb-4" />
                <span className="text-sm tracking-widest">LOADING SURVIVAL TASK...</span>
            </div>
        );
    }

    if (!task) {
        return (
            <div className="border border-dashed border-solo-red/40 p-8 text-center text-gray-500 uppercase tracking-widest font-mono text-xs">
                <AlertTriangle size={24} className="mx-auto mb-2 text-solo-red/60" />
                No active survival task found.
                {onBackToConfession && (
                    <button onClick={onBackToConfession} className="block mx-auto mt-4 text-solo-cyan underline text-[10px]">
                        Return to Confession
                    </button>
                )}
            </div>
        );
    }

    return (
        <motion.div
            data-testid="survival-task"
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            className="border border-solo-red/40 glass-panel p-6 rounded-none flex flex-col gap-5"
        >
            <div className="flex items-start gap-3">
                <div className="p-2 bg-solo-red/20 border border-solo-red/30 rounded-none">
                    <Shield size={20} className="text-solo-red" />
                </div>
                <div className="flex-1">
                    <h3 className="text-sm font-black text-solo-red uppercase tracking-widest mb-1">
                        {task.title}
                    </h3>
                    <span className="text-[10px] text-gray-500 uppercase tracking-widest font-mono">
                        {task.type} · {task.requiredCount} units required
                    </span>
                </div>
                {isComplete && (
                    <span className="text-[10px] font-bold px-2 py-0.5 border border-green-500 text-green-400 uppercase tracking-widest">
                        COMPLETED
                    </span>
                )}
            </div>

            <div className="text-xs text-gray-400 leading-relaxed font-mono border border-gray-800 bg-black/40 p-4 rounded-none">
                {task.description}
            </div>

            <div className="flex flex-col gap-1.5">
                <div className="flex justify-between text-[10px] text-gray-500 uppercase tracking-widest font-mono">
                    <span>PROGRESS: {task.completedCount} / {task.requiredCount}</span>
                    <span>{progressPercent}%</span>
                </div>
                <div className="w-full h-2 bg-gray-900 border border-gray-800 rounded-none overflow-hidden">
                    <motion.div
                        initial={{ width: 0 }}
                        animate={{ width: `${progressPercent}%` }}
                        transition={{ duration: 0.5 }}
                        className={`h-full ${isComplete ? 'bg-green-500' : 'bg-solo-red'}`}
                    />
                </div>
            </div>

            {!isComplete && (
                <div className="flex flex-col gap-3">
                    <div className="flex items-center gap-2">
                        <input
                            type="number"
                            min={1}
                            max={task.requiredCount - task.completedCount}
                            value={reportUnits}
                            onChange={(e) => setReportUnits(Math.max(1, Math.min(Number(e.target.value), task.requiredCount - task.completedCount)))}
                            className="w-16 bg-black border border-gray-800 text-white text-xs font-mono text-center p-1.5 rounded-none focus:border-solo-cyan focus:outline-none"
                        />
                        <button
                            onClick={handleReportProgress}
                            disabled={reporting || task.completedCount >= task.requiredCount}
                            className="flex-1 bg-solo-red/20 hover:bg-solo-red/40 border border-solo-red/50 text-white font-bold py-2 px-4 rounded-none transition-all tracking-widest uppercase text-xs disabled:opacity-50 flex items-center justify-center gap-1.5"
                        >
                            {reporting ? (
                                <div className="w-3 h-3 border-2 border-solo-red border-t-transparent rounded-full animate-spin" />
                            ) : (
                                <>
                                    <CheckCircle size={12} />
                                    <span>Report Progress</span>
                                </>
                            )}
                        </button>
                    </div>

                    <div className="flex gap-2">
                        <button
                            onClick={handleComplete}
                            disabled={completing || task.completedCount < task.requiredCount}
                            className="flex-1 bg-green-900/30 hover:bg-green-800/50 border border-green-500/60 text-green-300 font-bold py-2.5 px-4 rounded-none transition-all tracking-widest uppercase text-xs disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-1.5"
                        >
                            {completing ? (
                                <div className="w-3 h-3 border-2 border-green-500 border-t-transparent rounded-full animate-spin" />
                            ) : (
                                <>
                                    <CheckCircle size={12} />
                                    <span>Complete Task</span>
                                </>
                            )}
                        </button>

                        {!showRerollConfirm ? (
                            <button
                                onClick={() => setShowRerollConfirm(true)}
                                disabled={rerolling}
                                className="px-3 py-2.5 bg-gray-900/50 hover:bg-gray-800/60 border border-gray-700 text-gray-400 font-bold rounded-none transition-all tracking-widest uppercase text-[10px] disabled:opacity-50 flex items-center gap-1.5"
                                title="Request Reroll (Cost: 10% Gold)"
                            >
                                {rerolling ? (
                                    <div className="w-3 h-3 border-2 border-gray-400 border-t-transparent rounded-full animate-spin" />
                                ) : (
                                    <RefreshCw size={12} />
                                )}
                                <span>Reroll</span>
                            </button>
                        ) : (
                            <div className="flex items-center gap-1.5">
                                <span className="text-[9px] text-solo-red/80 uppercase tracking-widest font-mono">10% Gold?</span>
                                <button
                                    onClick={handleReroll}
                                    disabled={rerolling}
                                    className="px-2 py-1 bg-solo-red/30 border border-solo-red text-white text-[9px] font-bold rounded-none uppercase tracking-widest"
                                >
                                    Yes
                                </button>
                                <button
                                    onClick={() => setShowRerollConfirm(false)}
                                    className="px-2 py-1 bg-gray-800 border border-gray-600 text-gray-400 text-[9px] font-bold rounded-none uppercase tracking-widest"
                                >
                                    No
                                </button>
                            </div>
                        )}
                    </div>
                </div>
            )}

            {isComplete && (
                <div className="text-center py-4">
                    <p className="text-green-400 text-xs font-mono uppercase tracking-widest mb-3">
                        Survival task completed. Escaping Penalty Zone...
                    </p>
                    <button
                        onClick={onCompleted}
                        className="bg-green-900/40 hover:bg-green-800/60 border border-green-500/60 text-green-300 font-bold py-2.5 px-6 rounded-none transition-all tracking-widest uppercase text-xs"
                    >
                        Return to Dashboard
                    </button>
                </div>
            )}
        </motion.div>
    );
};
