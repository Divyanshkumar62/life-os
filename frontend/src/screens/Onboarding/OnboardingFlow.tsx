import React, { useState } from 'react';
import { OnboardingLayout } from './OnboardingLayout';
import { WelcomeScreen } from './WelcomeScreen';
import { TrialQuestScreen } from './TrialQuestScreen';
import { SystemAnalysisScreen } from './SystemAnalysisScreen';
import { AwakeningScreen } from './AwakeningScreen';
import { LoadingScreen } from './LoadingScreen';
import { AwakeningPenaltyScreen } from './AwakeningPenaltyScreen';

interface OnboardingFlowProps {
    onComplete: (playerId: string) => void;
}

// narrative order: assessment -> awakening -> trial
type Step = 'welcome' | 'analysis' | 'loading' | 'awakening' | 'trial' | 'trial-penalty';

export const OnboardingFlow: React.FC<OnboardingFlowProps> = ({ onComplete }) => {
    const [step, setStep] = useState<Step>('welcome');
    const [playerId, setPlayerId] = useState<string | null>(null);

    // Helper to store fetch calls
    const apiCall = async (endpoint: string, method: string, body?: any) => {
        try {
            const res = await fetch(`http://localhost:8080/api/onboarding${endpoint}`, {
                method,
                headers: { 'Content-Type': 'application/json' },
                body: body ? JSON.stringify(body) : undefined
            });
            if (!res.ok) throw new Error('API Request Failed');
            return await res.json();
        } catch (err) {
            console.error("Onboarding API Error:", err);
        }
    };

    const handleWelcomeNext = async () => {
        const username = "Hunter-" + Math.floor(Math.random() * 1000);
        const data = await apiCall(`/start?username=${username}`, 'POST');
        if (data && data.playerId) {
            setPlayerId(data.playerId);
        }
        setStep('analysis');
    };

    const handleAnalysisNext = async (data: Record<string, string>) => {
        if (playerId) {
            const payload = {
                biggestChallenge: data.biggestChallenge || '',
                pastFailures: data.pastFailures || '',
                focusArea: data.focusArea || 'Mental',
                sixMonthGoal: data.sixMonthGoal || '',
                availableTime: data.availableTime || '2-4 hours'
            };
            await apiCall(`/${playerId}/awakening`, 'POST', payload);
        }
        setStep('loading');
    };

    const handleLoadingComplete = () => {
        setStep('awakening');
    };

    const handleAwakeningNext = async () => {
        setStep('trial');
    };

    const handleTrialNext = async () => {
        if (playerId) {
            await apiCall(`/${playerId}/trial/complete`, 'POST');
            onComplete(playerId);
        }
    };

    const handleTrialFail = () => {
        setStep('trial-penalty');
    };

    const handlePenaltyCleared = () => {
        setStep('trial');
    };

    const handleResetOnboarding = () => {
        setPlayerId(null);
        setStep('welcome');
    };

    return (
        <div className="onboarding-root">
            {step === 'loading' ? (
                <LoadingScreen onComplete={handleLoadingComplete} />
            ) : (
                <OnboardingLayout>
                    {step === 'welcome' && <WelcomeScreen onNext={handleWelcomeNext} />}
                    {step === 'analysis' && <SystemAnalysisScreen onNext={handleAnalysisNext} />}
                    {step === 'awakening' && <AwakeningScreen onNext={handleAwakeningNext} />}
                    {step === 'trial' && <TrialQuestScreen onNext={handleTrialNext} onFail={handleTrialFail} playerId={playerId} />}
                    {step === 'trial-penalty' && <AwakeningPenaltyScreen playerId={playerId} onCleared={handlePenaltyCleared} onResetOnboarding={handleResetOnboarding} />}
                </OnboardingLayout>
            )}
        </div>
    );
};
