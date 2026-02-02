import { SystemButton } from '../../system';

export interface PromotionPopupProps {
    isOpen: boolean;
    onClose: () => void;
}

export function PromotionPopup({ isOpen, onClose }: PromotionPopupProps) {
    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center">
            {/* Backdrop */}
            <div
                className="absolute inset-0 bg-black/95 backdrop-blur-sm transition-opacity duration-300"
                onClick={onClose}
            />

            {/* Main Modal Container */}
            <div className="relative z-10 w-full max-w-2xl mx-4 animate-in fade-in zoom-in-95 duration-500">

                {/* Decorative Corner Brackets (Cyan) */}
                <div className="absolute -top-2 -left-2 w-8 h-8 border-t-2 border-l-2 border-cyan-400 rounded-tl-lg shadow-[0_0_15px_rgba(34,211,238,0.5)]" />
                <div className="absolute -top-2 -right-2 w-8 h-8 border-t-2 border-r-2 border-cyan-400 rounded-tr-lg shadow-[0_0_15px_rgba(34,211,238,0.5)]" />
                <div className="absolute -bottom-2 -left-2 w-8 h-8 border-b-2 border-l-2 border-cyan-400 rounded-bl-lg shadow-[0_0_15px_rgba(34,211,238,0.5)]" />
                <div className="absolute -bottom-2 -right-2 w-8 h-8 border-b-2 border-r-2 border-cyan-400 rounded-br-lg shadow-[0_0_15px_rgba(34,211,238,0.5)]" />

                {/* Main Content Card */}
                <div className="bg-[#050b14]/90 backdrop-blur-md border border-cyan-900/50 p-12 text-center shadow-[0_0_80px_rgba(34,211,238,0.15)] rounded-lg relative overflow-hidden">

                    {/* Background Glitch Effects */}
                    <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_center,_var(--tw-gradient-stops))] from-cyan-900/10 via-transparent to-transparent pointer-events-none" />

                    {/* Header: System Alert */}
                    <div className="flex justify-between items-center mb-10 px-2 opacity-90 border-b border-cyan-900/30 pb-2">
                        <div className="flex items-center gap-2 text-cyan-400 text-[10px] tracking-[0.2em] font-mono font-bold uppercase">
                            <span>!</span> SYSTEM NOTIFICATION
                        </div>
                        {/* Loading bar decoration */}
                        <div className="w-12 h-1 bg-cyan-900/50 rounded-full overflow-hidden">
                            <div className="h-full w-1/2 bg-cyan-400 animate-pulse" />
                        </div>
                    </div>

                    <div className="text-[10px] text-cyan-700 tracking-[0.3em] font-mono mb-6 uppercase">
                        ID: 8829-PROMOTION-CHAMBER
                    </div>

                    {/* Typography: Player Promotion Qualified */}
                    <div className="relative mb-12 transform scale-y-110">
                        <h1 className="text-5xl md:text-6xl font-black text-white italic tracking-tighter mb-0 drop-shadow-[0_0_15px_rgba(255,255,255,0.5)] font-sans" style={{ fontFamily: 'Inter, sans-serif' }}>
                            PLAYER
                        </h1>
                        <h1 className="text-5xl md:text-6xl font-black text-cyan-400 italic tracking-tighter drop-shadow-[0_0_20px_rgba(34,211,238,0.8)] font-sans mt-[-5px]" style={{ fontFamily: 'Inter, sans-serif' }}>
                            PROMOTION
                        </h1>

                        {/* Divider with 'QUALIFIED' */}
                        <div className="flex items-center justify-center gap-4 mt-6">
                            <div className="h-[1px] w-12 bg-gradient-to-l from-cyan-500 to-transparent" />
                            <span className="text-cyan-400 font-mono tracking-[0.5em] text-xs font-bold uppercase glitch-text" data-text="QUALIFIED">
                                QUALIFIED
                            </span>
                            <div className="h-[1px] w-12 bg-gradient-to-r from-cyan-500 to-transparent" />
                        </div>
                    </div>

                    {/* Description */}
                    {/* Description */}
                    <div className="mb-10 relative">
                        {/* Left/Right decorators */}
                        <div className="absolute top-0 left-0 bottom-0 w-[1px] bg-gradient-to-b from-transparent via-cyan-900/50 to-transparent -ml-8" />
                        <div className="absolute top-0 right-0 bottom-0 w-[1px] bg-gradient-to-b from-transparent via-cyan-900/50 to-transparent -mr-8" />

                        <p className="text-cyan-100/80 text-sm md:text-base leading-relaxed max-w-lg mx-auto font-mono uppercase tracking-wide">
                            The requirements for promotion have been met. The gate to the Trial Chamber is now open.
                        </p>
                        <p className="text-[10px] text-cyan-900 mt-4 uppercase tracking-widest">
                            Warning: Leaving the chamber is restricted until completion.
                        </p>
                    </div>

                    {/* Stats Grid */}
                    <div className="grid grid-cols-2 gap-px bg-cyan-900/30 mb-12 border border-cyan-900/30">
                        <div className="bg-[#050b14]/90 p-6 flex flex-col items-center justify-center relative overflow-hidden group">
                            <div className="absolute inset-0 bg-cyan-500/5 opacity-0 group-hover:opacity-100 transition-opacity" />
                            <span className="text-[10px] text-cyan-700 uppercase tracking-widest mb-2 font-mono">Current Grade</span>
                            <div className="text-2xl font-bold text-gray-400 font-sans tracking-tight drop-shadow-md">
                                E-RANK
                            </div>
                        </div>
                        <div className="bg-cyan-900/20 p-6 flex flex-col items-center justify-center relative border border-cyan-500/20 shadow-[inset_0_0_20px_rgba(34,211,238,0.1)]">
                            <span className="text-[10px] text-cyan-400 uppercase tracking-widest mb-2 font-mono">Promotion Target</span>
                            <div className="text-2xl font-bold text-cyan-400 font-sans tracking-tight drop-shadow-[0_0_10px_rgba(34,211,238,0.8)] flex items-center gap-2">
                                D-RANK <span className="text-xs animate-bounce">⏫</span>
                            </div>
                        </div>
                    </div>

                    {/* Action Button */}
                    <div className="flex flex-col items-center gap-6">
                        <SystemButton
                            variant="primary"
                            size="lg"
                            className="w-full max-w-sm h-16 text-lg italic font-black tracking-[0.1em] !bg-cyan-500 !text-black !border-none !shadow-[0_0_30px_rgba(34,211,238,0.6)] hover:!shadow-[0_0_50px_rgba(34,211,238,0.8)] hover:!scale-105 hover:!bg-cyan-400 transition-all duration-300 clip-path-slant"
                            onClick={() => { }} // Placeholder
                        >
                            <span className="mr-2">INITIATE TRIAL</span> <span className="text-xl">⚡</span>
                        </SystemButton>

                        <div className="flex items-center gap-4 text-[10px] text-cyan-900 font-mono tracking-widest uppercase">
                            <div className="h-[1px] w-8 bg-cyan-900/50" />
                            PROTOCOL X-99 ACTIVE
                            <div className="h-[1px] w-8 bg-cyan-900/50" />
                        </div>
                    </div>

                    {/* Side Decorators (Vertical Text) */}
                    <div className="absolute top-1/2 left-4 transform -translate-y-1/2 -rotate-90 text-[10px] text-blue-900/50 font-mono tracking-widest pointer-events-none">
                        SYS_OVERRIDE_ACTIVE
                    </div>
                    <div className="absolute top-1/2 right-4 transform -translate-y-1/2 rotate-90 text-[10px] text-cyan-900/50 font-mono tracking-widest pointer-events-none">
                        TRIAL_TYPE: COMBAT
                    </div>

                    {/* Bottom Status Bar */}
                    <div className="absolute bottom-0 left-0 right-0 h-8 bg-cyan-950/30 border-t border-cyan-900/50 flex items-center justify-between px-4 text-[8px] text-cyan-600 font-mono tracking-widest uppercase">
                        <span>COORD: 37.5665° N, 126.9780° E</span>
                        <span className="animate-pulse">CONNECTED</span>
                        <span>SYNC_LEVEL: 100%</span>
                    </div>

                </div>
            </div>
        </div>
    );
}
