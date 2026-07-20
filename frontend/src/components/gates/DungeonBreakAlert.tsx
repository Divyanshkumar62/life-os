import { TerminalCard } from '../system/TerminalCard';
import { SystemButton } from '../system/SystemButton';

export interface DungeonBreakAlertProps {
    onForceClose?: () => void;
}

/**
 * DungeonBreakAlert - Critical alert overlay triggered upon Dungeon Break.
 */
export function DungeonBreakAlert({ onForceClose }: DungeonBreakAlertProps) {
    return (
        <div className="fixed inset-0 z-[300] bg-black text-[#EF4444] p-8 flex flex-col justify-between items-center relative overflow-hidden font-space select-none">
            {/* Severe Threat background animations */}
            <div className="absolute inset-0 pointer-events-none opacity-10 bg-repeat bg-striped animate-pulse-glow" />
            <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[700px] h-[700px] bg-[#EF4444]/10 blur-[180px] rounded-full pointer-events-none" />

            {/* Top Bar Warning */}
            <div className="max-w-4xl w-full border-b border-[#EF4444]/30 pb-4 flex justify-between items-center font-mono">
                <span className="text-xs tracking-[0.2em] font-bold">WARNING: SYSTEM PURGE ACTIVE</span>
                <span className="text-xs tracking-widest animate-pulse font-bold">● CODE_RED_OVERRIDE</span>
            </div>

            {/* Center Alert Group */}
            <main className="max-w-xl w-full my-auto text-center flex flex-col items-center gap-8">
                {/* Flashing Warning Triangle */}
                <div className="w-24 h-24 flex items-center justify-center border-4 border-[#EF4444] rounded-none animate-bounce shadow-[0_0_20px_rgba(239,68,68,0.4)]">
                    <span className="text-6xl font-bold font-space">!</span>
                </div>

                <div className="space-y-4">
                    <h1 className="text-headline-lg font-bold tracking-widest text-[#EF4444] uppercase leading-snug animate-pulse">
                        [ CRITICAL ALERT: DUNGEON BREAK IMMINENT ]
                    </h1>
                    <p className="text-body-sm text-gray-400 font-sans leading-relaxed max-w-md mx-auto">
                        A Class-A gateway rift has collapsed. System perimeter breach detected. Expulsion protocols have been suspended.
                    </p>
                </div>

                {/* Monospace System Logs Card */}
                <TerminalCard
                    variant="alert"
                    className="w-full bg-[#05050A]/90 p-6 text-left border border-[#EF4444]/50 shadow-[0_0_15px_rgba(239,68,68,0.2)]"
                >
                    <div className="font-mono text-xs space-y-2 leading-relaxed">
                        <div className="flex justify-between border-b border-gray-900 pb-2">
                            <span className="text-gray-500">GATE_STATUS:</span>
                            <span className="font-bold uppercase text-[#EF4444] animate-pulse">TIMER_EXPIRED</span>
                        </div>
                        <div className="flex justify-between border-b border-gray-900 pb-2">
                            <span className="text-gray-500">SHADOW_CONSTRUCT:</span>
                            <span className="font-bold uppercase text-[#EF4444]">UNSTABLE</span>
                        </div>
                        <div className="flex justify-between border-b border-gray-900 pb-2">
                            <span className="text-gray-500">THREAT_LEVEL:</span>
                            <span className="font-bold text-[#FBBF24]">S-CLASS PURGE</span>
                        </div>
                        <div className="pt-2 text-gray-500">
                            <p>&gt; CONTEXT: SYSTEM PURGE PROTOCOL KICKING IN</p>
                            <p>&gt; FORCE CLOSE RE-ENTRY CHANNEL INITIATED...</p>
                            <p>&gt; LINK SECURITY CRITICAL...</p>
                        </div>
                    </div>
                </TerminalCard>
            </main>

            {/* Bottom Actions */}
            <div className="max-w-xl w-full border-t border-[#EF4444]/30 pt-6 flex justify-center">
                <SystemButton
                    variant="danger"
                    size="lg"
                    className="w-full py-4 text-headline-sm font-bold bg-[#EF4444] text-black hover:bg-[#EF4444]/90 border-none shadow-[0_0_20px_rgba(239,68,68,0.5)] transition-all hover:scale-[1.01] active:scale-[0.99]"
                    onClick={onForceClose}
                >
                    [ FORCE CLOSE RIFT ]
                </SystemButton>
            </div>
        </div>
    );
}
