import type { ReactNode } from 'react';
import { clsx } from 'clsx';

export interface Tab {
    id: string;
    label: string;
    icon?: ReactNode;
    badge?: number;
}

export interface SystemTabProps {
    tabs: Tab[];
    activeTab: string;
    onTabChange: (tabId: string) => void;
    className?: string;
}

/**
 * SystemTab - Tab navigation component
 * 
 * Responsibilities:
 * - Switch between different views
 * - Show active state with purple indicator
 * - Support icons and badges
 */
export function SystemTab({
    tabs,
    activeTab,
    onTabChange,
    className,
}: SystemTabProps) {
    return (
        <div className={clsx('flex gap-1 border-b border-gray-700', className)}>
            {tabs.map((tab) => {
                const isActive = tab.id === activeTab;

                return (
                    <button
                        key={tab.id}
                        onClick={() => onTabChange(tab.id)}
                        className={clsx(
                            'flex items-center gap-2 px-4 py-2',
                            'text-sm font-semibold uppercase tracking-wide',
                            'border-b-2 transition-smooth',
                            'focus:outline-none focus:ring-2 focus:ring-cyan-500 focus:ring-offset-2 focus:ring-offset-black',
                            isActive
                                ? 'text-cyan-400 border-cyan-500'
                                : 'text-gray-400 border-transparent hover:text-gray-300 hover:border-gray-600'
                        )}
                    >
                        {/* Icon */}
                        {tab.icon && <span className="text-base">{tab.icon}</span>}

                        {/* Label */}
                        <span>{tab.label}</span>

                        {/* Badge */}
                        {tab.badge !== undefined && tab.badge > 0 && (
                            <span
                                className={clsx(
                                    'px-1.5 py-0.5 rounded-full text-xs font-bold',
                                    isActive
                                        ? 'bg-cyan-500 text-white'
                                        : 'bg-gray-700 text-gray-400'
                                )}
                            >
                                {tab.badge}
                            </span>
                        )}
                    </button>
                );
            })}
        </div>
    );
}
