import React, { useState } from 'react';
import { SystemWindow } from '../../components/onboarding/SystemWindow';
import { GlowButton } from '../../components/onboarding/GlowButton';

// Simple multi-step form data structure
const QUESTIONS = [
    {
        id: 'role',
        text: 'What is your primary role in life right now?',
        options: ['Developer / Engineer', 'Student', 'Artist / Creative', 'Entrepreneur', 'Manager / Leader', 'Other']
    },
    {
        id: 'goal',
        text: 'What is your main goal for the next 6 months?',
        options: ['Career Advancement', 'Physical Transformation', 'Skill Mastery', 'Mental Resilience', 'Financial Freedom']
    },
    {
        id: 'weakness',
        text: 'What is your biggest weakness?',
        options: ['Procrastination', 'Lack of Focus', 'Burnout / Fatigue', 'Inconsistency', 'Fear of Failure']
    }
];

interface QuestionnaireScreenProps {
    onNext: (data: Record<string, string>) => void;
}

export const QuestionnaireScreen: React.FC<QuestionnaireScreenProps> = ({ onNext }) => {
    const [currentStep, setCurrentStep] = useState(0);
    const [answers, setAnswers] = useState<Record<string, string>>({});

    const question = QUESTIONS[currentStep];
    const progress = ((currentStep + 1) / QUESTIONS.length) * 100;

    const handleSelect = (option: string) => {
        setAnswers(prev => ({ ...prev, [question.id]: option }));
    };

    const handleNext = () => {
        if (currentStep < QUESTIONS.length - 1) {
            setCurrentStep(prev => prev + 1);
        } else {
            // Finish
            onNext(answers);
        }
    };

    const handleBack = () => {
        if (currentStep > 0) {
            setCurrentStep(prev => prev - 1);
        }
    };

    return (
        <div className="flex items-center justify-center min-h-[60vh]">
            <SystemWindow
                title="PLAYER SYNCHRONIZATION"
                subtitle={`SYNC_RATE: ${progress.toFixed(0)}%`}
                className="max-w-2xl w-full"
            >
                <div className="space-y-8">
                    {/* Progress Bar */}
                    <div className="w-full h-1 bg-slate-800 relative overflow-hidden">
                        <div
                            className="absolute top-0 left-0 h-full bg-[#0ea5e9] shadow-[0_0_10px_#0ea5e9] transition-all duration-500"
                            style={{ width: `${progress}%` }}
                        />
                    </div>

                    {/* Question Area */}
                    <div className="min-h-[200px] flex flex-col justify-center space-y-6">
                        <h2 className="text-2xl font-light text-white text-center">
                            {question.text}
                        </h2>

                        <div className="grid grid-cols-2 gap-3">
                            {question.options.map((opt) => (
                                <button
                                    key={opt}
                                    onClick={() => handleSelect(opt)}
                                    className={`
                    p-4 rounded border transition-all text-left font-mono text-sm
                    ${answers[question.id] === opt
                                            ? 'bg-[#0ea5e9]/20 border-[#0ea5e9] text-white shadow-[0_0_15px_rgba(14,165,233,0.2)]'
                                            : 'bg-slate-900/40 border-slate-700 text-slate-400 hover:bg-slate-800 hover:border-slate-500'}
                  `}
                                >
                                    <span className="mr-2 text-slate-600">{'>'}</span>
                                    {opt}
                                </button>
                            ))}
                        </div>
                    </div>

                    {/* Navigation */}
                    <div className="flex justify-between pt-4 border-t border-slate-800">
                        <GlowButton
                            variant="secondary"
                            onClick={handleBack}
                            disabled={currentStep === 0}
                            className={currentStep === 0 ? 'opacity-0 pointer-events-none' : ''}
                        >
                            {'< BACK'}
                        </GlowButton>

                        <GlowButton
                            onClick={handleNext}
                            disabled={!answers[question.id]}
                            className={!answers[question.id] ? 'opacity-50 cursor-not-allowed' : ''}
                        >
                            {currentStep === QUESTIONS.length - 1 ? 'COMPLETE SYNC >' : 'NEXT STEP >'}
                        </GlowButton>
                    </div>
                </div>
            </SystemWindow>
        </div>
    );
};
