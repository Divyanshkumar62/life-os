import { useState } from 'react';
import { TerminalCard } from '../system/TerminalCard';

export interface TelemetryLog {
    id: string;
    gateId: string;
    rank: 'C' | 'B' | 'A' | 'S';
    status: 'EXTRACTABLE' | 'EXTRACTED' | 'PURGED';
    timestamp: string;
}

const MOCK_LOGS: TelemetryLog[] = [
    { id: 'log-1', gateId: '#0981', rank: 'A', status: 'EXTRACTABLE', timestamp: '2026-07-16' },
    { id: 'log-2', gateId: '#0842', rank: 'B', status: 'EXTRACTED', timestamp: '2026-07-14' },
    { id: 'log-3', gateId: '#0711', rank: 'S', status: 'PURGED', timestamp: '2026-07-12' },
    { id: 'log-4', gateId: '#0690', rank: 'C', status: 'EXTRACTED', timestamp: '2026-07-10' },
    { id: 'log-5', gateId: '#0644', rank: 'A', status: 'PURGED', timestamp: '2026-07-08' },
];

// Generate 365 days of data representing activity levels:
// 0: NO_QUESTS (Grey), 1: FAILED (Crimson), 2: PARTIAL (Deep Blue), 3: ALL_CLEARED (Neon Blue)
const generateHeatmapData = () => {
    const data = [];
    const states = ['NO_QUESTS', 'FAILED', 'PARTIAL', 'ALL_CLEARED'];
    for (let i = 0; i < 364; i++) {
        // Higher probability of all cleared / partial than failed
        const rand = Math.random();
        const stateIndex = rand < 0.15 ? 0 : rand < 0.25 ? 1 : rand < 0.55 ? 2 : 3;
        data.push(states[stateIndex]);
    }
    return data;
};

const HEATMAP_CELLS = generateHeatmapData();

/**
 * TelemetryHub - Productivity Heatmap & Telemetry growth dashboard.
 */
export function TelemetryHub() {
    const [logs] = useState<TelemetryLog[]>(MOCK_LOGS);
    const [heatmap] = useState<string[]>(HEATMAP_CELLS);

    const getCellColor = (state: string) => {
        switch (state) {
            case 'ALL_CLEARED':
                return 'bg-[#22D3EE] shadow-[0_0_4px_rgba(34,211,238,0.4)]'; // Neon Blue
            case 'PARTIAL':
                return 'bg-[#2563EB]/60'; // Deep Blue
            case 'FAILED':
                return 'bg-[#EF4444]/65'; // Crimson Red
            case 'NO_QUESTS':
            default:
                return 'bg-gray-800/30'; // Grey
        }
    };

    return (
        <div className="bg-[#05050A] text-[#E2E8F0] p-6 space-y-8 text-left font-space max-w-6xl mx-auto">
            {/* Header */}
            <div className="flex justify-between items-center border-b border-gray-800 pb-4">
                <div className="flex items-center gap-3">
                    <span className="text-[#2563EB] text-2xl font-mono">📊</span>
                    <h2 className="text-headline-md tracking-widest text-[#E2E8F0] uppercase font-bold">
                        TELEMETRY_HUB
                    </h2>
                </div>
                <div className="flex items-center gap-4 text-xs font-mono">
                    <span className="text-gray-500">TOTAL DUNGEONS CLEARED:</span>
                    <span className="text-[#22D3EE] font-bold">242 CLEARS</span>
                </div>
            </div>

            {/* Heatmap Section */}
            <TerminalCard variant="default" className="p-6 space-y-4">
                <div className="flex flex-col md:flex-row justify-between md:items-center gap-2">
                    <h3 className="text-xs text-gray-400 font-bold tracking-widest uppercase flex items-center gap-2 font-mono">
                        <span className="w-1 h-3 bg-[#2563EB]" /> [ ANNUAL TELEMETRY // PRODUCTIVITY LOG ]
                    </h3>
                    {/* Legend */}
                    <div className="flex items-center gap-3 text-[10px] font-mono text-gray-500">
                        <span className="flex items-center gap-1">
                            <span className="w-2.5 h-2.5 bg-gray-800/30 inline-block" /> NO_QUESTS
                        </span>
                        <span className="flex items-center gap-1">
                            <span className="w-2.5 h-2.5 bg-[#EF4444]/65 inline-block" /> FAILED
                        </span>
                        <span className="flex items-center gap-1">
                            <span className="w-2.5 h-2.5 bg-[#2563EB]/60 inline-block" /> PARTIAL
                        </span>
                        <span className="flex items-center gap-1">
                            <span className="w-2.5 h-2.5 bg-[#22D3EE] inline-block" /> ALL_CLEARED
                        </span>
                    </div>
                </div>

                {/* 365 Days Grid (52 weeks x 7 days) */}
                <div className="overflow-x-auto scrollbar-hide py-2">
                    <div className="grid grid-flow-col grid-rows-7 gap-1.5 min-w-[650px] w-full">
                        {heatmap.map((state, idx) => (
                            <div
                                key={idx}
                                className={`w-3 h-3 transition-colors duration-200 hover:scale-125 ${getCellColor(state)}`}
                                title={`Day ${idx + 1}: ${state}`}
                            />
                        ))}
                    </div>
                </div>
            </TerminalCard>

            {/* Bottom Split Layout: Growth trends & Graveyard Archive */}
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                {/* Left Side: Growth Trends (Attributes scaling) (2 Columns) */}
                <div className="lg:col-span-2 space-y-4">
                    <h3 className="text-xs text-gray-400 font-bold tracking-widest uppercase flex items-center gap-2 font-mono">
                        <span className="w-1 h-3 bg-[#2563EB]" /> [ ATTRIBUTE SCALING // TELEMETRY ]
                    </h3>
                    <TerminalCard variant="default" className="p-6 space-y-4 h-[300px] flex flex-col justify-between">
                        {/* Mock Graph Layout */}
                        <div className="flex-1 flex items-end justify-between gap-2 border-b border-gray-800 pb-2 relative">
                            {/* Grid lines */}
                            <div className="absolute inset-0 flex flex-col justify-between pointer-events-none opacity-10">
                                <div className="border-t border-gray-500 w-full" />
                                <div className="border-t border-gray-500 w-full" />
                                <div className="border-t border-gray-500 w-full" />
                            </div>

                            {/* Columns represent monthly scaling values */}
                            {[45, 60, 55, 80, 75, 95, 110, 90, 120, 135, 150].map((val, idx) => (
                                <div key={idx} className="flex-1 flex flex-col items-center gap-1.5 group relative">
                                    <div
                                        className="w-full bg-[#2563EB] hover:bg-[#22D3EE] transition-all duration-500 relative"
                                        style={{ height: `${(val / 150) * 180}px` }}
                                    >
                                        <span className="absolute -top-6 left-1/2 -translate-x-1/2 text-[9px] font-mono text-[#22D3EE] opacity-0 group-hover:opacity-100 transition-opacity">
                                            {val}
                                        </span>
                                    </div>
                                    <span className="text-[9px] font-mono text-gray-600">M{idx + 1}</span>
                                </div>
                            ))}
                        </div>
                        <div className="flex justify-between text-xs text-gray-500 font-mono">
                            <span>TIMELINE: 12 MONTH TRACKING</span>
                            <span>STATUS: SYNC_COMPLETE</span>
                        </div>
                    </TerminalCard>
                </div>

                {/* Right Side: Graveyard Archive (1 Column) */}
                <div className="lg:col-span-1 space-y-4">
                    <h3 className="text-xs text-gray-400 font-bold tracking-widest uppercase flex items-center gap-2 font-mono">
                        <span className="w-1 h-3 bg-[#2563EB]" /> [ SHADOW EXTRACTION SOURCE // GRAVEYARD ]
                    </h3>
                    <TerminalCard variant="default" className="p-0 border border-gray-800 overflow-hidden h-[300px] flex flex-col justify-between">
                        {/* Feed */}
                        <div className="overflow-y-auto scrollbar-hide p-5 space-y-3 font-mono text-left flex-1">
                            {logs.map((log) => {
                                const isPurged = log.status === 'PURGED';
                                const isExtractable = log.status === 'EXTRACTABLE';
                                return (
                                    <div
                                        key={log.id}
                                        className="text-[11px] bg-black/40 p-2.5 border-l-2 border-gray-800 flex justify-between items-center"
                                    >
                                        <div className="space-y-0.5">
                                            <span className="text-gray-400 font-bold">&gt; GATE_ID: {log.gateId}</span>
                                            <span className="block text-[9px] text-gray-600">RANK: {log.rank} // {log.timestamp}</span>
                                        </div>
                                        <span className={`text-[10px] px-2 py-0.5 border ${
                                            isPurged ? 'border-[#EF4444]/30 bg-[#EF4444]/10 text-[#EF4444]' :
                                            isExtractable ? 'border-[#22C55E]/30 bg-[#22C55E]/10 text-[#22C55E] animate-pulse' :
                                            'border-[#2563EB]/30 bg-[#2563EB]/10 text-[#2563EB]'
                                        }`}>
                                            {log.status}
                                        </span>
                                    </div>
                                );
                            })}
                        </div>
                        {/* Footer */}
                        <div className="bg-black/60 border-t border-gray-800 p-4 flex justify-between items-center font-mono text-[10px]">
                            <span className="text-gray-500">EOF...</span>
                            <span className="text-[#2563EB]">SYSTEM_STABLE</span>
                        </div>
                    </TerminalCard>
                </div>
            </div>
        </div>
    );
}
