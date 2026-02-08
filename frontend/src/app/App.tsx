import { useState } from 'react';
import { DashboardView } from '../screens/Dashboard/DashboardView';
import { SystemLogView } from '../screens/SystemLog/SystemLogView';
import { DiagnosticView } from '../screens/Diagnostic/DiagnosticView';
import { HunterProfileView } from '../screens/Profile/HunterProfileView';
import { ActiveMissionsView } from '../screens/Missions/ActiveMissionsView';

type Screen = 'dashboard' | 'system_log' | 'diagnostic' | 'profile' | 'missions';

function App() {
    const [currentScreen, setCurrentScreen] = useState<Screen>('dashboard');

    if (currentScreen === 'system_log') {
        return <SystemLogView onBack={() => setCurrentScreen('dashboard')} />;
    }

    if (currentScreen === 'diagnostic') {
        return <DiagnosticView onBack={() => setCurrentScreen('dashboard')} />;
    }

    if (currentScreen === 'profile') {
        return <HunterProfileView onBack={() => setCurrentScreen('dashboard')} />;
    }

    if (currentScreen === 'missions') {
        console.log('Rendering ActiveMissionsView');
        return <ActiveMissionsView onBack={() => setCurrentScreen('dashboard')} />;
    }

    return (
        <DashboardView
            onViewSystemLog={() => setCurrentScreen('system_log')}
            onViewDiagnostic={() => setCurrentScreen('diagnostic')}
            onViewProfile={() => setCurrentScreen('profile')}
            onViewMissions={() => {
                console.log('App: Setting screen to missions');
                setCurrentScreen('missions');
            }}
        />
    );
}

export default App;
