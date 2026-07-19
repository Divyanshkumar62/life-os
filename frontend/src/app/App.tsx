import { useState } from 'react';
import { SystemInitialization } from '../components/awakening/SystemInitialization';
import { AwakeningQuestionnaire } from '../components/awakening/AwakeningQuestionnaire';
import { ArchitectsTrial } from '../components/awakening/ArchitectsTrial';
import { MainDashboard } from '../components/dashboard/MainDashboard';
import { IntelQuestManager } from '../components/dashboard/IntelQuestManager';
import { StatusWindow } from '../components/dashboard/StatusWindow';
import { GateHub, DungeonBreakAlert } from '../components/gates';
import { ClassSelection } from '../components/evolution/ClassSelection';
import { TelemetryHub, PunishmentPenance } from '../components/analytics';
import { SystemLayout } from '../components/layout/SystemLayout';
import { SystemButton } from '../components/system/SystemButton';

/**
 * App - The central lifecycle phase router and application controller.
 */
function App() {
    // Primary application lifecycles
    const [isAwakened, setIsAwakened] = useState(false);
    const [isPunished, setIsPunished] = useState(false);
    const [activeRoute, setActiveRoute] = useState('dashboard');

    // Onboarding sub-stages: 1: Initialization, 2: Questionnaire, 3: ArchitectsTrial
    const [onboardingStage, setOnboardingStage] = useState(1);

    // Overlay triggers
    const [isStatusOpen, setIsStatusOpen] = useState(false);
    const [isDungeonBreakActive, setIsDungeonBreakActive] = useState(false);

    // 1. Punishment takeover is active
    if (isPunished) {
        return (
            <PunishmentPenance
                onTransmitComplete={() => {
                    setIsPunished(false);
                }}
            />
        );
    }

    // 2. Awakening / Onboarding takeover is active
    if (!isAwakened) {
        if (onboardingStage === 1) {
            return (
                <SystemInitialization
                    onComplete={() => setOnboardingStage(2)}
                />
            );
        }
        if (onboardingStage === 2) {
            return (
                <AwakeningQuestionnaire
                    onSubmit={(data) => {
                        console.log('Calibrated data:', data);
                        setOnboardingStage(3);
                    }}
                />
            );
        }
        if (onboardingStage === 3) {
            return (
                <ArchitectsTrial
                    onAccept={() => {
                        setIsAwakened(true);
                        setActiveRoute('dashboard');
                    }}
                    onDecline={() => {
                        // Expelled / Reset
                        setOnboardingStage(1);
                    }}
                />
            );
        }
    }

    // 3. Regular active gameplay with persistent HUD Navigation
    const renderActiveView = () => {
        switch (activeRoute) {
            case 'intel':
                return <IntelQuestManager />;
            case 'gates':
                return isDungeonBreakActive ? (
                    <DungeonBreakAlert
                        onForceClose={() => setIsDungeonBreakActive(false)}
                    />
                ) : (
                    <div className="space-y-4">
                        <div className="flex justify-end px-6 max-w-6xl mx-auto">
                            <SystemButton
                                variant="danger"
                                className="px-4 py-2 text-xs"
                                onClick={() => setIsDungeonBreakActive(true)}
                            >
                                TRIGGER DUNGEON BREAK
                            </SystemButton>
                        </div>
                        <GateHub />
                    </div>
                );
            case 'evolution':
                return (
                    <ClassSelection
                        onSelectClass={(job) => {
                            console.log('Evolved Class Selected:', job);
                            alert(`Class Changed: ${job}`);
                        }}
                    />
                );
            case 'analytics':
                return <TelemetryHub />;
            case 'dashboard':
            default:
                return (
                    <div className="space-y-6">
                        {/* Allocate stats trigger */}
                        <div className="flex justify-end max-w-4xl mx-auto">
                            <SystemButton
                                variant="primary"
                                className="px-6 py-2 border-[#FBBF24] text-[#FBBF24] hover:bg-[#FBBF24]/10 shadow-[0_0_10px_rgba(251,191,36,0.15)]"
                                onClick={() => setIsStatusOpen(true)}
                            >
                                OPEN STATUS WINDOW
                            </SystemButton>
                        </div>
                        <MainDashboard />
                        <StatusWindow
                            isOpen={isStatusOpen}
                            onClose={() => setIsStatusOpen(false)}
                        />
                    </div>
                );
        }
    };

    return (
        <SystemLayout
            activeRoute={activeRoute}
            setActiveRoute={setActiveRoute}
            onTriggerPunish={() => setIsPunished(true)}
        >
            {renderActiveView()}
        </SystemLayout>
    );
}

export default App;
