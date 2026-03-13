import React from 'react';
import { Lock, ShoppingBag } from 'lucide-react';

interface ShopItemProps {
    name: String;
    description: String;
    cost: number;
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
    rankRequirement,
    stockLimit,
    isLocked = false,
    onPurchase
}) => {
    return (
        <div className={`relative group p-[1px] rounded-lg overflow-hidden transition-all duration-300 ${isLocked ? 'opacity-70 grayscale' : 'hover:shadow-glow-cyan'}`}>
            {/* Holographic Border Gradient */}
            <div className="absolute inset-0 bg-gradient-to-br from-solo-blue-500/50 via-transparent to-solo-blue-900/50 opacity-50 group-hover:opacity-100 transition-opacity" />

            <div className="relative bg-gray-900/90 backdrop-blur-md p-4 rounded-lg h-full flex flex-col border border-solo-blue-900/30 group-hover:border-solo-blue-500/50 transition-colors">
                {/* Header */}
                <div className="flex justify-between items-start mb-2">
                    <h3 className="text-solo-blue-100 font-bold text-lg tracking-wide uppercase font-sans">{name}</h3>
                    {rankRequirement && (
                        <span className={`text-xs px-2 py-0.5 rounded border ${isLocked ? 'border-red-500 text-red-400' : 'border-solo-blue-500 text-solo-blue-400'}`}>
                            {rankRequirement}
                        </span>
                    )}
                </div>

                {/* Description */}
                <p className="text-gray-400 text-sm mb-4 flex-grow font-mono">{description}</p>

                {/* Footer */}
                <div className="flex justify-between items-center mt-auto pt-3 border-t border-solo-blue-900/30">
                    <div className="flex items-center text-yellow-400 font-mono">
                        <span className="mr-1">🪙</span>
                        <span className="font-bold">{cost}</span>
                    </div>

                    <button
                        onClick={onPurchase}
                        disabled={isLocked}
                        className={`flex items-center px-3 py-1.5 rounded-md text-sm font-medium transition-all duration-200
              ${isLocked
                                ? 'bg-gray-800 text-gray-500 cursor-not-allowed'
                                : 'bg-solo-blue-900/50 text-solo-blue-300 hover:bg-solo-blue-600 hover:text-white hover:shadow-glow-cyan border border-solo-blue-700/50'
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
