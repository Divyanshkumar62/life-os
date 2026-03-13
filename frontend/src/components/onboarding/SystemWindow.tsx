import React from 'react';

interface SystemWindowProps {
    children: React.ReactNode;
    title?: string;
    subtitle?: string;
    className?: string;
    borderColor?: string;
}

export const SystemWindow: React.FC<SystemWindowProps> = ({
    children,
    title,
    subtitle,
    className = '',
    borderColor = 'border-[#0ea5e9]'
}) => {
    return (
        <div className={`relative bg-slate-900/80 backdrop-blur-md border ${borderColor} shadow-2xl overflow-hidden ${className}`}>
            {/* Header Bar */}
            {(title || subtitle) && (
                <div className="flex items-center justify-between px-6 py-3 border-b border-white/10 bg-black/20">
                    <div className="flex flex-col">
                        {title && <h2 className="text-[#0ea5e9] font-bold tracking-wider uppercase text-lg glow-text">{title}</h2>}
                        {subtitle && <span className="text-slate-400 text-xs font-mono">{subtitle}</span>}
                    </div>
                    {/* Decorative bits */}
                    <div className="flex gap-2">
                        <div className="w-2 h-2 bg-[#0ea5e9] rounded-full animate-pulse" />
                        <div className="w-2 h-2 bg-slate-700 rounded-full" />
                    </div>
                </div>
            )}

            {/* Content */}
            <div className="p-6 relative z-10">
                {children}
            </div>

            {/* Background Grid Pattern */}
            <div className="absolute inset-0 bg-[linear-gradient(rgba(14,165,233,0.03)_1px,transparent_1px),linear-gradient(90deg,rgba(14,165,233,0.03)_1px,transparent_1px)] bg-[size:20px_20px] pointer-events-none" />

            {/* Scanline Effect */}
            <div className="absolute inset-0 pointer-events-none bg-scanlines opacity-5" />
        </div>
    );
};
