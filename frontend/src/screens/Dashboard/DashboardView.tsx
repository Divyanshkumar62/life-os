import { ScreenFrame } from '../../components/layout';
import { TopBar } from './TopBar';
import { PlayerProfileCard } from './PlayerProfileCard';
import { CurrentStatusPanel } from './CurrentStatusPanel';
import { AssetsPanel } from './AssetsPanel';
import { DailyQuestsPanel } from './DailyQuestsPanel';
import { SystemLogPanel } from './SystemLogPanel';
import { CapacityPanel } from './CapacityPanel';

/**
 * DashboardView - Main dashboard layout composition
 * 
 * Responsibilities:
 * - Compose all dashboard panels
 * - Handle responsive grid layout
 * - Provide demo data
 */
export function DashboardView() {
    // Mock Data
    const mockQuests = [
        {
            id: '1',
            title: 'Strength Training: Push-ups',
            goal: 'GOAL: 100 REPS',
            current: 45,
            target: 100,
            reward: '+4 STR',
            completed: false,
        },
        {
            id: '2',
            title: 'Curl-ups',
            goal: 'GOAL: 100',
            current: 100,
            target: 100,
            reward: '+3 STR',
            completed: false,
        },
        {
            id: '3',
            title: 'Squats',
            goal: 'GOAL: 100',
            current: 80,
            target: 100,
            reward: '+3 STR',
            completed: false,
        },
        {
            id: '4',
            title: 'Running: 10km',
            goal: 'QUEST COMPLETE',
            current: 10,
            target: 10,
            reward: '+5 AGI',
            completed: true,
        },
    ];

    const mockLogs = [
        { id: '1', timestamp: '10:45 AM', type: 'success' as const, message: 'Daily Quest: Strength Training has been updated.' },
        { id: '2', timestamp: '09:54 AM', type: 'info' as const, message: 'Recovered 500 HP via sleep.' },
        { id: '3', timestamp: 'Yesterday', type: 'warning' as const, message: 'WARNING: Penalty Quest nearing expiration.' },
        { id: '4', timestamp: 'Yesterday', type: 'success' as const, message: 'Gold acquired: 50,000 G' },
    ];

    return (
        <ScreenFrame>
            {/* Top Bar */}
            <TopBar
                userName="SUNG JIN-WOO"
                systemStatus="online"
                diagnosticMode
            />

            {/* Main Grid */}
            <div className="grid grid-cols-1 lg:grid-cols-4 gap-4 mt-4">
                {/* Left Column */}
                <div className="space-y-4">
                    <PlayerProfileCard
                        avatarUrl="https://i.pravatar.cc/150?img=12"
                        rank="S"
                        title="SHADOW MONARCH"
                        fatigue={0}
                    />

                    <AssetsPanel
                        totalGold={1450000}
                        weeklyTrend={12.5}
                        dailyIncome={50}
                        shopStats={1200}
                    />
                </div>

                {/* Center Column (2 cols wide) */}
                <div className="lg:col-span-2 space-y-4">
                    <CurrentStatusPanel
                        level={45}
                        currentXp={35000}
                        maxXp={48000}
                        jobClass="NECROMANCER"
                        strength={205}
                        agility={188}
                        intellect={142}
                    />

                    <DailyQuestsPanel
                        quests={mockQuests}
                        onQuestToggle={(id, completed) => console.log('Toggle quest:', id, completed)}
                        onClaimReward={(id) => console.log('Claim reward:', id)}
                    />
                </div>

                {/* Right Column */}
                <div className="space-y-4">
                    <SystemLogPanel entries={mockLogs} />

                    <CapacityPanel
                        activeQuests={3}
                        maxQuests={6}
                        inventoryLoad={45}
                        equippedSkills={['⚡', '👁️', '🎯']}
                    />
                </div>
            </div>

            {/* Bottom Bar */}
            <div className="mt-4 p-3 border-t border-gray-700 flex justify-between items-center text-xs text-gray-500">
                <div className="flex items-center gap-4">
                    <span className="flex items-center gap-1">
                        <span className="w-2 h-2 bg-success rounded-full animate-pulse-glow" />
                        SERVER CONNECTED
                    </span>
                    <span>LATENCY: 12ms</span>
                </div>
                <span>SYSTEM ARCHITECT // ADMIN ACCESS: RESTRICTED</span>
            </div>
        </ScreenFrame>
    );
}
