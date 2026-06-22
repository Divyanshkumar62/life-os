import React from 'react';
import { Lock, ShoppingBag } from 'lucide-react';

interface ShopItemProps {
    name: String;
    description: String;
    cost: number;
    baseCost?: number;
    rankRequirement?: String; // e.g. "C-Rank"
    stockLimit?: number;
    imageUrl?: string;
    isLocked?: boolean;
    onPurchase: () => void;
}

export const ShopItemCard: React.FC<ShopItemProps> = ({
    name,
    description,
    cost,
    baseCost,
    rankRequirement,
    stockLimit,
    isLocked = false,
    onPurchase
}) => {
    const hasDiscount = baseCost !== undefined && baseCost > cost;

    return (
        <div className={`relative group p-[1px] rounded-none overflow-hidden transition-all duration-300 ${isLocked ? 'opacity-70 grayscale' : 'hover:system-glow'}`}>
            {/* Holographic Border Gradient */}
            <div className="absolute inset-0 bg-gradient-to-br from-solo-cyan/50 via-transparent to-solo-cyan/50 opacity-50 group-hover:opacity-100 transition-opacity" />

            <div className="relative glass-panel p-4 rounded-none h-full flex flex-col border border-solo-cyan/30 group-hover:border-solo-cyan transition-colors">
                {/* Header */}
                <div className="flex justify-between items-start mb-2">
                    <h3 className="text-white font-bold text-lg tracking-widest uppercase font-mono">{name}</h3>
                    {rankRequirement && (
                        <span className={`text-xs px-2 py-0.5 rounded-none border ${isLocked ? 'border-solo-red text-solo-red' : 'border-solo-cyan text-solo-cyan'}`}>
                             {rankRequirement}
                        </span>
                    )}
                </div>

                {/* Description */}
                <p className="text-gray-400 text-sm mb-4 flex-grow font-mono">{description}</p>

                {/* Footer */}
                <div className="flex justify-between items-center mt-auto pt-3 border-t border-solo-cyan/30">
                    <div className="flex items-center font-mono">
                        <span className="mr-1">🪙</span>
                        {hasDiscount ? (
                            <div className="flex items-center space-x-2">
                                <span className="line-through text-gray-500 text-xs">{baseCost}</span>
                                <span className="text-solo-cyan font-bold">{cost}</span>
                            </div>
                        ) : (
                            <span className="text-solo-gold font-bold">{cost}</span>
                        )}
                    </div>

                    <button
                        onClick={onPurchase}
                        disabled={isLocked}
                        className={`flex items-center px-3 py-1.5 rounded-none text-sm font-medium transition-all duration-200
              ${isLocked
                                ? 'bg-gray-800 text-gray-500 cursor-not-allowed border border-gray-700'
                                : 'bg-solo-cyan/20 text-solo-cyan hover:bg-solo-cyan/50 hover:text-white hover:system-glow border border-solo-cyan/50'
                            }`}
                    >
                        {isLocked ? <Lock size={14} className="mr-1" /> : <ShoppingBag size={14} className="mr-1" />}
                        {isLocked ? 'LOCKED' : 'BUY'}
                    </button>
                </div>

                {/* Stock Limit Badge */}
                {stockLimit && (
                    <div className="absolute top-2 right-2 text-[10px] text-gray-500">
                        Stock: {stockLimit}
                    </div>
                )}
            </div>
        </div>
    );
};
