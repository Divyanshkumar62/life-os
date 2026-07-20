import type { ReactNode, HTMLAttributes } from 'react';
import { clsx } from 'clsx';

export interface TerminalCardProps extends HTMLAttributes<HTMLDivElement> {
    children: ReactNode;
    variant?: 'default' | 'active' | 'alert' | 'success';
    className?: string;
}

/**
 * TerminalCard - Custom primitive container enforcing sharp chamfered corners and glows.
 */
export function TerminalCard({
    children,
    variant = 'default',
    className,
    style,
    ...props
}: TerminalCardProps) {
    const variantClasses = {
        default: 'border-secondary/30 bg-background text-on-surface',
        active: 'border-secondary bg-background text-on-surface shadow-[0_0_10px_rgba(37,99,235,0.2),inset_0_0_5px_rgba(37,99,235,0.1)]',
        alert: 'border-error bg-background text-error shadow-[0_0_15px_rgba(239,68,68,0.2),inset_0_0_5px_rgba(239,68,68,0.1)]',
        success: 'border-tertiary bg-background text-on-surface shadow-[0_0_15px_rgba(74,225,118,0.2),inset_0_0_5px_rgba(74,225,118,0.1)]',
    };

    return (
        <div
            className={clsx(
                'border p-6 rounded-none transition-all duration-300',
                variantClasses[variant],
                className
            )}
            style={{
                clipPath: 'polygon(10px 0, 100% 0, 100% calc(100% - 10px), calc(100% - 10px) 100%, 0 100%, 0 10px)',
                ...style
            }}
            {...props}
        >
            {children}
        </div>
    );
}
