import { TerminalCard } from '../system/TerminalCard';
import { SystemButton } from '../system/SystemButton';

export interface ArchitectsTrialProps {
    onAccept?: () => void;
    onDecline?: () => void;
}

/**
 * ArchitectsTrial - The S-Rank trial decision screen.
 */
export function ArchitectsTrial({ onAccept, onDecline }: ArchitectsTrialProps) {
    return (
        <div className="min-h-screen bg-[#05050A] text-[#E2E8F0] p-8 flex flex-col justify-between items-center relative overflow-hidden font-space">
            {/* Background Atmosphere Glow - Red/Gold Threat Colors */}
            <div className="absolute inset-0 pointer-events-none opacity-5 bg-repeat bg-striped" />
            <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[600px] h-[600px] bg-[#EF4444]/5 blur-[160px] rounded-full pointer-events-none" />
            <div className="absolute bottom-10 left-10 w-96 h-96 bg-[#FBBF24]/5 blur-[120px] rounded-full pointer-events-none" />

            {/* Top Bar */}
            <div className="max-w-4xl w-full border-b border-gray-800 pb-4 flex justify-between items-center">
                <span className="text-xs text-gray-500 tracking-[0.2em] uppercase font-bold">SYSTEM THREAT LEVEL: CRITICAL</span>
                <span className="text-xs text-[#EF4444] tracking-widest font-mono animate-pulse">● PROTOCOL_AWAITING_INPUT</span>
            </div>

            {/* Main Content Card */}
            <main className="max-w-2xl w-full my-auto text-center flex flex-col items-center">
                <TerminalCard
                    variant="alert"
                    className="w-full bg-[#05050A]/80 backdrop-blur-md p-8 md:p-12 text-center border-l-2 border-r-2 border-t-2 border-b-2"
                >
                    <div className="space-y-8">
                        {/* Title */}
                        <div>
                            <span className="text-xs text-[#FBBF24] tracking-[0.25em] font-bold uppercase block mb-2 glow-text-gold">
                                SYSTEM INTRUSION
                            </span>
                            <h1 className="text-headline-lg font-bold tracking-[0.1em] text-[#E2E8F0] uppercase leading-none">
                                SYSTEM: ARCHITECT'S TRIAL
                            </h1>
                        </div>

                        {/* Divider */}
                        <div className="w-16 h-1 bg-[#EF4444] mx-auto shadow-[0_0_8px_rgba(239,68,68,0.5)]" />

                        {/* Details */}
                        <div className="space-y-4 py-2">
                            <div className="bg-black/40 border border-gray-800 p-4 font-mono text-left space-y-3">
                                <div className="flex justify-between text-xs">
                                    <span className="text-gray-500">QUEST TITLE:</span>
                                    <span className="text-[#E2E8F0] font-bold">COURAGE OF THE WEAK</span>
                                </div>
                                <div className="flex justify-between text-xs">
                                    <span className="text-gray-500">DIFFICULTY:</span>
                                    <span className="text-[#FBBF24] font-bold glow-text-gold">[ S-RANK ]</span>
                                </div>
                                <div className="flex justify-between text-xs">
                                    <span className="text-gray-500">OBJECTIVE:</span>
                                    <span className="text-[#EF4444] font-bold animate-pulse">SURVIVE OR PERISH</span>
                                </div>
                            </div>

                            <p className="text-body-sm text-gray-400 font-sans leading-relaxed">
                                You have reached the boundary of initialization. To unlock progression and class promotion paths, you must submit to the trials of the Creator.
                            </p>

                            {/* Warning Box */}
                            <div className="bg-[#EF4444]/10 border border-[#EF4444]/40 p-4 chamfer text-left">
                                <p className="text-xs font-mono text-[#EF4444] uppercase tracking-wider leading-relaxed font-bold">
                                    [WARNING: DECLINING TRIAL WILL RESULT IN IMMEDIATE ACCOUNT TERMINATION AND SYSTEM EXPULSION]
                                </p>
                            </div>
                        </div>

                        {/* Split Action Switches */}
                        <div className="grid grid-cols-1 sm:grid-cols-2 gap-6 pt-4 w-full">
                            <SystemButton
                                variant="success"
                                size="lg"
                                className="py-4 text-[#05050A] tracking-[0.2em] font-bold shadow-[0_0_12px_rgba(74,225,118,0.4)] transition-all"
                                onClick={onAccept}
                            >
                                ACCEPT
                            </SystemButton>
                            <SystemButton
                                variant="danger"
                                size="lg"
                                className="py-4 tracking-[0.2em] hover:bg-[#EF4444]/10 transition-all font-bold"
                                onClick={onDecline}
                            >
                                DECLINE
                            </SystemButton>
                        </div>
                    </div>
                </TerminalCard>
            </main>

            {/* Bottom Section */}
            <div className="max-w-4xl w-full border-t border-gray-800 pt-6 text-center text-data-sm text-gray-600 font-mono">
                SECURE_LINK // AUTH_REQUIRED
            </div>
        </div>
    );
}
