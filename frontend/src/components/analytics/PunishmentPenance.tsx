import { useState, useEffect } from 'react';
import { TerminalCard } from '../system/TerminalCard';
import { SystemButton } from '../system/SystemButton';
import { clsx } from 'clsx';

export interface PunishmentPenanceProps {
    onTransmitComplete?: () => void;
}

/**
 * PunishmentPenance - Penalty reflection and survival workout screen.
 */
export function PunishmentPenance({ onTransmitComplete }: PunishmentPenanceProps) {
    const [reflection, setReflection] = useState('');
    const [wordCount, setWordCount] = useState(0);
    const [secondsLeft, setSecondsLeft] = useState(14399); // 3h 59m 59s

    // Workout counters
    const [pushups, setPushups] = useState(0);
    const [situps, setSitups] = useState(0);
    const [squats, setSquats] = useState(0);
    const [running, setRunning] = useState(0.0);

    useEffect(() => {
        const interval = setInterval(() => {
            setSecondsLeft((prev) => (prev > 0 ? prev - 1 : 0));
        }, 1000);
        return () => clearInterval(interval);
    }, []);

    const handleTextChange = (text: string) => {
        setReflection(text);
        const trimmed = text.trim();
        const words = trimmed ? trimmed.split(/\s+/).length : 0;
        setWordCount(words);
    };

    const isReflectionValid = wordCount >= 600;
    const isWorkoutValid = pushups >= 100 && situps >= 100 && squats >= 100 && running >= 10.0;
    const isPenanceComplete = isReflectionValid && isWorkoutValid;

    const handleTransmit = () => {
        if (isPenanceComplete) {
            onTransmitComplete?.();
        }
    };

    // Countdown formatting
    const h = Math.floor(secondsLeft / 3600).toString().padStart(2, '0');
    const m = Math.floor((secondsLeft % 3600) / 60).toString().padStart(2, '0');
    const s = (secondsLeft % 60).toString().padStart(2, '0');

    return (
        <div className="min-h-screen bg-[#05050A] text-[#EF4444] p-8 flex flex-col justify-between items-center relative overflow-hidden font-space">
            {/* Background Threat Grid */}
            <div className="absolute inset-0 pointer-events-none opacity-5 bg-repeat bg-striped" />
            <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[700px] h-[700px] bg-[#EF4444]/5 blur-[160px] rounded-full pointer-events-none" />

            {/* Top Bar Warning */}
            <div className="max-w-6xl w-full border-b border-[#EF4444]/30 pb-4 flex justify-between items-center font-mono">
                <span className="text-xs tracking-[0.2em] font-bold">SYSTEM PURGE ACTIVE // LOCKOUT_ZONE</span>
                <span className="text-xs tracking-widest animate-pulse font-bold">● SYSTEM_PENANCE_ENFORCED</span>
            </div>

            {/* Center Grid */}
            <main className="max-w-6xl w-full my-auto grid grid-cols-1 lg:grid-cols-3 gap-8 pt-4 items-stretch">
                {/* Left/Center Columns: Reflection text input (2 Columns) */}
                <div className="lg:col-span-2 flex flex-col gap-4">
                    <TerminalCard
                        variant="alert"
                        className="flex-1 flex flex-col justify-between gap-4 bg-[#05050A]/95 p-6 border-[#EF4444]/40"
                    >
                        <div className="space-y-4 text-left">
                            <div>
                                <span className="text-[9px] text-[#FBBF24] tracking-[0.25em] font-bold uppercase block mb-1">
                                    PENALTY CONSTRAINTS CHECK
                                </span>
                                <h1 className="text-headline-md font-bold tracking-widest text-[#EF4444] uppercase leading-none">
                                    [ SYSTEM PROTOCOL: PENANCE ENFORCED ]
                                </h1>
                            </div>

                            <p className="text-xs text-gray-400 font-sans leading-relaxed">
                                SUBMIT WRITTEN REFLECTION TO RESTORE ROUTING. Text must satisfy the minimum length constraint.
                            </p>

                            {/* Text Input */}
                            <textarea
                                id="penance-input"
                                value={reflection}
                                onChange={(e) => handleTextChange(e.target.value)}
                                placeholder="Enter written reflection describing failure analysis, behavioral correctives, and commitments to the Architect..."
                                className="w-full h-64 bg-black/40 border border-[#EF4444]/30 text-[#E2E8F0] p-4 text-xs font-mono focus:outline-none focus:border-[#EF4444] placeholder:text-[#4B5563]"
                            />
                        </div>

                        {/* Words Counter & Submit */}
                        <div className="flex justify-between items-center border-t border-[#EF4444]/20 pt-4 font-mono text-xs">
                            <span className={clsx(
                                'font-bold uppercase tracking-wider',
                                isReflectionValid ? 'text-[#22C55E]' : 'text-[#EF4444]'
                            )}>
                                {wordCount} / 600 WORDS
                            </span>
                            <SystemButton
                                variant={isPenanceComplete ? 'success' : 'danger'}
                                disabled={!isPenanceComplete}
                                onClick={handleTransmit}
                                className={clsx(
                                    'px-8 py-2.5 tracking-widest border font-bold text-xs',
                                    !isPenanceComplete && 'opacity-30 border-gray-800 text-gray-700 cursor-not-allowed'
                                )}
                            >
                                [ TRANSMIT PENANCE ]
                            </SystemButton>
                        </div>
                    </TerminalCard>
                </div>

                {/* Right Column: Workout checklist panel (1 Column) */}
                <div className="lg:col-span-1 flex flex-col gap-4">
                    <TerminalCard
                        variant="alert"
                        className="flex-1 flex flex-col justify-between p-6 border-[#EF4444]/40 bg-[#05050A]/95 text-left"
                    >
                        <div className="space-y-6">
                            {/* Threat Timer */}
                            <div className="space-y-1 font-mono text-left border-b border-[#EF4444]/20 pb-4">
                                <span className="block text-[9px] text-gray-500 uppercase tracking-widest">TIME REMAINING</span>
                                <span className="text-headline-md text-[#EF4444] font-bold animate-pulse">
                                    TIME REMAINING: {h}:{m}:{s}
                                </span>
                            </div>

                            {/* Tickers */}
                            <div className="space-y-3 font-mono">
                                <span className="block text-[9px] text-gray-500 uppercase tracking-widest">SURVIVAL WORKOUT PARAMETERS</span>

                                {[
                                    { name: 'Push-Ups', val: pushups, set: setPushups, max: 100, unit: 'REPS' },
                                    { name: 'Sit-Ups', val: situps, set: setSitups, max: 100, unit: 'REPS' },
                                    { name: 'Squats', val: squats, set: setSquats, max: 100, unit: 'REPS' },
                                    { name: 'Running', val: running, set: setRunning, max: 10.0, unit: 'KM', step: 1.0 },
                                ].map((item) => {
                                    const isDone = item.val >= item.max;
                                    return (
                                        <div key={item.name} className="bg-black/40 border border-gray-950 p-3 flex justify-between items-center">
                                            <div className="space-y-0.5">
                                                <span className="text-xs text-gray-400 font-bold">{item.name}</span>
                                                <span className={`block text-[11px] font-bold ${isDone ? 'text-[#22C55E]' : 'text-[#EF4444]'}`}>
                                                    {item.val} / {item.max} {item.unit}
                                                </span>
                                            </div>
                                            {/* Click simulation hooks to increment stats */}
                                            <button
                                                className="px-3 py-1 bg-black/80 hover:bg-gray-800 text-[10px] border border-[#EF4444]/30 text-[#EF4444] transition-colors"
                                                onClick={() => item.set((prev) => Math.min(item.max, prev + (item.step || 10)))}
                                            >
                                                +{(item.step || 10)}
                                            </button>
                                        </div>
                                    );
                                })}
                            </div>
                        </div>

                        {/* Footer State */}
                        <div className="border-t border-[#EF4444]/20 pt-4 font-mono text-[10px] text-gray-500 flex justify-between">
                            <span>WORKOUT STATUS:</span>
                            <span className={isWorkoutValid ? 'text-[#22C55E] font-bold' : 'text-[#EF4444]'}>
                                {isWorkoutValid ? 'READY' : 'INCOMPLETE'}
                            </span>
                        </div>
                    </TerminalCard>
                </div>
            </main>

            {/* Bottom Section */}
            <div className="max-w-6xl w-full border-t border-[#EF4444]/30 pt-6 text-center text-data-sm text-gray-600 font-mono">
                CRITICAL_LOCKOUT // TRANS_LINK_DISRUPTED
            </div>
        </div>
    );
}
