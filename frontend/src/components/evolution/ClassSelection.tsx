import { useState } from 'react';
import { TerminalCard } from '../system/TerminalCard';
import { SystemButton } from '../system/SystemButton';
import { clsx } from 'clsx';

export interface ClassSelectionProps {
    onSelectClass?: (jobClass: string) => void;
}

interface ClassOption {
    key: string;
    title: string;
    description: string;
    perk: string;
    perkDesc: string;
    recommended?: boolean;
    colorTheme: 'vanguard' | 'scholar' | 'shadow';
}

const CLASS_OPTIONS: ClassOption[] = [
    {
        key: 'VANGUARD',
        title: 'Vanguard',
        description: 'Musculoskeletal fortress optimized for raw power, durability, and burst speed calibration.',
        perk: '+15% PHYSICAL XP',
        perkDesc: 'Boosts gains from daily training sprints and muscle stress calibrations.',
        colorTheme: 'vanguard'
    },
    {
        key: 'SCHOLAR',
        title: 'Scholar',
        description: 'Arcane strategist balancing cognitive deflection shields with high-output focus matrices.',
        perk: '+15% COGNITIVE XP',
        perkDesc: 'Enhances cognitive pathway processing and intellectual calibration outputs.',
        colorTheme: 'scholar'
    },
    {
        key: 'SHADOW_NECROMANCER',
        title: 'Shadow Necromancer',
        description: 'Sovereign commander mapping the void to extract and bind constructs. Monarch class lineage.',
        perk: '+15% SOUL RESONANCE',
        perkDesc: 'Grants deep link connection to the graveyard archives and extractions.',
        recommended: true,
        colorTheme: 'shadow'
    }
];

/**
 * ClassSelection - Class Selection Interface featuring three-column layout.
 */
export function ClassSelection({ onSelectClass }: ClassSelectionProps) {
    const [selectedClass, setSelectedClass] = useState<string | null>(null);

    const handleSelect = (key: string) => {
        setSelectedClass(key);
        onSelectClass?.(key);
    };

    return (
        <div className="bg-[#05050A] text-[#E2E8F0] p-6 md:p-8 space-y-8 text-left font-space max-w-5xl mx-auto">
            {/* Header */}
            <div className="flex justify-between items-center border-b border-gray-800 pb-4">
                <div>
                    <span className="text-[10px] text-[#2563EB] tracking-[0.25em] font-bold uppercase font-mono">
                        EVOLUTION PATH UNLOCKED
                    </span>
                    <h2 className="text-headline-md font-bold tracking-widest text-[#E2E8F0] uppercase">
                        [ Class Selection ]
                    </h2>
                </div>
                <span className="text-xs text-[#2563EB] font-mono tracking-widest uppercase animate-pulse">
                    LINK: LOCKED_AWAITING_CHOICE
                </span>
            </div>

            {/* Three-Column Cards Grid */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-8 pt-4">
                {CLASS_OPTIONS.map((item) => {
                    const isSelected = selectedClass === item.key;
                    const isShadow = item.colorTheme === 'shadow';
                    const isScholar = item.colorTheme === 'scholar';
                    const isVanguard = item.colorTheme === 'vanguard';

                    // Variant mappings based on specs
                    const cardVariant = isSelected
                        ? (item.recommended ? 'active' : 'active')
                        : 'default';

                    return (
                        <div key={item.key} className="relative flex flex-col">
                            {/* Recommended badge */}
                            {item.recommended && (
                                <div className="absolute -top-3 left-1/2 -translate-x-1/2 z-10 bg-[#FBBF24] text-black text-[9px] font-bold tracking-[0.2em] px-3 py-1 border border-black uppercase font-mono">
                                    RECOMMENDED
                                </div>
                            )}

                            <TerminalCard
                                variant={cardVariant}
                                className={clsx(
                                    'flex-1 flex flex-col justify-between p-6 gap-6 relative border-t-2',
                                    // Custom colors mapping per specs
                                    isVanguard && 'border-t-[#E2E8F0]/50 hover:border-l-[#E2E8F0] hover:border-r-[#E2E8F0]',
                                    isScholar && 'border-t-[#22D3EE]/50 hover:border-l-[#22D3EE] hover:border-r-[#22D3EE]',
                                    isShadow && 'border-t-[#2563EB] shadow-[0_0_12px_rgba(37,99,235,0.15)] hover:shadow-[0_0_20px_rgba(37,99,235,0.3)]'
                                )}
                            >
                                <div className="space-y-6">
                                    {/* Class Icon & Title */}
                                    <div className="space-y-4">
                                        <div className={clsx(
                                            'w-16 h-16 border flex items-center justify-center bg-black/40',
                                            isVanguard && 'border-[#E2E8F0]/30 text-[#E2E8F0]',
                                            isScholar && 'border-[#22D3EE]/30 text-[#22D3EE]',
                                            isShadow && 'border-[#2563EB] shadow-[0_0_10px_rgba(37,99,235,0.3)] text-[#2563EB]'
                                        )}>
                                            {isVanguard && <VanguardIcon />}
                                            {isScholar && <ScholarIcon />}
                                            {isShadow && <ShadowIcon />}
                                        </div>

                                        <div>
                                            <h3 className={clsx(
                                                'text-headline-sm font-bold tracking-widest uppercase',
                                                isVanguard && 'text-[#E2E8F0]',
                                                isScholar && 'text-[#22D3EE] glow-text-cyan',
                                                isShadow && 'text-[#2563EB] glow-text-blue'
                                            )}>
                                                {item.title}
                                            </h3>
                                            <p className="text-body-sm text-gray-400 font-sans leading-relaxed mt-2">
                                                {item.description}
                                            </p>
                                        </div>
                                    </div>

                                    {/* Perks & Stats */}
                                    <div className="bg-black/40 border border-gray-800 p-4 font-mono space-y-2 text-left">
                                        <span className="block text-[9px] text-gray-500 uppercase tracking-widest">UNIQUE PERK</span>
                                        <span className={clsx(
                                            'text-xs font-bold block',
                                            isVanguard && 'text-[#E2E8F0]',
                                            isScholar && 'text-[#22D3EE]',
                                            isShadow && 'text-[#FBBF24] glow-text-gold'
                                        )}>
                                            {item.perk}
                                        </span>
                                        <p className="text-[10px] text-gray-500 font-sans leading-normal">
                                            {item.perkDesc}
                                        </p>
                                    </div>
                                </div>

                                {/* Select Button */}
                                <SystemButton
                                    variant={isShadow ? 'success' : 'primary'}
                                    fullWidth
                                    className="mt-4"
                                    onClick={() => handleSelect(item.key)}
                                >
                                    [ Select Class ]
                                </SystemButton>
                            </TerminalCard>
                        </div>
                    );
                })}
            </div>
        </div>
    );
}

// Inline SVGs for the class icons matching the custom SVG design specs
function VanguardIcon() {
    return (
        <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="square">
            <path d="M12 2L3 7v6c0 5.5 4.5 10 9 10s9-4.5 9-10V7l-9-5z" />
            <path d="M12 6v11M9 9h6M9 13h6" />
        </svg>
    );
}

function ScholarIcon() {
    return (
        <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="square">
            <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20M4 19.5A2.5 2.5 0 0 0 6.5 22H20M4 19.5V3.5A2.5 2.5 0 0 1 6.5 1H20v21H6.5" />
            <path d="M9 6h6M9 10h6M9 14h6" />
        </svg>
    );
}

function ShadowIcon() {
    return (
        <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="square">
            <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5" />
            <circle cx="12" cy="7" r="1.5" fill="currentColor" />
        </svg>
    );
}
