import { useState } from 'react';
import { TerminalCard } from '../system/TerminalCard';
import { SystemButton } from '../system/SystemButton';

export interface StatusWindowProps {
    isOpen?: boolean;
    onClose?: () => void;
}

interface Attribute {
    name: string;
    label: string;
    value: number;
    pending: number;
}

/**
 * StatusWindow - The Level-Up Overlay and player attributes allocator window.
 */
export function StatusWindow({ isOpen = true, onClose }: StatusWindowProps) {
    const [level, setLevel] = useState(120);
    const [pointsPool, setPointsPool] = useState(5);
    const [attributes, setAttributes] = useState<Attribute[]>([
        { name: 'STR', label: 'Strength', value: 245, pending: 0 },
        { name: 'AGI', label: 'Agility', value: 312, pending: 0 },
        { name: 'SEN', label: 'Perception', value: 150, pending: 0 },
        { name: 'VIT', label: 'Vitality', value: 220, pending: 0 },
        { name: 'INT', label: 'Intelligence', value: 189, pending: 0 },
    ]);

    if (!isOpen) return null;

    const adjustStat = (name: string, amount: number) => {
        if (amount > 0 && pointsPool <= 0) return; // Pool exhausted
        const attr = attributes.find((a) => a.name === name);
        if (!attr) return;
        if (amount < 0 && attr.pending <= 0) return; // Can't deduct below baseline

        setAttributes((prev) =>
            prev.map((a) => {
                if (a.name === name) {
                    return { ...a, pending: a.pending + amount };
                }
                return a;
            })
        );
        setPointsPool((pool) => pool - amount);
    };

    const handleAllocate = () => {
        // Finalize allocations
        setAttributes((prev) =>
            prev.map((a) => ({
                ...a,
                value: a.value + a.pending,
                pending: 0,
            }))
        );
        // Level up if all pool allocated
        if (pointsPool === 0) {
            setLevel((l) => l + 1);
            setPointsPool(5); // Grant another 5 points
        }
    };

    return (
        <div className="fixed inset-0 z-[200] bg-black/85 backdrop-blur-md flex items-center justify-center p-4 font-space">
            {/* Background Atmosphere */}
            <div className="absolute inset-0 pointer-events-none opacity-5 bg-repeat bg-striped" />

            <TerminalCard
                variant="active"
                className="max-w-xl w-full bg-[#05050A] border-2 border-[#2563EB]/80 text-left p-6 md:p-8"
            >
                <div className="space-y-6">
                    {/* Header */}
                    <div className="flex justify-between items-center border-b border-gray-800 pb-4">
                        <div>
                            <span className="text-[10px] text-[#FBBF24] tracking-[0.25em] font-bold uppercase glow-text-gold">
                                SHADOW STATUS DASHBOARD
                            </span>
                            <h2 className="text-headline-md font-bold tracking-wider text-[#E2E8F0] uppercase">
                                SYSTEM: LEVEL UP
                            </h2>
                        </div>
                        <button
                            className="text-gray-500 hover:text-[#E2E8F0] transition-colors text-xl font-mono"
                            onClick={onClose}
                        >
                            [X]
                        </button>
                    </div>

                    {/* Level Change HUD */}
                    <div className="bg-black/40 border border-gray-800 p-4 flex items-center justify-between text-center font-mono">
                        <div>
                            <span className="block text-[9px] text-gray-500 uppercase">CURRENT</span>
                            <span className="text-headline-sm text-gray-400">LV. {level}</span>
                        </div>
                        <span className="text-[#2563EB] text-xl animate-pulse">➔</span>
                        <div>
                            <span className="block text-[9px] text-[#FBBF24] uppercase glow-text-gold">NEXT</span>
                            <span className="text-headline-sm text-[#FBBF24] font-bold glow-text-gold">
                                LV. {level + 1}
                            </span>
                        </div>
                        <div>
                            <span className="block text-[9px] text-[#22C55E] uppercase">REWARD GOLD</span>
                            <span className="text-xs text-[#22C55E] font-bold">+1,250,000</span>
                        </div>
                    </div>

                    {/* Stat Pool */}
                    <div className="flex justify-between items-center bg-[#2563EB]/10 border border-[#2563EB]/30 px-4 py-2">
                        <span className="text-xs text-on-surface-variant font-bold tracking-widest uppercase">
                            UNALLOCATED POINTS POOL:
                        </span>
                        <span className="text-headline-sm font-mono text-[#2563EB] font-bold">
                            +{pointsPool}
                        </span>
                    </div>

                    {/* Attributes Allocator Rows */}
                    <div className="space-y-3 font-mono">
                        {attributes.map((attr) => (
                            <div
                                key={attr.name}
                                className="flex justify-between items-center bg-black/20 p-3 border-l-2 border-[#2563EB]"
                            >
                                {/* Left Side: Labels & Baseline */}
                                <div>
                                    <span className="text-xs text-[#E2E8F0] font-bold uppercase tracking-wider">
                                        {attr.name} <span className="text-[10px] text-gray-600 font-normal">({attr.label})</span>
                                    </span>
                                    <span className="block text-sm text-gray-400 mt-0.5">
                                        BASE: {attr.value}
                                    </span>
                                </div>

                                {/* Right Side: Allocations controls */}
                                <div className="flex items-center gap-4">
                                    {/* Increment Indicator Badge */}
                                    {attr.pending > 0 && (
                                        <span className="text-[11px] text-[#22C55E] font-bold bg-[#22C55E]/15 border border-[#22C55E]/30 px-2 py-0.5 animate-pulse">
                                            (+{attr.pending})
                                        </span>
                                    )}

                                    {/* Control Switches */}
                                    <div className="flex items-center border border-gray-800">
                                        <button
                                            className="w-8 h-8 flex items-center justify-center bg-black/40 hover:bg-gray-800 text-gray-400 active:text-[#E2E8F0] transition-colors border-r border-gray-800 disabled:opacity-30 disabled:cursor-not-allowed"
                                            disabled={attr.pending <= 0}
                                            onClick={() => adjustStat(attr.name, -1)}
                                        >
                                            -
                                        </button>
                                        <span className="w-12 text-center text-sm font-bold text-[#E2E8F0]">
                                            {attr.value + attr.pending}
                                        </span>
                                        <button
                                            className="w-8 h-8 flex items-center justify-center bg-black/40 hover:bg-gray-800 text-gray-400 active:text-[#E2E8F0] transition-colors disabled:opacity-30 disabled:cursor-not-allowed"
                                            disabled={pointsPool <= 0}
                                            onClick={() => adjustStat(attr.name, 1)}
                                        >
                                            +
                                        </button>
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>

                    {/* Bottom Actions */}
                    <div className="pt-2 border-t border-gray-800 flex justify-end">
                        <SystemButton
                            variant={pointsPool === 0 ? 'success' : 'primary'}
                            size="lg"
                            className="w-full tracking-[0.2em] font-bold text-[#FBBF24] border-[#FBBF24] hover:bg-[#FBBF24]/10 shadow-[0_0_10px_rgba(251,191,36,0.2)] active:shadow-[0_0_15px_rgba(251,191,36,0.4)]"
                            onClick={handleAllocate}
                        >
                            {pointsPool === 0 ? 'CONFIRM CHANGES' : 'ALLOCATE POINTS'}
                        </SystemButton>
                    </div>
                </div>
            </TerminalCard>
        </div>
    );
}
