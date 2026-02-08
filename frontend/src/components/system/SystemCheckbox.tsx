import type { InputHTMLAttributes } from 'react';
import { clsx } from 'clsx';

export interface SystemCheckboxProps extends Omit<InputHTMLAttributes<HTMLInputElement>, 'type' | 'onChange'> {
    label?: string;
    checked?: boolean;
    onCheckedChange?: (checked: boolean) => void;
}

/**
 * SystemCheckbox - Custom styled checkbox
 * 
 * Responsibilities:
 * - Boolean input with purple accent
 * - Optional label
 * - Accessible keyboard navigation
 */
export function SystemCheckbox({
    label,
    checked = false,
    onCheckedChange,
    disabled,
    className,
    ...props
}: SystemCheckboxProps) {
    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        onCheckedChange?.(e.target.checked);
    };

    return (
        <label
            className={clsx(
                'inline-flex items-center gap-2 cursor-pointer',
                disabled && 'opacity-50 cursor-not-allowed',
                className
            )}
        >
            <div className="relative">
                <input
                    type="checkbox"
                    checked={checked}
                    onChange={handleChange}
                    disabled={disabled}
                    className="sr-only peer"
                    {...props}
                />

                {/* Custom Checkbox */}
                <div
                    className={clsx(
                        'w-5 h-5 rounded border-2 transition-smooth',
                        'peer-checked:bg-cyan-600 peer-checked:border-cyan-500',
                        'peer-focus:ring-2 peer-focus:ring-cyan-500 peer-focus:ring-offset-2 peer-focus:ring-offset-black',
                        !checked && 'bg-gray-800 border-gray-600'
                    )}
                >
                    {/* Checkmark */}
                    {checked && (
                        <svg
                            className="w-full h-full text-white"
                            viewBox="0 0 20 20"
                            fill="none"
                            xmlns="http://www.w3.org/2000/svg"
                        >
                            <path
                                d="M16 6L7.5 14.5L4 11"
                                stroke="currentColor"
                                strokeWidth="2"
                                strokeLinecap="round"
                                strokeLinejoin="round"
                            />
                        </svg>
                    )}
                </div>
            </div>

            {/* Label */}
            {label && (
                <span className="text-sm text-gray-300 select-none">
                    {label}
                </span>
            )}
        </label>
    );
}
