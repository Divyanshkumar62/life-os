import { useEffect, useState } from 'react';
import { SystemButton } from '../system/SystemButton';

export interface SystemInitializationProps {
    onComplete?: () => void;
}

const BOOT_LOGS = [
    '[SYSTEM] INITIALIZING ABYSSAL BOOT PROTOCOL.v01...',
    '[SYSTEM] LOADING DESIGN TOKENS // ABYSS_BLACK (#05050A)...',
    '[SYSTEM] ESTABLISHING ENCRYPTED LINK TO SOVEREIGN GATE...',
    '[SYSTEM] UPLINK PATHWAYS: SECURING SYNC...',
    '[SYSTEM] RUNNING QUANTUM CALIBRATION OVER RIFT...',
    '[SYSTEM] WARNING: UNIDENTIFIED SIGNATURE DETECTED IN SECTOR 4...',
    '[SYSTEM] INJECTING SOVEREIGN ANOMALY OVERRIDE KEYS...',
    '[SYSTEM] OVERRIDE: SUCCESSFUL. SIGNATURE STABILIZED.',
    '[SYSTEM] INTEGRATING CLASS PROTOCOLS: [VANGUARD], [SCHOLAR], [SHADOW_MONARCH]...',
    '[SYSTEM] SCANNING BIO-METRIC AND COGNITIVE WAVELENGTHS...',
    '[SYSTEM] LINK INTEGRATION COMPLETE. SYSTEM STATE: ONLINE.',
];

/**
 * SystemInitialization - Terminal Boot Sequence Simulator screen.
 */
export function SystemInitialization({ onComplete }: SystemInitializationProps) {
    const [logs, setLogs] = useState<string[]>([]);
    const [logIndex, setLogIndex] = useState(0);
    const [bootComplete, setBootComplete] = useState(false);

    useEffect(() => {
        if (logIndex < BOOT_LOGS.length) {
            const delay = logIndex === 0 ? 500 : Math.random() * 400 + 150;
            const timer = setTimeout(() => {
                setLogs((prev) => [...prev, BOOT_LOGS[logIndex]]);
                setLogIndex((prev) => prev + 1);
            }, delay);
            return () => clearTimeout(timer);
        } else {
            const timer = setTimeout(() => {
                setBootComplete(true);
            }, 800);
            return () => clearTimeout(timer);
        }
    }, [logIndex]);

    return (
        <div className="min-h-screen bg-[#05050A] text-[#E2E8F0] font-mono flex flex-col justify-between p-8 relative overflow-hidden">
            {/* Background Shader effect */}
            <div className="absolute inset-0 pointer-events-none opacity-5 bg-repeat bg-striped" />
            <div className="absolute bottom-0 right-0 w-96 h-96 bg-[#2563EB]/5 blur-[120px] rounded-full pointer-events-none" />

            {/* Top Bar */}
            <div className="flex justify-between items-center border-b border-gray-800 pb-4">
                <span className="text-xs text-[#2563EB] tracking-widest uppercase">SYS_INITIALIZE // SX-2940</span>
                <span className="text-xs text-[#2563EB] animate-pulse">● CONNECTION_ESTABLISHED</span>
            </div>

            {/* Terminal Logs View */}
            <div className="flex-1 my-8 overflow-y-auto scrollbar-hide flex flex-col justify-start gap-2 max-w-4xl mx-auto w-full text-left">
                {logs.map((log, index) => (
                    <div key={index} className="text-data-sm text-[#E2E8F0] tracking-wider leading-relaxed">
                        <span className="text-[#2563EB] mr-2">&gt;</span>
                        {log}
                    </div>
                ))}
                {!bootComplete && (
                    <div className="text-data-sm text-[#2563EB] animate-pulse">
                        <span className="text-[#2563EB] mr-2">&gt;</span>
                        [SYSTEM] AWAITING NEXT TASK...
                    </div>
                )}
            </div>

            {/* Bottom Section */}
            <div className="border-t border-gray-800 pt-6 flex flex-col items-center gap-4">
                {bootComplete ? (
                    <div className="animate-slide-in flex flex-col items-center gap-6">
                        <div className="text-center">
                            <h1 className="text-headline-md tracking-[0.25em] text-[#E2E8F0] animate-pulse font-space">
                                AWAITING PLAYER IDENTIFICATION
                            </h1>
                            <p className="text-xs text-gray-500 uppercase mt-2">Press the core button to initiate calibration link</p>
                        </div>
                        <SystemButton
                            variant="primary"
                            size="lg"
                            className="px-12 py-3 border-[#22D3EE] text-[#22D3EE] hover:bg-[#22D3EE]/10 shadow-[0_0_15px_rgba(34,211,238,0.3)] font-space"
                            onClick={onComplete}
                        >
                            AWAKEN
                        </SystemButton>
                    </div>
                ) : (
                    <div className="text-xs text-gray-600 uppercase font-space tracking-widest">
                        Boot Sequence Loading: {Math.floor((logIndex / BOOT_LOGS.length) * 100)}%
                    </div>
                )}
            </div>
        </div>
    );
}
