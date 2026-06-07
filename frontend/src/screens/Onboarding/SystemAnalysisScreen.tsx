import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { SystemWindow } from '../../components/onboarding/SystemWindow';
import { GlowButton } from '../../components/onboarding/GlowButton';

const PHASES = [
    {
        title: "WORSHIP THE LORD",
        subtitle: "PROTOCOL: CONFRONTING REALITY",
        questions: [
            {
                id: 'biggestChallenge',
                text: "WHAT IS THE NAME OF THE BEAST THAT HAUNTS YOU?",
                subtext: "Identify your greatest daily struggle, bad habit, or obstacle.",
                type: 'textarea',
                placeholder: "Describe the beast you must conquer..."
            },
            {
                id: 'pastFailures',
                text: "LOOK AT THE BONES OF YOUR PAST ATTEMPTS. WHY DID YOU FALL BEFORE REACHING THE END?",
                subtext: "What caused you to abandon your self-improvement goals before?",
                type: 'textarea',
                placeholder: "Why did you lose resolve in the past? Be honest..."
            }
        ]
    },
    {
        title: "PRAISE THE LORD",
        subtitle: "PROTOCOL: IDENTIFYING STRENGTH",
        questions: [
            {
                id: 'focusArea',
                text: "IF YOU STRIP AWAY ALL MAGIC AND ARMOR, WHAT IS YOUR INNATE WEAPON?",
                subtext: "Select your core strength or focus area.",
                type: 'select',
                options: [
                    { id: 'Physical', label: 'PHYSICAL STRENGTH', desc: 'Endurance, raw power, physical conditioning' },
                    { id: 'Mental', label: 'MENTAL SHARPNESS', desc: 'Discipline, calculated strategy, focus' },
                    { id: 'Career', label: 'RELENTLESS AMBITION', desc: 'Career growth, coding mastery, academic/skills excellence' }
                ]
            }
        ]
    },
    {
        title: "PROVE YOUR FAITH",
        subtitle: "PROTOCOL: COMMITMENT",
        questions: [
            {
                id: 'sixMonthGoal',
                text: "WHAT CROWN DO YOU INTEND TO CLAIM BEFORE THIS CYCLE ENDS?",
                subtext: "State your ultimate 6-month goal.",
                type: 'textarea',
                placeholder: "What is your primary milestone 6 months from now?..."
            },
            {
                id: 'availableTime',
                text: "THE DUNGEON DOES NOT WAIT. HOW MANY HOURS A DAY CAN YOU BLEED FOR THIS?",
                subtext: "Select your daily time allocation for growth.",
                type: 'select',
                options: [
                    { id: '1-2 hours', label: '1-2 HOURS', desc: 'Minimum daily commitment' },
                    { id: '2-4 hours', label: '2-4 HOURS', desc: 'Standard daily commitment' },
                    { id: '4+ hours', label: '4+ HOURS', desc: 'Sovereign daily commitment' }
                ]
            }
        ]
    }
];

interface SystemAnalysisScreenProps {
    onNext: (data: Record<string, string>) => void;
}

export const SystemAnalysisScreen: React.FC<SystemAnalysisScreenProps> = ({ onNext }) => {
    const [currentStep, setCurrentStep] = useState(0);
    const [answers, setAnswers] = useState<Record<string, string>>({});

    const phase = PHASES[currentStep];

    const handleTextChange = (id: string, value: string) => {
        setAnswers(prev => ({ ...prev, [id]: value }));
    };

    const handleSelect = (id: string, optionId: string) => {
        setAnswers(prev => ({ ...prev, [id]: optionId }));
    };

    const handleConfirm = () => {
        if (currentStep < PHASES.length - 1) {
            setCurrentStep(prev => prev + 1);
        } else {
            onNext(answers);
        }
    };

    const handleBack = () => {
        if (currentStep > 0) {
            setCurrentStep(prev => prev - 1);
        }
    };

    const isPhaseValid = phase.questions.every(q => {
        const answer = answers[q.id] || '';
        return answer.trim().length > 0;
    });

    return (
        <div className="flex items-center justify-center min-h-[70vh]">
            <SystemWindow
                title={phase.title}
                subtitle={phase.subtitle}
                className="max-w-3xl w-full border-red-900/50 shadow-[0_0_30px_rgba(120,0,0,0.2)] bg-slate-950/90"
                borderColor="border-red-900/50"
            >
                <div className="flex flex-col min-h-[400px]">
                    <div className="flex-1 space-y-12 py-6 overflow-y-auto pr-2 custom-scrollbar">
                        <AnimatePresence mode="wait">
                            <motion.div
                                key={currentStep}
                                initial={{ opacity: 0, x: 20 }}
                                animate={{ opacity: 1, x: 0 }}
                                exit={{ opacity: 0, x: -20 }}
                                transition={{ duration: 0.3 }}
                                className="space-y-12"
                            >
                                {phase.questions.map((question) => (
                                    <div key={question.id} className="space-y-6">
                                        {/* Question Header */}
                                        <div className="text-center space-y-2">
                                            <h2 className="text-2xl md:text-3xl font-black text-white tracking-widest uppercase drop-shadow-[0_0_10px_rgba(255,0,0,0.5)]">
                                                {question.text}
                                            </h2>
                                            <p className="text-red-400/80 font-mono text-xs md:text-sm uppercase tracking-wider">
                                                {question.subtext}
                                            </p>
                                        </div>

                                        {/* Input Area */}
                                        <div className="flex items-center justify-center">
                                            {question.type === 'textarea' ? (
                                                <textarea
                                                    value={answers[question.id] || ''}
                                                    onChange={(e) => handleTextChange(question.id, e.target.value)}
                                                    placeholder={question.placeholder}
                                                    className="w-full h-32 p-4 bg-slate-950/80 border border-red-900/40 text-white font-mono text-sm placeholder-red-950/60 focus:outline-none focus:border-red-500 focus:shadow-[0_0_15px_rgba(239,68,68,0.2)] rounded transition-all resize-none"
                                                />
                                            ) : (
                                                <div className="grid grid-cols-1 md:grid-cols-3 gap-4 w-full">
                                                    {question.options?.map((opt) => {
                                                        const isSelected = answers[question.id] === opt.id;
                                                        return (
                                                            <button
                                                                key={opt.id}
                                                                onClick={() => handleSelect(question.id, opt.id)}
                                                                className={`
                                                                    group relative p-5 border transition-all duration-300 overflow-hidden text-left flex flex-col justify-between rounded
                                                                    ${isSelected
                                                                        ? 'bg-red-950/40 border-red-500 text-white shadow-[0_0_20px_rgba(220,38,38,0.3)]'
                                                                        : 'bg-slate-950 border-red-900/30 text-slate-400 hover:border-red-600/50 hover:text-red-100'}
                                                                `}
                                                            >
                                                                <div className={`
                                                                    absolute inset-0 bg-gradient-to-r from-red-600/10 to-transparent opacity-0 transition-opacity duration-300
                                                                    ${isSelected ? 'opacity-100' : 'group-hover:opacity-50'}
                                                                `} />

                                                                <div className="relative z-10 space-y-2">
                                                                    <div className="font-black font-mono text-base tracking-wider">
                                                                        {opt.label}
                                                                    </div>
                                                                    <div className="text-xs font-mono text-slate-500 group-hover:text-slate-400 transition-colors">
                                                                        {opt.desc}
                                                                    </div>
                                                                </div>
                                                            </button>
                                                        );
                                                    })}
                                                </div>
                                            )}
                                        </div>
                                    </div>
                                ))}
                            </motion.div>
                        </AnimatePresence>
                    </div>

                    {/* Footer Controls */}
                    <div className="flex justify-between items-center pt-6 mt-6 border-t border-red-900/30">
                        <button
                            onClick={handleBack}
                            disabled={currentStep === 0}
                            className={`
                                font-mono text-sm font-bold uppercase tracking-widest px-4 py-2 border transition-all duration-300 rounded
                                ${currentStep === 0
                                    ? 'border-slate-800 text-slate-700 cursor-not-allowed'
                                    : 'border-red-900/50 text-red-500 hover:bg-red-950/20 hover:border-red-500'}
                            `}
                        >
                            Back
                        </button>
                        
                        <GlowButton
                            onClick={handleConfirm}
                            disabled={!isPhaseValid}
                            className={`
                                ${!isPhaseValid ? 'opacity-50 grayscale cursor-not-allowed' : 'shadow-[0_0_20px_rgba(220,38,38,0.4)]'}
                            `}
                            variant="danger"
                        >
                            {currentStep === PHASES.length - 1 ? "Prove your Faith" : "Next Phase"}
                        </GlowButton>
                    </div>

                </div>
            </SystemWindow>
        </div>
    );
};
