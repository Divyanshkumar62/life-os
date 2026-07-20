import { useState } from 'react';
import { TerminalCard } from '../system/TerminalCard';
import { SystemButton } from '../system/SystemButton';
import { SystemProgressBar } from '../system/SystemProgressBar';

export interface DungeonGate {
    id: string;
    code: string;
    rank: 'C' | 'B' | 'A' | 'S';
    clearsRequired: number;
    clearsCompleted: number;
    unlocked: boolean;
    activeFloor: number;
    totalFloors: number;
}

const INITIAL_GATES: DungeonGate[] = [
    { id: 'gate-1', code: '#0690', rank: 'C', clearsRequired: 5, clearsCompleted: 5, unlocked: true, activeFloor: 4, totalFloors: 5 },
    { id: 'gate-2', code: '#0842', rank: 'B', clearsRequired: 10, clearsCompleted: 7, unlocked: true, activeFloor: 2, totalFloors: 10 },
    { id: 'gate-3', code: '#0981', rank: 'A', clearsRequired: 15, clearsCompleted: 0, unlocked: false, activeFloor: 1, totalFloors: 15 },
    { id: 'gate-4', code: '#0711', rank: 'S', clearsRequired: 20, clearsCompleted: 0, unlocked: false, activeFloor: 1, totalFloors: 20 },
];

/**
 * GateHub - System Gate Hub & Dungeon Floor progress tracker.
 */
export function GateHub() {
    const [gates, setGates] = useState<DungeonGate[]>(INITIAL_GATES);
    const [keys, setKeys] = useState(2);

    const handleUnlock = (id: string) => {
        if (keys <= 0) return;
        setGates((prev) =>
            prev.map((g) => (g.id === id ? { ...g, unlocked: true } : g))
        );
        setKeys((k) => k - 1);
    };

    return (
        <div className="bg-[#05050A] text-[#E2E8F0] p-6 space-y-8 text-left font-space max-w-6xl mx-auto">
            {/* Header */}
            <div className="flex justify-between items-center border-b border-gray-800 pb-4">
                <div className="flex items-center gap-3">
                    <span className="text-[#2563EB] text-2xl font-mono">⛩️</span>
                    <h2 className="text-headline-md tracking-widest text-[#E2E8F0] uppercase font-bold">SYSTEM GATE HUB</h2>
                </div>
                <div className="flex items-center gap-3 bg-black/40 px-4 py-1.5 border border-[#2563EB] font-mono text-xs">
                    <span className="text-gray-500">GATE KEYS:</span>
                    <span className="text-[#FBBF24] font-bold">{keys} AVAILABLE</span>
                </div>
            </div>

            {/* Content Split: Left - Active Gates list, Right - Active Floor Tracker */}
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                {/* Left Side: Gate Selection list (2 Columns) */}
                <div className="lg:col-span-2 space-y-4">
                    <h3 className="text-xs text-gray-400 font-bold tracking-widest uppercase mb-4 flex items-center gap-2 font-mono">
                        <span className="w-1 h-3 bg-[#2563EB]" /> GATEWAYS DETECTED
                    </h3>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        {gates.map((gate) => {
                            const isSRank = gate.rank === 'S';
                            const isARank = gate.rank === 'A';
                            return (
                                <TerminalCard
                                    key={gate.id}
                                    variant={gate.unlocked ? 'active' : 'default'}
                                    className="p-5 flex flex-col justify-between gap-4"
                                >
                                    {/* Gate Info */}
                                    <div className="flex justify-between items-start font-mono">
                                        <div>
                                            <span className="text-[10px] text-gray-500">GATE_CODE</span>
                                            <h4 className="text-headline-sm font-bold text-[#E2E8F0]">{gate.code}</h4>
                                        </div>
                                        <div className="text-right">
                                            <span className="text-[10px] text-gray-500 block">THREAT</span>
                                            <span className={`text-headline-sm font-bold ${
                                                isSRank ? 'text-[#FBBF24] glow-text-gold' : isARank ? 'text-[#EF4444]' : 'text-[#2563EB]'
                                            }`}>
                                                {gate.rank}-CLASS
                                            </span>
                                        </div>
                                    </div>

                                    {/* Clears Progress */}
                                    <div className="space-y-1">
                                        <div className="flex justify-between text-[11px] font-mono">
                                            <span className="text-gray-500">CLEAR PROGRESS</span>
                                            <span className="text-gray-300">{gate.clearsCompleted} / {gate.clearsRequired} CLEARS</span>
                                        </div>
                                        <SystemProgressBar
                                            current={gate.clearsCompleted}
                                            max={gate.clearsRequired}
                                            color={gate.clearsCompleted === gate.clearsRequired ? 'success' : 'cyan'}
                                        />
                                    </div>

                                    {/* Action Button */}
                                    {gate.unlocked ? (
                                        <div className="bg-[#2563EB]/10 border border-[#2563EB]/30 p-2.5 text-center text-xs font-mono text-[#2563EB] tracking-widest uppercase">
                                            GATE STABILIZED // ENTER RIFT
                                        </div>
                                    ) : (
                                        <SystemButton
                                            variant="primary"
                                            fullWidth
                                            disabled={keys <= 0}
                                            onClick={() => handleUnlock(gate.id)}
                                        >
                                            BURN KEY TO INITIALIZE
                                        </SystemButton>
                                    )}
                                </TerminalCard>
                            );
                        })}
                    </div>
                </div>

                {/* Right Side: Dungeon Floor Tracker (1 Column) */}
                <div className="lg:col-span-1 space-y-4">
                    <h3 className="text-xs text-gray-400 font-bold tracking-widest uppercase mb-4 flex items-center gap-2 font-mono">
                        <span className="w-1 h-3 bg-[#2563EB]" /> active floor tracker
                    </h3>
                    <TerminalCard variant="default" className="p-0 border border-gray-800">
                        {/* Terminal Header */}
                        <div className="bg-black/60 border-b border-gray-800 p-4 flex justify-between items-center font-mono text-[10px]">
                            <span className="text-[#2563EB] font-bold">MONARCH_LOGS.EXE</span>
                            <span className="text-[#22D3EE] animate-pulse">FLOOR_ACTIVE</span>
                        </div>

                        {/* Terminal Body */}
                        <div className="p-5 space-y-5 font-mono">
                            <div className="text-data-sm text-[#E2E8F0] space-y-1 text-left leading-relaxed">
                                <p>&gt; INITIALIZING CONSTRUCT SYNC...</p>
                                <p>&gt; FLOOR STAGE: 02/10 SECURED.</p>
                                <p>&gt; ACTIVE MONSTERS: 12 HOUNDS.</p>
                            </div>

                            {/* Subtask list / progress tracker */}
                            <div className="space-y-3 pt-2 text-left border-t border-gray-900">
                                {[
                                    { id: 1, label: 'Clear Floor 1 (Introduction)', state: 'SECURED' },
                                    { id: 2, label: 'Clear Floor 2 (Hound Lair)', state: 'IN_PROGRESS' },
                                    { id: 3, label: 'Clear Floor 3 (Boss Chamber)', state: 'LOCKED' },
                                ].map((floor) => (
                                    <div key={floor.id} className="flex justify-between items-center bg-black/40 p-2.5 border-l border-gray-800">
                                        <div className="space-y-1">
                                            <span className="text-[11px] text-gray-400">{floor.label}</span>
                                        </div>
                                        <span className={`text-[10px] px-2 py-0.5 border ${
                                            floor.state === 'SECURED' ? 'border-[#22C55E]/30 bg-[#22C55E]/10 text-[#22C55E]' :
                                            floor.state === 'IN_PROGRESS' ? 'border-[#22D3EE]/30 bg-[#22D3EE]/10 text-[#22D3EE] animate-pulse' :
                                            'border-gray-800 text-gray-700'
                                        }`}>
                                            {floor.state}
                                        </span>
                                    </div>
                                ))}
                            </div>
                        </div>
                    </TerminalCard>
                </div>
            </div>
        </div>
    );
}
