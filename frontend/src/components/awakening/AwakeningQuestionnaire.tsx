import { useState } from 'react';
import { clsx } from 'clsx';
import { TerminalCard } from '../system/TerminalCard';
import { SystemButton } from '../system/SystemButton';

export interface AwakeningQuestionnaireProps {
    onSubmit?: (data: { weakness: string; timeCommitment: string }) => void;
}

const WEAKNESSES = [
    { key: 'PHYSICAL', title: 'PHYSICAL', desc: 'Focuses on muscle strength, agility, vital stamina, and physical breakdown prevention.' },
    { key: 'INTELLECTUAL', title: 'INTELLECTUAL', desc: 'Prioritizes cognitive sharpening, critical strategy, information defragmentation, and mana efficiency.' },
    { key: 'DISCIPLINE', title: 'DISCIPLINE', desc: 'Enforces routine routines, consistency checkpoints, and penalty evasion protocols.' },
];

const TIME_COMMITMENTS = [
    { key: '0.5H', label: '0.5 HOURS', desc: 'Daily calibration slice for fast-paced players.' },
    { key: '2.0H', label: '2.0 HOURS', desc: 'Standard system alignment. Balances intensity and recovery.' },
    { key: '8.0H', label: '8.0 HOURS', desc: 'Elite commitment. High risk of exhaustion. Purge limits extended.' },
];

/**
 * AwakeningQuestionnaire - Player calibration questionnaire screen.
 */
export function AwakeningQuestionnaire({ onSubmit }: AwakeningQuestionnaireProps) {
    const [selectedWeakness, setSelectedWeakness] = useState<string | null>(null);
    const [selectedTime, setSelectedTime] = useState<string | null>(null);

    const handleFormSubmit = () => {
        if (selectedWeakness && selectedTime) {
            onSubmit?.({
                weakness: selectedWeakness,
                timeCommitment: selectedTime,
            });
        }
    };

    const isFormValid = selectedWeakness !== null && selectedTime !== null;

    return (
        <div className="min-h-screen bg-[#05050A] text-[#E2E8F0] p-8 flex flex-col justify-between relative overflow-hidden font-space">
            {/* Background Atmosphere */}
            <div className="absolute inset-0 pointer-events-none opacity-5 bg-repeat bg-striped" />
            <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-[#2563EB]/5 blur-[120px] rounded-full pointer-events-none" />

            {/* Header */}
            <header className="max-w-4xl mx-auto w-full border-b border-gray-800 pb-4 mb-8 flex justify-between items-center">
                <span className="text-headline-sm text-[#E2E8F0] tracking-wider uppercase font-bold">
                    SYSTEM CALIBRATION // INITIALIZING GROK NEURAL LINK
                </span>
                <span className="text-data-sm text-[#2563EB] font-mono">STEP_01/02</span>
            </header>

            {/* Content Questionnaire Cards */}
            <main className="max-w-4xl mx-auto w-full flex-1 flex flex-col gap-10 justify-center">
                {/* Weakness Section */}
                <section className="space-y-4 text-left">
                    <h2 className="text-xs text-[#2563EB] tracking-[0.2em] font-bold uppercase mb-4 flex items-center gap-2">
                        <span className="w-1 h-3 bg-[#2563EB]" /> [ IDENTIFY PRIMARY SYSTEM WEAKNESS ]
                    </h2>
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                        {WEAKNESSES.map((item) => {
                            const isSelected = selectedWeakness === item.key;
                            return (
                                <TerminalCard
                                    key={item.key}
                                    variant={isSelected ? 'active' : 'default'}
                                    className="cursor-pointer hover:border-[#2563EB] transition-colors p-5 flex flex-col justify-between text-left"
                                    onClick={() => setSelectedWeakness(item.key)}
                                >
                                    <div className="space-y-3">
                                        <h3 className={clsx(
                                            'text-headline-sm uppercase font-bold tracking-widest',
                                            isSelected ? 'text-[#2563EB] glow-text-blue' : 'text-[#E2E8F0]'
                                        )}>
                                            {item.title}
                                        </h3>
                                        <p className="text-body-sm text-gray-400 leading-relaxed font-sans">
                                            {item.desc}
                                        </p>
                                    </div>
                                    <div className="mt-4 flex justify-end">
                                        <span className={clsx(
                                            'text-[10px] font-mono tracking-widest border px-2 py-0.5 uppercase',
                                            isSelected ? 'border-[#2563EB] text-[#2563EB]' : 'border-gray-800 text-gray-600'
                                        )}>
                                            {isSelected ? 'SELECTED' : 'SELECT'}
                                        </span>
                                    </div>
                                </TerminalCard>
                            );
                        })}
                    </div>
                </section>

                {/* Time Commitment Section */}
                <section className="space-y-4 text-left">
                    <h2 className="text-xs text-[#2563EB] tracking-[0.2em] font-bold uppercase mb-4 flex items-center gap-2">
                        <span className="w-1 h-3 bg-[#2563EB]" /> [ ALLOCATE DAILY TIME COMMITMENT ]
                    </h2>
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                        {TIME_COMMITMENTS.map((item) => {
                            const isSelected = selectedTime === item.key;
                            return (
                                <TerminalCard
                                    key={item.key}
                                    variant={isSelected ? 'active' : 'default'}
                                    className="cursor-pointer hover:border-[#2563EB] transition-colors p-5 flex flex-col justify-between text-left"
                                    onClick={() => setSelectedTime(item.key)}
                                >
                                    <div className="space-y-3">
                                        <h3 className={clsx(
                                            'text-headline-sm uppercase font-bold tracking-widest',
                                            isSelected ? 'text-[#2563EB] glow-text-blue' : 'text-[#E2E8F0]'
                                        )}>
                                            {item.label}
                                        </h3>
                                        <p className="text-body-sm text-gray-400 leading-relaxed font-sans">
                                            {item.desc}
                                        </p>
                                    </div>
                                    <div className="mt-4 flex justify-end">
                                        <span className={clsx(
                                            'text-[10px] font-mono tracking-widest border px-2 py-0.5 uppercase',
                                            isSelected ? 'border-[#2563EB] text-[#2563EB]' : 'border-gray-800 text-gray-600'
                                        )}>
                                            {isSelected ? 'SELECTED' : 'SELECT'}
                                        </span>
                                    </div>
                                </TerminalCard>
                            );
                        })}
                    </div>
                </section>
            </main>

            {/* Footer / Submit Actions */}
            <footer className="max-w-4xl mx-auto w-full border-t border-gray-800 pt-6 mt-8 flex flex-col md:flex-row justify-between items-center gap-4">
                <span className="text-data-sm text-gray-500 font-mono">
                    PRESS [ENTER] TO CONFIRM SEQUENCE
                </span>
                <SystemButton
                    variant={isFormValid ? 'success' : 'primary'}
                    disabled={!isFormValid}
                    size="lg"
                    className={clsx(
                        'px-10 py-3 uppercase tracking-[0.2em]',
                        !isFormValid && 'opacity-30 border-gray-700 text-gray-600 cursor-not-allowed'
                    )}
                    onClick={handleFormSubmit}
                >
                    SUBMIT DATA
                </SystemButton>
            </footer>
        </div>
    );
}
