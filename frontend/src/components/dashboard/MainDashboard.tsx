import { useState } from 'react';
import { clsx } from 'clsx';
import { TerminalCard } from '../system/TerminalCard';
import { SystemButton } from '../system/SystemButton';
import { SystemProgressBar } from '../system/SystemProgressBar';

export interface DailyQuest {
    id: string;
    title: string;
    category: string;
    rank: string;
    timer: string;
    xpReward: number;
    completed: boolean;
}

const INITIAL_DAILIES: DailyQuest[] = [
    { id: '1', title: 'SHADOW SPRINT', category: 'PHYSICAL TRAINING', rank: 'B-RANK', timer: '00:45:00', xpReward: 45000, completed: false },
    { id: '2', title: 'VOID STRIKE', category: 'COMBAT MASTERY', rank: 'A-RANK', timer: '01:15:00', xpReward: 82000, completed: false },
    { id: '3', title: 'ABYSSAL FOCUS', category: 'MENTAL FORTITUDE', rank: 'S-RANK', timer: '02:30:00', xpReward: 125000, completed: false },
];

/**
 * MainDashboard - The primary player dashboard HUD interface.
 */
export function MainDashboard() {
    const [dailies, setDailies] = useState<DailyQuest[]>(INITIAL_DAILIES);
    const [gold, setGold] = useState(1250000);
    const [level] = useState(120);

    const toggleComplete = (id: string) => {
        setDailies((prev) =>
            prev.map((quest) => {
                if (quest.id === id) {
                    const nextState = !quest.completed;
                    if (nextState) {
                        setGold((g) => g + 25000);
                    } else {
                        setGold((g) => Math.max(0, g - 25000));
                    }
                    return { ...quest, completed: nextState };
                }
                return quest;
            })
        );
    };

    return (
        <div className="min-h-screen bg-[#05050A] text-[#E2E8F0] pt-20 pb-8 px-6 relative overflow-hidden font-space">
            {/* Background Atmosphere */}
            <div className="absolute inset-0 pointer-events-none opacity-5 bg-repeat bg-striped" />
            <div className="absolute bottom-10 right-10 w-[500px] h-[500px] bg-[#2563EB]/5 blur-[140px] rounded-full pointer-events-none" />

            {/* Warning Pulsating Marquee */}
            <div className="fixed top-0 left-0 w-full z-50 h-16 bg-[#05050A]/90 backdrop-blur-xl border-b border-[#EF4444] shadow-[0_0_15px_rgba(239,68,68,0.3)] flex justify-between items-center px-6">
                <div className="flex items-center gap-4">
                    <span className="text-[24px] tracking-tighter text-[#E2E8F0] uppercase font-bold leading-none">SYSTEM.v01</span>
                    <div className="h-4 w-[1px] bg-gray-800 mx-2" />
                    <div className="bg-[#EF4444]/20 px-3 py-1 flex items-center gap-2 overflow-hidden border-l-2 border-[#EF4444]">
                        <span className="material-symbols-outlined text-[#EF4444] text-sm animate-pulse">warning</span>
                        <span className="font-mono text-xs text-[#EF4444] uppercase tracking-widest font-bold">
                            [WARNING: SYSTEM ANOMALY DETECTED — QUEST RESET IN 02:45:12]
                        </span>
                    </div>
                </div>
                <div className="flex items-center gap-6">
                    <div className="flex items-center gap-3 bg-black/40 px-4 py-1.5 border border-[#2563EB]">
                        <div className="w-2 h-2 rounded-full bg-[#2563EB] animate-pulse shadow-[0_0_8px_#2563EB]" />
                        <span className="font-mono text-[12px] text-[#2563EB]">SYSTEM ONLINE</span>
                    </div>
                </div>
            </div>

            {/* Core Multi-Column Layout */}
            <div className="max-w-7xl mx-auto grid grid-cols-1 lg:grid-cols-4 gap-8 mt-4 text-left">
                {/* Left Attributes & Identity Panel (1 Column) */}
                <aside className="lg:col-span-1 space-y-6">
                    <TerminalCard variant="active" className="p-6">
                        <div className="space-y-6">
                            {/* Avatar / Identity */}
                            <div className="flex items-center gap-4">
                                <div className="w-16 h-16 border border-[#2563EB] shadow-[0_0_10px_rgba(37,99,235,0.2)] flex items-center justify-center bg-black/40">
                                    <span className="text-3xl text-[#2563EB]">👤</span>
                                </div>
                                <div>
                                    <h3 className="text-headline-md leading-none text-[#E2E8F0]">LV. {level}</h3>
                                    <p className="text-xs text-[#2563EB] font-mono tracking-wider mt-1 uppercase">RANK: S-CLASS</p>
                                </div>
                            </div>

                            {/* XP Progress Bar */}
                            <div className="space-y-1">
                                <SystemProgressBar
                                    current={85}
                                    max={100}
                                    label="XP PROGRESS"
                                    color="electric-cyan"
                                    showPercentage
                                />
                                <span className="text-[10px] text-gray-500 font-mono block text-right">LOCKED AT LEVEL BOUNDARY</span>
                            </div>

                            {/* Wallet Panel */}
                            <div className="flex justify-between items-center bg-black/40 p-3 border border-gray-800">
                                <div className="flex items-center gap-2">
                                    <span className="text-[#FBBF24]">💰</span>
                                    <span className="text-stat-value font-mono text-[#FBBF24] glow-text-gold">{gold.toLocaleString()}</span>
                                </div>
                                <span className="text-[10px] text-gray-500 font-mono">GOLD</span>
                            </div>
                        </div>
                    </TerminalCard>

                    {/* Attributes Grid Card */}
                    <TerminalCard variant="default" className="p-6">
                        <h4 className="text-xs text-gray-400 font-bold tracking-widest uppercase mb-4 flex items-center gap-2">
                            <span className="w-1 h-3 bg-[#2563EB]" /> CORE ATTRIBUTES
                        </h4>
                        <div className="space-y-2 font-mono">
                            {[
                                { name: 'STR', val: 245, full: 'Strength' },
                                { name: 'INT', val: 189, full: 'Intelligence' },
                                { name: 'VIT', val: 220, full: 'Vitality' },
                                { name: 'AGI', val: 312, full: 'Agility' },
                                { name: 'SEN', val: 150, full: 'Perception' },
                            ].map((stat) => (
                                <div
                                    key={stat.name}
                                    className="flex justify-between items-center bg-black/20 p-2.5 border-l-2 border-[#2563EB] hover:bg-[#2563EB]/10 transition-colors group cursor-default"
                                >
                                    <span className="text-xs text-gray-400 group-hover:text-[#E2E8F0]">{stat.name} <span className="text-[9px] text-gray-600">({stat.full})</span></span>
                                    <span className="text-sm text-[#E2E8F0] font-bold">{stat.val}</span>
                                </div>
                            ))}
                        </div>
                    </TerminalCard>
                </aside>

                {/* Right/Center Loop Content (3 Columns) */}
                <main className="lg:col-span-3 space-y-8">
                    {/* Dailies Section */}
                    <section className="space-y-6">
                        <div className="flex items-center gap-3">
                            <span className="text-[#2563EB] text-2xl font-mono">&gt;_</span>
                            <h2 className="text-headline-md tracking-widest text-[#E2E8F0] uppercase font-bold">CORE DAILIES</h2>
                            <div className="flex-1 h-[1px] bg-gray-800 opacity-50 ml-4" />
                        </div>

                        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                            {dailies.map((quest, index) => (
                                <TerminalCard
                                    key={quest.id}
                                    variant={quest.completed ? 'success' : 'default'}
                                    className="relative flex flex-col justify-between"
                                >
                                    {/* Number Badge */}
                                    <div className="absolute top-3 right-3 text-xs text-gray-600 font-mono">
                                        {(index + 1).toString().padStart(2, '0')}
                                    </div>

                                    {/* Info */}
                                    <div className="space-y-4 mb-6 text-left">
                                        <div>
                                            <p className="text-[10px] text-[#2563EB] tracking-wider uppercase font-mono">{quest.category}</p>
                                            <h3 className="text-headline-sm text-[#E2E8F0] font-bold uppercase mt-1">{quest.title}</h3>
                                        </div>

                                        <div className="space-y-2 text-xs font-mono">
                                            <div className="flex justify-between">
                                                <span className="text-gray-500">RANK</span>
                                                <span className={clsx(
                                                    'font-bold',
                                                    quest.rank === 'S-RANK' ? 'text-[#FBBF24] glow-text-gold' : 'text-[#E2E8F0]'
                                                )}>
                                                    {quest.rank}
                                                </span>
                                            </div>
                                            <div className="flex justify-between">
                                                <span className="text-gray-500">TIMER</span>
                                                <span className="text-gray-300">{quest.timer}</span>
                                            </div>
                                            <div className="flex justify-between">
                                                <span className="text-[#22D3EE]">XP REWARD</span>
                                                <span className="text-[#22D3EE] font-bold">+{quest.xpReward.toLocaleString()}</span>
                                            </div>
                                        </div>
                                    </div>

                                    {/* Actions */}
                                    <SystemButton
                                        variant={quest.completed ? 'success' : 'primary'}
                                        fullWidth
                                        onClick={() => toggleComplete(quest.id)}
                                    >
                                        {quest.completed ? 'COMPLETED' : 'COMPLETE'}
                                    </SystemButton>
                                </TerminalCard>
                            ))}
                        </div>
                    </section>
                </main>
            </div>
        </div>
    );
}
