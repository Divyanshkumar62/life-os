import React, { useState } from 'react';
import { OnboardingLayout } from './OnboardingLayout';
import { WelcomeScreen } from './WelcomeScreen';
import { TrialQuestScreen } from './TrialQuestScreen';
import { SystemAnalysisScreen } from './SystemAnalysisScreen';
import { AwakeningScreen } from './AwakeningScreen';
import { LoadingScreen } from './LoadingScreen';

interface OnboardingFlowProps {
    onComplete: (playerId: string) => void;
}

// narrative order: assessment -> awakening -> trial
type Step = 'welcome' | 'analysis' | 'loading' | 'awakening' | 'trial';

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
        // Start Onboarding (Create Player/Identity)
        // In new flow, we just move to analysis. Player creation happens after basic agreement?
        // Let's create the player here to have an ID for tracking answers.
        const username = "Hunter-" + Math.floor(Math.random() * 1000);
        const data = await apiCall(`/start?username=${username}`, 'POST');
        if (data && data.playerId) {
            setPlayerId(data.playerId);
        }
        setStep('analysis');
    };

    const handleAnalysisNext = async (data: Record<string, string>) => {
        // Submit awakening questionnaire
        if (playerId) {
            const payload = {
                archetype: data.archetype || 'BALANCE',
                primaryWeakness: data.struggle || '',
                mainGoal: data.desire || '',
                biggestChallenge: data.struggle || '',
                availableTime: '2-4 hours',
                focusArea: data.weapon || 'Mental'
            };
            await apiCall(`/${playerId}/awakening`, 'POST', payload);
        }
        // Move to Loading (Calculations)
        setStep('loading');
    };

    const handleLoadingComplete = () => {
        // Transformation Complete -> Show Status
        setStep('awakening');
    };

    const handleAwakeningNext = async () => {
        // Stats calibration is now handled within awakening in backend
        // Just proceed to trial
        setStep('trial');
    };

    const handleTrialNext = async () => {
        // Complete Trial
        if (playerId) {
            await apiCall(`/${playerId}/trial/complete`, 'POST');
            onComplete(playerId);
        }
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
                    {step === 'trial' && <TrialQuestScreen onNext={handleTrialNext} playerId={playerId} />}
                </OnboardingLayout>
            )}
        </div>
    );
};
