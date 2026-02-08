import { useState, useEffect } from 'react';
import { ScreenFrame } from '../../components/layout';
import { clsx } from 'clsx';

// Types representing the different log levels from the reference
type LogType = 'SYS_BOOT' | 'INFO' | 'RESTORE' | 'SUCCESS' | 'WARN' | 'CRITICAL' | 'ERROR';

interface LogEntry {
    id: string;
    timestamp: string;
    type: LogType;
    message: string;
}

interface SystemLogViewProps {
    onBack: () => void;
}

export function SystemLogView({ onBack }: SystemLogViewProps) {
    // Mock Data based on the reference image
    const [logs] = useState<LogEntry[]>([
        { id: '1', timestamp: '[07:59:55]', type: 'SYS_BOOT', message: 'SYSTEM INITIALIZED. WELCOME BACK, PLAYER.' },
        { id: '2', timestamp: '[08:00:00]', type: 'INFO', message: "DAILY QUEST ASSIGNED: 'MORNING RUN - 10KM'" },
        { id: '3', timestamp: '[12:30:15]', type: 'RESTORE', message: '>>> FATIGUE REDUCED BY 20%. MP FULLY RESTORED.' },
        { id: '4', timestamp: '[18:45:12]', type: 'SUCCESS', message: 'DAILY QUEST COMPLETED: +10 EXP APPLIED' },
        { id: '5', timestamp: '[23:59:59]', type: 'WARN', message: "STREAK BROKEN: 'READING' SKILL DEGRADED" },
        { id: '6', timestamp: '[00:00:01]', type: 'CRITICAL', message: 'PENALTY ZONE ENTERED: SURVIVAL QUEST INITIATED' },
        { id: '7', timestamp: '[00:00:05]', type: 'ERROR', message: 'PROMOTION FAILED: LEVEL REMAINS 14' },
    ]);

    // Blinking cursor effect
    const [showCursor, setShowCursor] = useState(true);
    useEffect(() => {
        const interval = setInterval(() => setShowCursor(c => !c), 500);
        return () => clearInterval(interval);
    }, []);

    // Helper to get styles for log types
    const getTypeStyles = (type: LogType) => {
        switch (type) {
            case 'SYS_BOOT':
                return 'border border-gray-600 text-gray-500 bg-transparent';
            case 'INFO':
                return 'bg-cyan-900/40 text-cyan-400 border border-cyan-800';
            case 'RESTORE':
                return 'bg-green-900/40 text-green-400 border border-green-800';
            case 'SUCCESS':
                return 'bg-cyan-500 text-black font-bold border border-cyan-400';
            case 'WARN':
                return 'bg-yellow-900/40 text-yellow-400 border border-yellow-700';
            case 'CRITICAL':
                return 'bg-red-500/20 text-red-500 border border-red-500 font-bold';
            case 'ERROR':
                return 'bg-red-900/40 text-red-500 border border-red-800';
            default:
                return 'text-gray-400';
        }
    };

    const getRowStyles = (type: LogType) => {
        if (type === 'SUCCESS') return 'bg-cyan-900/20 border-y border-cyan-500/30';
        if (type === 'CRITICAL') return 'bg-red-900/10 border-y border-red-500/30';
        return 'border-b border-gray-800/50 hover:bg-gray-900/50';
    };

    return (
        <ScreenFrame className="flex flex-col h-screen overflow-hidden relative">

            {/* --- TOP HEADER BAR --- */}
            <header className="flex justify-between items-start border-b border-gray-800 pb-4 mb-8">
                <div>
                    <div className="flex items-center gap-3 mb-1">
                        <div className="border border-cyan-500 p-1 rounded-sm">
                            <div className="w-4 h-4 bg-cyan-900 flex items-center justify-center">
                                <span className="text-[10px] text-cyan-400">⚡</span>
                            </div>
                        </div>
                        <h1 className="text-xl font-black italic tracking-wider text-transparent bg-clip-text bg-gradient-to-r from-cyan-400 to-blue-500" style={{ fontFamily: 'Inter, sans-serif' }}>
                            SYSTEM OS
                        </h1>
                    </div>
                    <div className="text-[10px] text-gray-500 font-mono tracking-widest pl-9">
                        VERSION 2.4.1 // <span className="text-green-500">ONLINE</span>
                    </div>
                </div>

                <div className="flex items-center gap-4">
                    <div className="bg-gray-900 border border-gray-700 rounded px-3 py-1 flex items-center gap-2">
                        <span className="w-2 h-2 rounded-full bg-green-500 animate-pulse" />
                        <span className="text-[10px] text-gray-300 font-mono tracking-wider">CONNECTED</span>
                    </div>
                    <button
                        onClick={onBack}
                        className="bg-transparent border border-cyan-600 text-cyan-400 hover:bg-cyan-900/20 px-4 py-1 rounded text-[10px] font-mono tracking-widest transition-colors flex items-center gap-2"
                    >
                        <span className="text-lg leading-none">⏻</span> LOGOUT
                    </button>
                </div>
            </header>

            {/* --- MAIN HEADER --- */}
            <div className="flex justify-between items-end mb-8 relative">
                {/* Decorative Dot */}
                <div className="absolute -left-6 top-2 w-2 h-2 rounded-full bg-cyan-500" />

                <div>
                    <h2 className="text-4xl font-black text-white tracking-tighter mb-2">SYSTEM_LOG</h2>
                    <div className="text-xs text-gray-400 font-mono tracking-widest">
                        // USER_ID: <span className="text-white font-bold">PLAYER</span> // CLASS: <span className="text-white">SHADOW_MONARCH</span>
                    </div>
                </div>

                <div className="text-right">
                    <div className="flex gap-8">
                        <div>
                            <div className="text-[10px] text-gray-500 uppercase tracking-widest mb-1">Current Level</div>
                            <div className="text-3xl font-bold text-white font-mono">14</div>
                        </div>
                        <div>
                            <div className="text-[10px] text-gray-500 uppercase tracking-widest mb-1">Job Quest</div>
                            <div className="text-xl font-bold text-yellow-500 font-mono">PENDING</div>
                        </div>
                    </div>
                </div>
            </div>

            {/* --- LOG TABLE --- */}
            <div className="flex-1 bg-gray-900/30 border border-gray-800 rounded-lg overflow-hidden flex flex-col relative">

                {/* Table Header */}
                <div className="grid grid-cols-12 gap-4 p-4 bg-gray-900/80 border-b border-gray-700 text-[10px] text-gray-500 font-mono tracking-widest uppercase">
                    <div className="col-span-2">Timestamp</div>
                    <div className="col-span-2">Type</div>
                    <div className="col-span-8">Event_Message</div>
                </div>

                {/* Table Body */}
                <div className="overflow-y-auto flex-1 font-mono text-xs">
                    {logs.map((log) => (
                        <div
                            key={log.id}
                            className={clsx(
                                "grid grid-cols-12 gap-4 px-4 py-3 items-center transition-colors",
                                getRowStyles(log.type)
                            )}
                        >
                            <div className="col-span-2 text-gray-500">{log.timestamp}</div>
                            <div className="col-span-2">
                                <span className={clsx("px-2 py-0.5 text-[10px] uppercase tracking-wider rounded-sm inline-block min-w-[60px] text-center", getTypeStyles(log.type))}>
                                    {log.type}
                                </span>
                            </div>
                            <div className={clsx(
                                "col-span-8 tracking-wide",
                                log.type === 'SUCCESS' ? 'text-cyan-400 font-bold' :
                                    log.type === 'CRITICAL' ? 'text-red-400 font-bold' :
                                        'text-gray-300'
                            )}>
                                {log.type === 'RESTORE' && <span className="text-green-500 mr-2">{">>>"}</span>}
                                {log.message}
                            </div>
                        </div>
                    ))}

                    {/* Input Cursor Line */}
                    <div className="px-4 py-6 flex items-center gap-2 text-cyan-500 text-xs font-mono tracking-wider">
                        <span className="animate-spin text-sm">↻</span>
                        AWAITING INPUT...
                        <span className={clsx("inline-block w-2.5 h-4 bg-cyan-400 ml-1", showCursor ? 'opacity-100' : 'opacity-0')} />
                    </div>
                </div>

                {/* Scanline Overlay */}
                <div className="absolute inset-0 bg-[linear-gradient(rgba(18,18,18,0)_50%,rgba(0,0,0,0.1)_50%),linear-gradient(90deg,rgba(255,0,0,0.06),rgba(255,0,0,0.02),rgba(255,0,0,0.06))] pointer-events-none bg-[length:100%_4px,6px_100%] opacity-10" />
            </div>

            {/* --- FOOTER --- */}
            <footer className="mt-4 pt-4 border-t border-gray-800 flex justify-between items-center text-[10px] text-gray-600 font-mono tracking-widest uppercase">
                <div className="flex gap-6">
                    <span>MEM: 64TB OK</span>
                    <span>CPU: 12%</span>
                    <span>NET: SECURE</span>
                </div>
                <div className="flex items-center gap-2">
                    <span className="w-1.5 h-1.5 rounded-full bg-cyan-600" />
                    SERVER_TIME_UTC
                </div>
            </footer>

        </ScreenFrame>
    );
}
