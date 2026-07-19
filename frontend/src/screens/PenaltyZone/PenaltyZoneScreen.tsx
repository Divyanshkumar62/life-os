import React, { useState, useEffect } from 'react';
import { AnimatePresence } from 'framer-motion';
import { Skull } from 'lucide-react';
import { useSystemContext } from '../../context/SystemContext';
import { PenaltyPopup } from '../../components/features/PenaltyZone/PenaltyPopup';
import { ConfessionForm } from '../../components/features/PenaltyZone/ConfessionForm';
import { LockoutTimer } from '../../components/features/PenaltyZone/LockoutTimer';
import { SurvivalTaskView } from '../../components/features/PenaltyZone/SurvivalTaskView';

interface PenaltyZoneScreenProps {
    playerId: string | null;
}

export const PenaltyZoneScreen: React.FC<PenaltyZoneScreenProps> = ({ playerId }) => {
    const { statusWindow, refreshSystem } = useSystemContext();
    const [step, setStep] = useState<'entry' | 'confession' | 'lockout' | 'survival'>('entry');
    const [submitting, setSubmitting] = useState(false);
    const [localAttempts, setLocalAttempts] = useState<number | null>(null);
    const [localLockoutUntil, setLocalLockoutUntil] = useState<string | null>(null);
    const [timeLeft, setTimeLeft] = useState<number>(0);
    const [survivalQuestId, setSurvivalQuestId] = useState<string | null>(null);

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
                setStep('confession');
                refreshSystem();
            } else {
                setTimeLeft(diff);
            }
        };

        updateTimer();
        const interval = setInterval(updateTimer, 1000);
        return () => clearInterval(interval);
    }, [localLockoutUntil, statusWindow?.temporalState?.penaltyLockoutUntil, refreshSystem]);

    const formatTime = (ms: number) => {
        const totalSecs = Math.floor(ms / 1000);
        const hours = Math.floor(totalSecs / 3600);
        const minutes = Math.floor((totalSecs % 3600) / 60);
        const seconds = totalSecs % 60;
        return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
    };

    const strikesUsed = localAttempts !== null
        ? localAttempts
        : (statusWindow?.temporalState?.failedConfessionAttempts || 0);

    const handleConfessionAccepted = (taskId?: string) => {
        setSurvivalQuestId(taskId || null);
        setStep('survival');
        refreshSystem();
    };

    const handleConfessionRejected = (attemptsRemaining: number, _lockoutUntil?: string) => {
        const attemptsUsed = 3 - attemptsRemaining;
        setLocalAttempts(attemptsUsed);
    };

    const handleLockout = (lockoutUntil: string) => {
        setLocalLockoutUntil(lockoutUntil);
        setStep('lockout');
    };

    const handleSurvivalCompleted = () => {
        refreshSystem();
        window.location.href = '/dashboard';
    };

    return (
        <div className="min-h-screen bg-gradient-to-b from-[#1c0707] via-[#0d0202] to-black text-white flex flex-col font-mono relative overflow-hidden select-none">
            <div className="absolute top-1/4 left-1/2 -translate-x-1/2 w-[500px] h-[500px] bg-red-950/20 rounded-full blur-[120px] pointer-events-none" />

            <div className="flex-1 max-w-3xl w-full mx-auto px-6 py-12 flex flex-col justify-center relative z-10">
                {(step === 'confession' || step === 'lockout' || step === 'survival') && (
                    <div className="border border-solo-red/30 glass-panel p-6 rounded-none text-center shadow-[0_0_30px_rgba(255,0,60,0.15)] relative mb-6">
                        <div className="absolute top-2 right-2 flex items-center gap-1.5 text-[10px] text-solo-red/70">
                            <Skull size={10} className="animate-pulse" />
                            <span>ZONE_ID: PENALTY_01</span>
                        </div>
                        <h1 className="text-3xl md:text-4xl font-black text-solo-red tracking-widest mb-2 uppercase drop-shadow-[0_0_10px_rgba(255,0,60,0.5)]">
                            PENALTY ZONE
                        </h1>
                        <p className="text-[10px] md:text-xs text-gray-400 uppercase tracking-widest leading-relaxed max-w-md mx-auto mt-2">
                            You have failed the daily training requirements. Access to the standard system is restricted.
                        </p>
                    </div>
                )}

                {step === 'confession' && (
                    <div className="border border-dashed border-solo-red/40 bg-solo-red/10 p-4 rounded-none flex flex-col items-center gap-1.5 text-center mb-6">
                        <span className="text-[10px] font-bold text-solo-red/60 uppercase tracking-widest">[ DEATH TOLL ASSESSMENT ]</span>
                        <h2 className="text-sm md:text-base font-black text-solo-red uppercase tracking-widest">
                            PENALTY ASSESSED: -25% EXP, -20% GOLD
                        </h2>
                    </div>
                )}

                <AnimatePresence mode="wait">
                    {step === 'lockout' && (
                        <LockoutTimer key="lockout" timeLeft={timeLeft} formatTime={formatTime} />
                    )}

                    {step === 'confession' && (
                        <ConfessionForm
                            key="confession"
                            playerId={playerId}
                            strikesUsed={strikesUsed}
                            submitting={submitting}
                            setSubmitting={setSubmitting}
                            onAccepted={handleConfessionAccepted}
                            onRejected={handleConfessionRejected}
                            onLockout={handleLockout}
                        />
                    )}

                    {step === 'survival' && (
                        <SurvivalTaskView
                            key="survival"
                            playerId={playerId}
                            questId={survivalQuestId}
                            onCompleted={handleSurvivalCompleted}
                        />
                    )}
                </AnimatePresence>
            </div>

            <PenaltyPopup
                isOpen={step === 'entry'}
                onAccept={() => setStep('confession')}
            />
        </div>
    );
};
