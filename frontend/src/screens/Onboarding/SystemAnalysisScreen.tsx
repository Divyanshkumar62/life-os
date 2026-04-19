import React, { useState } from 'react';
import { SystemWindow } from '../../components/onboarding/SystemWindow';
import { GlowButton } from '../../components/onboarding/GlowButton';

// Thematic questions inspired by the Double Dungeon assessment
const ANALYSIS_QUESTIONS = [
    {
        id: 'struggle',
        text: "WHAT 'MONSTER' STANDS BEFORE YOU?",
        subtext: "Identify the greatest obstacle to your ascent.",
        options: [
            { id: 'burnout', label: 'THE EXHAUSTION (Burnout)' },
            { id: 'procrastination', label: 'THE IDLE (Procrastination)' },
            { id: 'weakness', label: 'THE FRAILTY (Physical Weakness)' },
            { id: 'doubt', label: 'THE SHADOW (Self-Doubt)' }
        ]
    },
    {
        id: 'weapon',
        text: "HOW WILL YOU SURVIVE?",
        subtext: "Choose your primary means of overcoming adversity.",
        options: [
            { id: 'discipline', label: 'UNYIELDING WILL (Discipline)' },
            { id: 'strategy', label: 'CALCULATED MIND (Strategy)' },
            { id: 'strength', label: 'SHEER POWER (Strength)' },
            { id: 'endurance', label: 'UNBROKEN SPIRIT (Endurance)' }
        ]
    },
    {
        id: 'desire',
        text: "WHAT LIES BEYOND THE STRUGGLE?",
        subtext: "Define the reward you seek.",
        options: [
            { id: 'control', label: 'ABSOLUTE CONTROL (Mastery)' },
            { id: 'freedom', label: 'TRUE FREEDOM (Financial/Time)' },
            { id: 'power', label: 'OVERWHELMING POWER (Physical)' },
            { id: 'peace', label: 'INNER SILENCE (Mental)' }
        ]
    }
];

interface SystemAnalysisScreenProps {
    onNext: (data: Record<string, string>) => void;
}

export const SystemAnalysisScreen: React.FC<SystemAnalysisScreenProps> = ({ onNext }) => {
    const [currentStep, setCurrentStep] = useState(0);
    const [answers, setAnswers] = useState<Record<string, string>>({});

    const question = ANALYSIS_QUESTIONS[currentStep];

    const handleSelect = (optionId: string) => {
        // Determine next state immediately or after delay? 
        // Immediate selection for sharper feel
        setAnswers(prev => ({ ...prev, [question.id]: optionId }));
    };

    const handleConfirm = () => {
        if (currentStep < ANALYSIS_QUESTIONS.length - 1) {
            setCurrentStep(prev => prev + 1);
        } else {
            onNext(answers);
        }
    };

    return (
        <div className="flex items-center justify-center min-h-[70vh]">
            <SystemWindow
                title="SYSTEM ANALYSIS"
                subtitle={`PROTOCOL: EVALUATION_${currentStep + 1}/${ANALYSIS_QUESTIONS.length}`}
                className="max-w-3xl w-full border-red-900/50 shadow-[0_0_30px_rgba(120,0,0,0.2)]"
                borderColor="border-red-900/50"
            >
                <div className="space-y-10 py-6">

                    {/* Question Header */}
                    <div className="text-center space-y-2">
                        <h2 className="text-3xl font-bold text-white tracking-widest uppercase drop-shadow-[0_0_10px_rgba(255,0,0,0.5)]">
                            {question.text}
                        </h2>
                        <p className="text-red-400/60 font-mono text-sm uppercase tracking-wide">
               // {question.subtext}
                        </p>
                    </div>

                    {/* Options Grid */}
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        {question.options.map((opt) => {
                            const isSelected = answers[question.id] === opt.id;
                            return (
                                <button
                                    key={opt.id}
                                    onClick={() => handleSelect(opt.id)}
                                    className={`
                    group relative p-6 border transition-all duration-300 overflow-hidden text-left
                    ${isSelected
                                            ? 'bg-red-950/40 border-red-500 text-white shadow-[0_0_20px_rgba(220,38,38,0.3)]'
                                            : 'bg-[#020617] border-red-900/30 text-slate-500 hover:border-red-600/50 hover:text-red-100'}
                  `}
                                >
                                    <div className={`
                    absolute inset-0 bg-gradient-to-r from-red-600/10 to-transparent opacity-0 transition-opacity duration-300
                    ${isSelected ? 'opacity-100' : 'group-hover:opacity-50'}
                  `} />

                                    <span className={`relative z-10 font-bold font-mono text-lg tracking-wider`}>
                                        {opt.label}
                                    </span>
                                </button>
                            );
                        })}
                    </div>

                    {/* Footer Control */}
                    <div className="flex justify-end pt-6 border-t border-red-900/30">
                        <GlowButton
                            onClick={handleConfirm}
                            disabled={!answers[question.id]}
                            className={`
                ${!answers[question.id] ? 'opacity-50 grayscale' : 'shadow-[0_0_20px_rgba(220,38,38,0.4)]'}
              `}
                            variant="danger" // Assuming we might add a red variant, fallback to primary if not existed
                        >
                            Confirm Selection
                        </GlowButton>
                    </div>

                </div>
            </SystemWindow>
        </div>
    );
};
