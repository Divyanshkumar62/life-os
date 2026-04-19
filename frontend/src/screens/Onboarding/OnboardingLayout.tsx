import React from 'react';

interface OnboardingLayoutProps {
    children: React.ReactNode;
}

export const OnboardingLayout: React.FC<OnboardingLayoutProps> = ({ children }) => {
    return (
        <div className="min-h-screen bg-[#020617] text-slate-100 flex items-center justify-center relative overflow-hidden font-sans selection:bg-[#0ea5e9]/30">

            {/* Ambient Background Glows */}
            <div className="absolute top-[-20%] left-[-10%] w-[500px] h-[500px] bg-blue-600/20 rounded-full blur-[120px] pointer-events-none" />
            <div className="absolute bottom-[-20%] right-[-10%] w-[600px] h-[600px] bg-cyan-600/10 rounded-full blur-[100px] pointer-events-none" />

            {/* Main Content Area */}
            <div className="relative z-10 w-full max-w-4xl p-4">
                {children}
            </div>

            {/* System Version Watermark */}
            <div className="absolute bottom-4 left-6 text-slate-600 font-mono text-xs">
                SYSTEM.OS_VER.2.0 // AWAKENING_PROTOCOL
            </div>
        </div>
    );
};
