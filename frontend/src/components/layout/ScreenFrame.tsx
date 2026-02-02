import type { ReactNode } from 'react';
import { clsx } from 'clsx';

export interface ScreenFrameProps {
    children: ReactNode;
    className?: string;
}

/**
 * ScreenFrame - Full-screen container with padding
 * 
 * Responsibilities:
 * - Provide page-level layout structure
 * - Apply consistent padding
 * - Handle overflow
 */
export function ScreenFrame({
    children,
    className,
}: ScreenFrameProps) {
    return (
        <div
            className={clsx(
                'h-screen w-full bg-black',
                'p-4 md:p-6',
                'overflow-y-auto',
                className
            )}
        >
            {children}
        </div>
    );
}
