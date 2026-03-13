import React from 'react';

interface ScreenFrameProps {
    children: React.ReactNode;
    className?: string;
    onBack?: () => void;
}

export const ScreenFrame: React.FC<ScreenFrameProps> = ({ children, className = '', onBack }) => {
    return (
        <div className={`relative min-h-screen bg-[#020617] text-white p-4 ${className}`}>
            {onBack && (
                <button
                    onClick={onBack}
                    className="absolute top-4 left-4 z-50 text-slate-400 hover:text-white"
                >
                    ← BACK
                </button>
            )}
            {children}
        </div>
    );
};
