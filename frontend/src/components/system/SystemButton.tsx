import type { ReactNode, ButtonHTMLAttributes } from 'react';
import { clsx } from 'clsx';

export interface SystemButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
    children: ReactNode;
    variant?: 'primary' | 'secondary' | 'ghost' | 'danger';
    size?: 'sm' | 'md' | 'lg';
    icon?: ReactNode;
    iconPosition?: 'left' | 'right';
    fullWidth?: boolean;
    loading?: boolean;
}

/**
 * SystemButton - Interactive action button
 * 
 * Responsibilities:
 * - Trigger user actions
 * - Support multiple visual styles
 * - Handle loading and disabled states
 */
export function SystemButton({
    children,
    variant = 'primary',
    size = 'md',
    icon,
    iconPosition = 'left',
    fullWidth = false,
    loading = false,
    disabled,
    className,
    ...props
}: SystemButtonProps) {
    const variantClasses = {
        primary: 'bg-solo-cyan hover:bg-solo-cyan/80 text-solo-bg border-solo-cyan system-glow',
        secondary: 'bg-gray-800 hover:bg-gray-700 text-white border-gray-600',
        ghost: 'bg-transparent hover:bg-gray-800 text-solo-cyan border-solo-cyan',
        danger: 'bg-solo-red/20 hover:bg-solo-red/30 text-solo-red border-solo-red',
    };

    const sizeClasses = {
        sm: 'text-xs px-3 py-1.5',
        md: 'text-sm px-4 py-2',
        lg: 'text-base px-6 py-3',
    };

    const isDisabled = disabled || loading;

    return (
        <button
            className={clsx(
                'inline-flex items-center justify-center gap-2',
                'rounded-none border font-semibold uppercase tracking-widest',
                'transition-smooth',
                'focus:outline-none focus:ring-2 focus:ring-solo-cyan focus:ring-offset-2 focus:ring-offset-black',
                variantClasses[variant],
                sizeClasses[size],
                fullWidth && 'w-full',
                isDisabled && 'opacity-50 cursor-not-allowed',
                !isDisabled && 'hover:scale-105 active:scale-95',
                className
            )}
            disabled={isDisabled}
            {...props}
        >
            {/* Loading Spinner */}
            {loading && (
                <svg
                    className="animate-spin h-4 w-4"
                    xmlns="http://www.w3.org/2000/svg"
                    fill="none"
                    viewBox="0 0 24 24"
                >
                    <circle
                        className="opacity-25"
                        cx="12"
                        cy="12"
                        r="10"
                        stroke="currentColor"
                        strokeWidth="4"
                    />
                    <path
                        className="opacity-75"
                        fill="currentColor"
                        d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                    />
                </svg>
            )}

            {/* Icon Left */}
            {!loading && icon && iconPosition === 'left' && <span>{icon}</span>}

            {/* Text */}
            <span>{children}</span>

            {/* Icon Right */}
            {!loading && icon && iconPosition === 'right' && <span>{icon}</span>}
        </button>
    );
}
