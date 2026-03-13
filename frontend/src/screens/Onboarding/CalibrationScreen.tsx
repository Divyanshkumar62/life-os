import React, { useState } from 'react';
import { SystemWindow } from '../../components/onboarding/SystemWindow';
import { GlowButton } from '../../components/onboarding/GlowButton';
import { StatBar } from '../../components/onboarding/StatBar';

const ATTRIBUTES = ['DISCIPLINE', 'STRENGTH', 'INTELLECT', 'VITALITY', 'SENSE'];

interface CalibrationScreenProps {
    onNext: (stats: Record<string, number>) => void;
}

export const CalibrationScreen: React.FC<CalibrationScreenProps> = ({ onNext }) => {
    const [stats, setStats] = useState<Record<string, number>>({
        'DISCIPLINE': 5,
        'STRENGTH': 5,
        'INTELLECT': 5,
        'VITALITY': 5,
        'SENSE': 5
    });

    const handleChange = (key: string, val: number) => {
        setStats(prev => ({ ...prev, [key]: val }));
    };

    const calculateTotal = () => Object.values(stats).reduce((a, b) => a + b, 0);

    const handleComplete = () => {
        onNext(stats);
    };

    return (
        <div className="flex items-center justify-center min-h-[70vh] gap-8">
            {/* Left: Avatar / Silhouette Placeholder */}
            <div className="hidden md:flex flex-col items-center justify-center w-64 h-96 border border-slate-700 bg-slate-900/50 backdrop-blur rounded relative overflow-hidden">
                <div className="absolute inset-0 bg-[radial-gradient(circle_at_center,_var(--tw-gradient-stops))] from-blue-900/20 to-transparent opacity-50" />
                <div className="text-slate-600 font-mono text-xs mb-4">AVATAR_PREVIEW</div>
                <div className="w-32 h-64 bg-slate-800 rounded-full opacity-20 blur-sm animate-pulse" />
                <div className="absolute bottom-4 text-center w-full">
                    <div className="text-[#0ea5e9] font-bold text-xl">LVL 1</div>
                    <div className="text-slate-500 text-xs tracking-widest">PLAYER</div>
                </div>
            </div>

            {/* Right: Stats Panel */}
            <SystemWindow
                title="STATUS"
                subtitle="ATTRIBUTE_CALIBRATION"
                className="max-w-lg w-full"
            >
                <div className="space-y-6">
                    <div className="flex justify-between items-end border-b border-slate-700 pb-2">
                        <div className="text-sm text-slate-400">
                            Set your current estimated capability levels.
                            <br />
                            <span className="text-xs italic opacity-70">1 = Weak, 5 = Average, 10 = Strong</span>
                        </div>
                        <div className="text-right">
                            <div className="text-xs text-slate-500 uppercase">Total Power</div>
                            <div className="text-xl font-mono text-[#0ea5e9]">{calculateTotal()}</div>
                        </div>
                    </div>

                    <div className="space-y-2">
                        {ATTRIBUTES.map(attr => (
                            <StatBar
                                key={attr}
                                label={attr.substring(0, 3)}
                                value={stats[attr]}
                                maxValue={10}
                                onChange={(val) => handleChange(attr, val)}
                            />
                        ))}
                    </div>

                    <div className="pt-6 border-t border-slate-800">
                        <GlowButton onClick={handleComplete} fullWidth pulsating variant="primary">
                            FINALIZE AWAKENING
                        </GlowButton>
                    </div>
                </div>
            </SystemWindow>
        </div>
    );
};
