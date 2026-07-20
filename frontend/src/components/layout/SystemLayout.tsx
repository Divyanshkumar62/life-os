import type { ReactNode } from 'react';
import { clsx } from 'clsx';

export interface SystemLayoutProps {
    children: ReactNode;
    activeRoute: string;
    setActiveRoute: (route: string) => void;
    onTriggerPunish?: () => void;
}

const NAV_ITEMS = [
    { key: 'dashboard', label: 'DASHBOARD', icon: 'grid_view' },
    { key: 'intel', label: 'SYSTEM INTEL', icon: 'psychology' },
    { key: 'gates', label: 'GATE HUB', icon: 'sensor_door' },
    { key: 'evolution', label: 'CLASS SELECTION', icon: 'award_star' },
    { key: 'analytics', label: 'TELEMETRY HUB', icon: 'analytics' },
];

/**
 * SystemLayout - High-performance sovereign HUD navigation frame.
 */
export function SystemLayout({
    children,
    activeRoute,
    setActiveRoute,
    onTriggerPunish,
}: SystemLayoutProps) {
    return (
        <div className="min-h-screen bg-[#05050A] text-[#E2E8F0] font-space flex relative overflow-hidden text-left selection:bg-[#2563EB]/30 selection:text-[#2563EB]">
            {/* Background Atmosphere */}
            <div className="absolute inset-0 pointer-events-none opacity-5 bg-repeat bg-striped" />
            <div className="absolute bottom-0 left-0 w-96 h-96 bg-[#2563EB]/5 blur-[120px] rounded-full pointer-events-none" />

            {/* Pinned Left Sidebar (HUD navigation) */}
            <aside className="w-64 border-r border-gray-800 bg-[#0e0e14]/90 backdrop-blur-2xl flex flex-col justify-between shrink-0 z-40 relative">
                {/* Header Section */}
                <div className="p-6 border-b border-gray-800">
                    <span className="text-[10px] text-[#2563EB] tracking-[0.25em] font-bold uppercase font-mono block mb-1">
                        SOVEREIGN LINK v0.1
                    </span>
                    <h1 className="text-headline-sm font-bold tracking-widest text-[#E2E8F0] uppercase">
                        SHADOW HUD
                    </h1>
                </div>

                {/* Nav Links */}
                <nav className="p-4 flex-1 space-y-2 font-mono">
                    {NAV_ITEMS.map((item) => {
                        const isActive = activeRoute === item.key;
                        return (
                            <button
                                key={item.key}
                                onClick={() => setActiveRoute(item.key)}
                                className={clsx(
                                    'w-full flex items-center gap-3 px-4 py-3 text-xs tracking-wider uppercase text-left transition-all duration-300 border-l-2',
                                    isActive
                                        ? 'bg-[#2563EB]/10 border-[#2563EB] text-[#E2E8F0] font-bold'
                                        : 'bg-transparent border-transparent text-gray-500 hover:text-[#E2E8F0] hover:bg-gray-800/20'
                                )}
                            >
                                <span className="material-symbols-outlined text-sm">{item.icon}</span>
                                <span>{item.label}</span>
                            </button>
                        );
                    })}
                </nav>

                {/* Footer Section */}
                <div className="p-6 border-t border-gray-800 space-y-3 bg-[#0e0e14]/50">
                    <button
                        onClick={onTriggerPunish}
                        className="w-full bg-[#EF4444]/10 hover:bg-[#EF4444] text-[#EF4444] hover:text-black border border-[#EF4444] transition-all duration-300 py-2 px-3 text-[11px] font-mono tracking-widest uppercase font-bold"
                    >
                        TRIGGER SYSTEM PUNISH
                    </button>
                    <div className="text-[9px] font-mono text-gray-600 uppercase text-center">
                        SECURE_HUD_UPLINK
                    </div>
                </div>
            </aside>

            {/* Main Canvas Area */}
            <div className="flex-1 flex flex-col min-w-0 min-h-screen relative overflow-y-auto">
                <main className="flex-1 p-6 md:p-8">
                    {children}
                </main>
            </div>
        </div>
    );
}
