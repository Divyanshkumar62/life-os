import type { ReactNode } from 'react';
import { clsx } from 'clsx';

export interface GridLayoutProps {
    children: ReactNode;
    columns?: 1 | 2 | 3 | 4;
    gap?: 'sm' | 'md' | 'lg';
    className?: string;
}

/**
 * GridLayout - Responsive grid container
 * 
 * Responsibilities:
 * - Provide consistent grid structure
 * - Support responsive breakpoints
 * - Flexible column configuration
 */
export function GridLayout({
    children,
    columns = 3,
    gap = 'md',
    className,
}: GridLayoutProps) {
    const columnClasses = {
        1: 'grid-cols-1',
        2: 'grid-cols-1 lg:grid-cols-2',
        3: 'grid-cols-1 lg:grid-cols-3',
        4: 'grid-cols-1 md:grid-cols-2 lg:grid-cols-4',
    };

    const gapClasses = {
        sm: 'gap-2',
        md: 'gap-4',
        lg: 'gap-6',
    };

    return (
        <div
            className={clsx(
                'grid',
                columnClasses[columns],
                gapClasses[gap],
                className
            )}
        >
            {children}
        </div>
    );
}
