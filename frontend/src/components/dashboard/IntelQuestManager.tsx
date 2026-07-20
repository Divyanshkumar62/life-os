import { useEffect, useState } from 'react';
import { TerminalCard } from '../system/TerminalCard';
import { SystemButton } from '../system/SystemButton';
import { clsx } from 'clsx';

export interface IntelQuest {
    id: string;
    title: string;
    description: string;
    statType: string;
    statValue: number;
    xpReward: number;
    initialSeconds: number; // For active countdown
    completed: boolean;
}

const INITIAL_INTELS: IntelQuest[] = [
    {
        id: 'intel-1',
        title: 'NEURAL MAPPING REFINEMENT',
        description: 'Deep scan of cognitive pathways to optimize mana circulation efficiency during high-output combat scenarios.',
        statType: 'INT',
        statValue: 1,
        xpReward: 12500,
        initialSeconds: 2700, // 45 minutes (< 1 Hour, triggers Crimson Red)
        completed: false
    },
    {
        id: 'intel-2',
        title: 'KINETIC OVERLOAD CALIBRATION',
        description: 'Stress testing musculoskeletal structure under 50x gravity simulation to prevent systemic failure during burst mobility.',
        statType: 'STR',
        statValue: 1,
        xpReward: 15800,
        initialSeconds: 8100, // 2h 15m (> 1 Hour, normal style)
        completed: false
    },
    {
        id: 'intel-3',
        title: 'SENSORY ACUITY TUNING',
        description: 'Processing high-frequency atmospheric vibrations to detect stealth anomalies within a 5km radius.',
        statType: 'SEN',
        statValue: 1,
        xpReward: 9200,
        initialSeconds: 300, // 5m (< 1 Hour, triggers Crimson Red)
        completed: false
    }
];

/**
 * IntelQuestManager - Active quest catalog displaying attributes enhancement tasks.
 */
export function IntelQuestManager() {
    const [quests, setQuests] = useState<IntelQuest[]>(INITIAL_INTELS);

    const handleComplete = (id: string) => {
        setQuests((prev) =>
            prev.map((q) => (q.id === id ? { ...q, completed: true } : q))
        );
    };

    return (
        <div className="bg-[#05050A] text-[#E2E8F0] p-6 space-y-6 text-left font-space max-w-4xl mx-auto">
            {/* Section Header */}
            <div className="flex items-center gap-3">
                <span className="text-[#2563EB] text-2xl font-mono">📊</span>
                <h2 className="text-headline-md tracking-widest text-[#E2E8F0] uppercase font-bold">SYSTEM CALIBRATION</h2>
                <div className="flex-1 h-[1px] bg-gray-800 opacity-50 ml-4" />
            </div>

            {/* List Layout */}
            <div className="space-y-4">
                {quests.map((quest) => (
                    <TerminalCard
                        key={quest.id}
                        variant={quest.completed ? 'success' : 'default'}
                        className="flex flex-col md:flex-row items-start md:items-center justify-between p-6 gap-6"
                    >
                        {/* Left Side: Detail & Description */}
                        <div className="flex-1 space-y-3">
                            <div className="flex flex-wrap items-center gap-3">
                                <h3 className="text-headline-sm text-[#E2E8F0] font-bold tracking-wide uppercase">
                                    {quest.title}
                                </h3>
                                <span className="px-2 py-0.5 border border-success/30 bg-success/10 text-success text-[10px] font-mono tracking-widest uppercase">
                                    [+{quest.statValue} {quest.statType}]
                                </span>
                            </div>
                            <p className="text-body-sm text-gray-400 font-sans leading-relaxed max-w-2xl">
                                {quest.description}
                            </p>
                        </div>

                        {/* Center/Right Side: Timer & Rewards & Complete Button */}
                        <div className="flex items-center justify-between md:justify-end w-full md:w-auto gap-8 border-t border-gray-850 md:border-0 pt-4 md:pt-0">
                            {/* Live Timer & Rewards */}
                            <div className="text-left md:text-right font-mono">
                                <span className="block text-[10px] text-gray-500 uppercase tracking-wider">XP REWARD</span>
                                <span className="text-sm text-[#22D3EE] font-bold">+{quest.xpReward.toLocaleString()}</span>
                                <div className="mt-1">
                                    <CountdownTimer initialSeconds={quest.initialSeconds} completed={quest.completed} />
                                </div>
                            </div>

                            {/* Button */}
                            <SystemButton
                                variant={quest.completed ? 'success' : 'primary'}
                                disabled={quest.completed}
                                onClick={() => handleComplete(quest.id)}
                                className="w-32"
                            >
                                {quest.completed ? 'COMPLETE' : 'INVESTIGATE'}
                            </SystemButton>
                        </div>
                    </TerminalCard>
                ))}
            </div>
        </div>
    );
}

interface CountdownTimerProps {
    initialSeconds: number;
    completed: boolean;
}

function CountdownTimer({ initialSeconds, completed }: CountdownTimerProps) {
    const [seconds, setSeconds] = useState(initialSeconds);

    useEffect(() => {
        if (completed) return;
        const interval = setInterval(() => {
            setSeconds((prev) => (prev > 0 ? prev - 1 : 0));
        }, 1000);
        return () => clearInterval(interval);
    }, [completed]);

    if (completed) {
        return <span className="text-xs text-success uppercase tracking-wider">CALIBRATION DONE</span>;
    }

    const h = Math.floor(seconds / 3600).toString().padStart(2, '0');
    const m = Math.floor((seconds % 3600) / 60).toString().padStart(2, '0');
    const s = (seconds % 60).toString().padStart(2, '0');

    const lessThanHour = seconds < 3600;

    return (
        <span className={clsx(
            'text-xs tracking-widest font-bold',
            lessThanHour ? 'text-[#EF4444] animate-pulse shadow-[0_0_8px_rgba(239,68,68,0.2)]' : 'text-gray-400'
        )}>
            TIMER: {h}:{m}:{s}
        </span>
    );
}
