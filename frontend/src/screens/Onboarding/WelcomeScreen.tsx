import React from 'react';
import { SystemWindow } from '../../components/onboarding/SystemWindow';
import { GlowButton } from '../../components/onboarding/GlowButton';

interface WelcomeScreenProps {
    onNext: () => void;
}

export const WelcomeScreen: React.FC<WelcomeScreenProps> = ({ onNext }) => {
    return (
        <div className="flex items-center justify-center min-h-[60vh]">
            <SystemWindow
                title="SYSTEM ALERT"
                subtitle="MSG_ID: 001_AWAKENING"
                className="max-w-xl w-full text-center border-amber-500/50"
                borderColor="border-amber-500/50"
            >
                <div className="space-y-8 py-4">
                    <div className="text-6xl text-amber-500 animate-pulse">
                        !
                    </div>

                    <div className="space-y-4">
                        <h1 className="text-3xl font-bold text-white tracking-widest uppercase glow-text-white">
                            You Have Been Chosen
                        </h1>
                        <p className="text-slate-300 leading-relaxed max-w-md mx-auto">
                            The System has detected latent potential within you.
                            To unlock your true capabilities, you must undergo a qualification trial.
                        </p>
                        <p className="text-slate-400 text-sm italic">
                            "Those who lack the courage to start are destined to remain in the shadows."
                        </p>
                    </div>

                    <div className="flex flex-col gap-4 max-w-xs mx-auto pt-4">
                        <GlowButton onClick={onNext} pulsating>
                            ACCEPT
                        </GlowButton>
                        <GlowButton variant="ghost" disabled className="opacity-50 cursor-not-allowed">
                            DECLINE (LOCKED)
                        </GlowButton>
                    </div>
                </div>
            </SystemWindow>
        </div>
    );
};
