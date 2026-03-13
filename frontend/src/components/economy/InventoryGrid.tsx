import React from 'react';
import { Package, Zap } from 'lucide-react';

interface InventoryItem {
    id: string;
    name: string;
    description: string;
    quantity: number;
    itemCode: string;
}

interface InventoryGridProps {
    items: InventoryItem[];
    onUseItem: (itemCode: string) => void;
}

export const InventoryGrid: React.FC<InventoryGridProps> = ({ items, onUseItem }) => {
    if (items.length === 0) {
        return (
            <div className="flex flex-col items-center justify-center p-12 border-2 border-dashed border-solo-blue-900/30 rounded-lg bg-gray-900/50">
                <Package size={48} className="text-solo-blue-900 mb-4" />
                <p className="text-gray-500 font-mono">Inventory Empty</p>
            </div>
        );
    }

    return (
        <div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-5 gap-4">
            {items.map((item) => (
                <div
                    key={item.id}
                    className="relative group bg-gray-900 border border-solo-blue-900/30 rounded-lg p-4 hover:border-solo-blue-500/50 transition-all hover:shadow-glow-cyan"
                >
                    {/* Quantity Badge */}
                    <div className="absolute top-2 right-2 bg-solo-blue-900/80 text-solo-blue-100 text-xs font-mono px-2 py-0.5 rounded border border-solo-blue-700">
                        x{item.quantity}
                    </div>

                    {/* Icon Placeholder */}
                    <div className="flex justify-center mb-3 mt-2">
                        <div className="w-12 h-12 rounded-full bg-solo-blue-900/20 flex items-center justify-center text-solo-blue-400 group-hover:bg-solo-blue-500/20 group-hover:text-solo-blue-200 transition-colors">
                            <Package size={24} />
                        </div>
                    </div>

                    {/* Details */}
                    <h4 className="text-center text-sm font-bold text-gray-200 mb-1 truncate">{item.name}</h4>
                    <p className="text-center text-[10px] text-gray-500 mb-3 h-8 overflow-hidden">{item.description}</p>

                    {/* Action */}
                    <button
                        onClick={() => onUseItem(item.itemCode)}
                        className="w-full flex items-center justify-center py-1.5 rounded bg-solo-blue-900/30 text-solo-blue-400 text-xs font-medium hover:bg-solo-blue-600 hover:text-white transition-colors border border-transparent hover:border-solo-blue-400/50"
                    >
                        <Zap size={12} className="mr-1" />
                        USE
                    </button>
                </div>
            ))}
        </div>
    );
};
