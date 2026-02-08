import { clsx } from 'clsx';

interface DiagnosticViewProps {
    onBack: () => void;
}

export function DiagnosticView({ onBack }: DiagnosticViewProps) {
    const continuityScore = 42;
    const targetScore = 103;

    const logEntries = [
        {
            id: '#9942',
            date: 'YESTERDAY',
            event: 'SEQ_BREAK',
            reason: 'PENALTY ZONE ENTRY',
            impact: 'STREAK PRESERVATION PROTOCOL ACTIVE',
            details: '// USER ENTERED PROHIBITED ZONE "SOCIAL MEDIA" DURING FOCUSED HOURS. PENALTY INCURRED.',
            type: 'error'
        },
        {
            id: '#9918',
            date: '2023-10-15',
            event: 'ROUTINE_MAINTENANCE',
            reason: 'SCHEDULED REST',
            type: 'info'
        },
        {
            id: '#9840',
            date: '2023-09-30',
            event: 'SYNC_COMPLETE',
            status: 'OPTIMAL',
            type: 'success'
        }
    ];

    return (
        <div className="h-screen w-full bg-[#050911] text-white overflow-hidden flex flex-col" style={{ fontFamily: 'Consolas, Monaco, monospace' }}>

            {/* Header Bar */}
            <div className="flex justify-between items-center px-6 py-3 border-b border-gray-800/30">
                <div className="flex items-center gap-3">
                    {/* Terminal Icon */}
                    <div className="w-7 h-7 border border-cyan-700/80 flex items-center justify-center bg-black/60">
                        <span className="text-cyan-400 text-xs font-bold">&gt;_</span>
                    </div>
                    {/* System Label */}
                    <div className="text-[9px] tracking-[0.25em] text-cyan-700/90 font-semibold">
                        SYSTEM_OS_VER_4.2 // CONTINUITY MONITORING
                    </div>
                </div>

                <div className="flex items-center gap-5 text-[9px] tracking-[0.15em]">
                    <div className="flex items-center gap-2 text-gray-600">
                        <div className="w-1.5 h-1.5 rounded-full bg-green-500" />
                        <span>SERVER: ONLINE</span>
                    </div>
                    <div className="text-gray-600">PLAYER: SUNG</div>
                    <div className="text-gray-600">SYNC: 98.4%</div>
                    <button
                        onClick={onBack}
                        className="border border-cyan-800/60 text-cyan-500 hover:bg-cyan-900/20 px-4 py-1.5 text-[9px] tracking-[0.2em] transition-all duration-200 font-bold"
                    >
                        LOGOUT
                    </button>
                </div>
            </div>

            <div className="flex-1 flex gap-0 overflow-hidden">

                {/* Main Content Area */}
                <div className="flex-[1.4] flex flex-col gap-0 px-6 py-6">

                    {/* Title Section */}
                    <div className="mb-6 relative pl-5">
                        <div className="absolute left-0 top-0 bottom-0 w-1 bg-cyan-500" />
                        <h1 className="text-[42px] font-black tracking-[-0.02em] leading-[1.1] mb-1.5" style={{ fontFamily: 'Inter, system-ui, sans-serif' }}>
                            DIAGNOSTIC MODE
                        </h1>
                        <div className="flex items-center gap-2 text-[10px] text-gray-600 tracking-[0.15em]">
                            <span className="inline-block animate-spin">↻</span>
                            <span>ACTIVE SCANNING...</span>
                        </div>
                    </div>

                    {/* Continuity Index Card */}
                    <div className="mb-5 relative border border-cyan-900/30 bg-gradient-to-b from-black/30 to-black/50 py-12 px-6">

                        <div className="text-center">
                            <div className="text-[10px] text-cyan-700/70 tracking-[0.35em] mb-10 font-bold">
                                CONTINUITY INDEX
                            </div>

                            <div className="flex items-end justify-center gap-4 mb-12">
                                <span className="text-[150px] font-black leading-[0.85] tracking-[-0.04em] text-white" style={{ fontFamily: 'Inter, system-ui, sans-serif' }}>
                                    042
                                </span>
                                <span className="text-[32px] font-bold text-cyan-600/90 tracking-[0.25em] pb-4 leading-none">
                                    DAYS
                                </span>
                            </div>

                            {/* Progress Bar */}
                            <div className="w-full h-[2px] bg-cyan-950/30 mb-2.5 overflow-hidden">
                                <div
                                    className="h-full bg-cyan-500"
                                    style={{
                                        width: `${(continuityScore / targetScore) * 100}%`,
                                        boxShadow: '0 0 8px rgba(6, 182, 212, 0.5)'
                                    }}
                                />
                            </div>
                            <div className="flex justify-between text-[9px] tracking-[0.22em] text-cyan-900/60 font-medium">
                                <span>MIN: 000</span>
                                <span>TARGET: {targetScore}</span>
                            </div>
                        </div>

                        {/* Corner Brackets */}
                        <div className="absolute top-0 left-0 w-3 h-3 border-t border-l border-cyan-800/40" />
                        <div className="absolute top-0 right-0 w-3 h-3 border-t border-r border-cyan-800/40" />
                        <div className="absolute bottom-0 left-0 w-3 h-3 border-b border-l border-cyan-800/40" />
                        <div className="absolute bottom-0 right-0 w-3 h-3 border-b border-r border-cyan-800/40" />
                    </div>

                    {/* Status Grid */}
                    <div className="grid grid-cols-2 gap-5 mb-5">
                        {/* Current Status */}
                        <div className="bg-black/30 border border-cyan-900/25 p-6 relative">
                            <div className="absolute top-5 right-5 text-[48px] text-green-900/10 leading-none">✓</div>
                            <div className="text-[9px] text-gray-600 tracking-[0.22em] mb-3 font-bold">CURRENT STATUS</div>
                            <div className="flex items-center gap-2.5 text-[26px] font-black text-green-400 leading-none" style={{ fontFamily: 'Inter, system-ui, sans-serif' }}>
                                STABLE
                                <div className="w-2 h-2 bg-green-500 rounded-full" />
                            </div>
                            <div className="text-[9px] text-gray-700/80 mt-3 tracking-[0.08em]">NO CRITICAL ERRORS DETECTED.</div>
                        </div>

                        {/* Next Milestone */}
                        <div className="bg-black/30 border border-cyan-900/25 p-6 relative">
                            <div className="absolute top-5 right-5 text-[48px] text-cyan-900/10 leading-none">⚑</div>
                            <div className="text-[9px] text-gray-600 tracking-[0.22em] mb-3 font-bold">NEXT MILESTONE</div>
                            <div className="text-[26px] font-black text-white leading-none" style={{ fontFamily: 'Inter, system-ui, sans-serif' }}>
                                050 <span className="text-[14px] font-normal text-gray-600 tracking-wide">DAYS</span>
                            </div>
                            <div className="text-[9px] text-gray-700/80 mt-3 tracking-[0.08em]">ESTIMATED ARRIVAL: T-MINUS 8 DAYS</div>
                        </div>
                    </div>

                    {/* Graph Card */}
                    <div className="flex-1 bg-black/30 border border-gray-800/40 p-5 flex flex-col min-h-[220px]">
                        <div className="flex justify-between items-center mb-5">
                            <div className="text-[9px] text-gray-600 tracking-[0.22em] font-bold">DISCIPLINE INTEGRITY</div>
                            <div className="text-[9px] text-orange-500/90 tracking-[0.22em] font-bold">VARIATION: -2%</div>
                        </div>

                        <div className="flex-1 relative">
                            {/* Grid Pattern */}
                            <div className="absolute inset-0 opacity-[0.06]"
                                style={{
                                    backgroundImage: 'radial-gradient(circle, #64748b 1.5px, transparent 1.5px)',
                                    backgroundSize: '24px 24px'
                                }}
                            />

                            {/* SVG Graph */}
                            <svg className="absolute inset-0 w-full h-full" preserveAspectRatio="none" viewBox="0 0 600 200">
                                <defs>
                                    <linearGradient id="graphFill" x1="0%" y1="0%" x2="0%" y2="100%">
                                        <stop offset="0%" stopColor="#06b6d4" stopOpacity="0.18" />
                                        <stop offset="100%" stopColor="#06b6d4" stopOpacity="0" />
                                    </linearGradient>
                                    <filter id="lineGlow">
                                        <feGaussianBlur stdDeviation="1.2" result="blur" />
                                        <feMerge>
                                            <feMergeNode in="blur" />
                                            <feMergeNode in="SourceGraphic" />
                                        </feMerge>
                                    </filter>
                                </defs>

                                {/* Fill Area */}
                                <path
                                    d="M0,100 Q120,75 240,90 T480,85 T600,80 L600,200 L0,200 Z"
                                    fill="url(#graphFill)"
                                />

                                {/* Stroke Line */}
                                <path
                                    d="M0,100 Q120,75 240,90 T480,85 T600,80"
                                    stroke="#06b6d4"
                                    strokeWidth="2.5"
                                    fill="none"
                                    filter="url(#lineGlow)"
                                    opacity="0.9"
                                />
                            </svg>
                        </div>
                    </div>

                </div>

                {/* Sidebar - Disruption Log */}
                <div className="w-[420px] flex flex-col border-l border-gray-800/30 px-6 py-6 bg-black/10">

                    <div className="flex justify-between items-center mb-6">
                        <div className="flex items-center gap-2.5">
                            <span className="text-cyan-500 text-sm">↻</span>
                            <h3 className="text-[13px] font-bold tracking-wide">DISRUPTION LOG</h3>
                        </div>
                        <div className="text-[8px] border border-gray-700/40 px-2.5 py-1 text-gray-600 tracking-[0.15em]">READ_ONLY</div>
                    </div>

                    {/* Log Entries */}
                    <div className="flex-1 space-y-8 overflow-y-auto pr-2">
                        {logEntries.map((log, idx) => (
                            <div
                                key={idx}
                                className={clsx(
                                    "relative pl-5 border-l-[3px]",
                                    log.type === 'error' ? 'border-orange-500' : 'border-gray-700/40'
                                )}
                            >
                                {/* Timeline Dot */}
                                <div className={clsx(
                                    "absolute -left-[7px] top-2 w-3 h-3 rounded-full ring-[5px] ring-[#050911]",
                                    log.type === 'error' ? 'bg-orange-500' : 'bg-gray-700'
                                )} />

                                <div className="flex justify-between items-baseline mb-3">
                                    <span className={clsx(
                                        "text-[11px] font-bold tracking-[0.05em]",
                                        log.type === 'error' ? 'text-orange-500' : 'text-gray-600'
                                    )}>
                                        LOG ENTRY {log.id}
                                    </span>
                                    <span className="text-[8px] text-gray-700 tracking-[0.12em]">{log.date}</span>
                                </div>

                                <div className="space-y-2">
                                    <div className="grid grid-cols-[65px_1fr] gap-2 text-[10px]">
                                        <span className="text-gray-700 tracking-wide">EVENT:</span>
                                        <span className="text-gray-400">{log.event}</span>
                                    </div>
                                    <div className="grid grid-cols-[65px_1fr] gap-2 text-[10px]">
                                        <span className="text-gray-700 tracking-wide">REASON:</span>
                                        <span className={clsx(
                                            log.type === 'error' ? 'bg-orange-900/25 text-orange-400 px-2 py-0.5 -ml-2' : 'text-gray-500'
                                        )}>
                                            {log.reason || log.status}
                                        </span>
                                    </div>
                                    {log.impact && (
                                        <div className="grid grid-cols-[65px_1fr] gap-2 text-[10px]">
                                            <span className="text-gray-700 tracking-wide">IMPACT:</span>
                                            <span className="text-gray-500">{log.impact}</span>
                                        </div>
                                    )}
                                </div>

                                {log.details && (
                                    <div className="mt-3 text-[9px] text-gray-700/70 leading-relaxed border-t border-gray-800/40 pt-3">
                                        {log.details}
                                    </div>
                                )}
                            </div>
                        ))}
                    </div>

                    {/* Bottom Actions */}
                    <div className="mt-8 border-t border-gray-800/30 pt-5">
                        <div className="mb-4 text-[10px] text-cyan-700/80 tracking-wide">
                            &gt; AWAITING INPUT...
                        </div>
                        <div className="grid grid-cols-2 gap-4">
                            <button className="h-10 border border-cyan-900/40 text-cyan-500 hover:bg-cyan-900/15 text-[9px] tracking-[0.2em] transition-all duration-200 font-bold">
                                [ ACKNOWLEDGE ]
                            </button>
                            <button className="h-10 border border-gray-700/40 text-gray-600 hover:bg-gray-800/20 text-[9px] tracking-[0.2em] transition-all duration-200 font-bold">
                                [ RE-SYNC ]
                            </button>
                        </div>
                        <div className="text-center mt-4 text-[8px] text-gray-700/70 tracking-[0.08em]">
                            MAINTAIN CONSISTENCY OR FACE SYSTEM PENALTY.
                        </div>
                    </div>
                </div>

            </div>
        </div>
    );
}
