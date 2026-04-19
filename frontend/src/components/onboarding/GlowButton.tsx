import React from 'react';

interface GlowButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
    variant?: 'primary' | 'secondary' | 'danger' | 'ghost';
    fullWidth?: boolean;
    className?: string;
    pulsating?: boolean;
}

export const GlowButton: React.FC<GlowButtonProps> = ({
    children,
    variant = 'primary',
    fullWidth = false,
    className = '',
    pulsating = false,
    ...props
}) => {
    const baseStyles = "relative px-6 py-3 font-mono font-bold uppercase transition-all duration-300 border backdrop-blur-sm group overflow-hidden";

    const variants = {
        primary: "bg-[#0ea5e9]/10 border-[#0ea5e9] text-[#0ea5e9] hover:bg-[#0ea5e9]/20 hover:shadow-[0_0_20px_rgba(14,165,233,0.4)]",
        secondary: "bg-slate-800/50 border-slate-600 text-slate-300 hover:bg-slate-700/50 hover:border-slate-400",
        danger: "bg-red-500/10 border-red-500 text-red-500 hover:bg-red-500/20 hover:shadow-[0_0_20px_rgba(239,68,68,0.4)]",
        ghost: "bg-transparent border-transparent text-slate-400 hover:text-white"
    };

    const pulseEffect = pulsating ? "animate-pulse shadow-[0_0_15px_rgba(14,165,233,0.5)]" : "";
    const widthClass = fullWidth ? "w-full" : "";

    return (
        <button
            className={`${baseStyles} ${variants[variant]} ${pulseEffect} ${widthClass} ${className}`}
            {...props}
        >
            {/* Glitch Effect Overlay on Hover */}
            <span className="absolute inset-0 w-full h-full bg-white/5 opacity-0 group-hover:opacity-100 transition-opacity" />

            {/* Corner Accents */}
            <span className="absolute top-0 left-0 w-2 h-2 border-t-2 border-l-2 border-current opacity-70" />
            <span className="absolute bottom-0 right-0 w-2 h-2 border-b-2 border-r-2 border-current opacity-70" />

            {children}
        </button>
    );
};
