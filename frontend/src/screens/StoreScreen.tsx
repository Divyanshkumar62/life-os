import { useState, useEffect } from 'react';
import { ShopItemCard } from '../components/economy/ShopItemCard';
import { ShoppingBag, Loader, Lock } from 'lucide-react';
import { EconomyAPI } from '../api/api';
import { useSystemContext } from '../context/SystemContext';

interface ShopItem {
    itemId: string;
    code: string;
    name: string;
    description: string;
    cost: number;
    baseCost?: number;
    stockLimit?: number;
    rankRequirement?: string;
}

interface StoreScreenProps {
    playerId: string | null;
}

export const StoreScreen = ({ playerId }: StoreScreenProps) => {
    const [items, setItems] = useState<ShopItem[]>([]);
    const [loading, setLoading] = useState(true);
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    const [_purchasing, setPurchasing] = useState<string | null>(null);

    const { statusWindow, refreshSystem } = useSystemContext();
    const playerGold = statusWindow?.economy?.gold || 0;
    const playerRank = statusWindow?.identity?.rank || 'E-RANK';
    const playerLevel = statusWindow?.identity?.level || 1;

    const fetchData = async () => {
        if (!playerId) return;
        try {
            const shopItems = await EconomyAPI.fetchShopItems(playerId);
            setItems(shopItems);
        } catch (error) {
            console.error("Failed to load store:", error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchData();
    }, [playerId]);

    const handlePurchase = async (item: ShopItem) => {
        if (!playerId) return;
        if (playerGold < item.cost) {
            alert("Insufficient Funds!");
            return;
        }

        setPurchasing(item.code);
        try {
            await EconomyAPI.purchaseItem(playerId, item.code);
            await fetchData(); // Refresh to update stock
            await refreshSystem(); // Hard refresh global state to deduct gold natively
        } catch (error) {
            console.error("Purchase failed:", error);
            alert("Purchase failed. Check rank or stock limits.");
        } finally {
            setPurchasing(null);
        }
    };

    // Helper to check rank
    const isRankLocked = (req?: string) => {
        if (!req) return false;
        const ranks = ['E-RANK', 'D-RANK', 'C-RANK', 'B-RANK', 'A-RANK', 'S-RANK', 'SS-RANK'];
        const reqIndex = ranks.indexOf(req);
        const playerIndex = ranks.indexOf(playerRank);
        return playerIndex < reqIndex;
    };

    if (loading) {
        return (
            <div className="min-h-screen bg-black flex items-center justify-center text-solo-cyan">
                <Loader className="animate-spin mr-2" /> Loading System Store...
            </div>
        );
    }

    if (playerLevel < 10) {
        return (
            <div className="min-h-screen bg-black text-white p-6 relative overflow-hidden flex flex-col items-center justify-center">
                {/* Background Ambience */}
                <div className="absolute inset-0 bg-grid-pattern opacity-5 pointer-events-none" />
                <div className="absolute top-[50%] left-[50%] translate-x-[-50%] translate-y-[-50%] w-[500px] h-[500px] bg-red-950/20 rounded-full blur-[120px] pointer-events-none animate-pulse" />

                {/* Dark Glassmorphic Prison Layout */}
                <div className="relative z-10 text-center max-w-lg mx-auto p-10 rounded-none glass-panel border border-solo-red/40 shadow-[0_0_50px_rgba(255,0,60,0.15)] flex flex-col items-center">
                    <div className="w-24 h-24 bg-solo-red/30 border border-solo-red/40 rounded-full flex items-center justify-center mb-8 animate-pulse text-solo-red shadow-[0_0_30px_rgba(255,0,60,0.3)]">
                        <Lock size={48} className="stroke-[1.5]" />
                    </div>
                    
                    <h2 className="text-xs font-mono tracking-widest text-solo-red font-bold uppercase mb-3 animate-pulse">
                        [ ACCESS DENIED ]
                    </h2>
                    
                    <h1 className="text-3xl font-black italic tracking-wider text-transparent bg-clip-text bg-gradient-to-r from-white via-gray-200 to-solo-red mb-6 uppercase">
                        GATE LOCKED
                    </h1>
                    
                    <p className="text-sm text-solo-red/90 mb-8 leading-relaxed font-mono uppercase border border-solo-red/60 bg-solo-red/20 px-6 py-4 rounded-none shadow-inner">
                        GATE LOCKED: Unlocks at Level 10. Increase your hunter level to access the System Merchant.
                    </p>

                    <div className="flex items-center space-x-4 glass-panel border border-solo-red/50 px-6 py-3 rounded-none shadow-glow-red">
                        <span className="text-xs font-mono text-gray-500">CURRENT STATUS:</span>
                        <span className="text-sm font-bold text-solo-red font-mono">Lvl {playerLevel} / 10</span>
                    </div>

                    <div className="mt-8 text-[10px] font-mono text-gray-600 tracking-widest uppercase">
                        The System requires higher power to establish connection.
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-black text-white p-6 relative overflow-hidden">
            {/* Background Ambience */}
            <div className="absolute top-0 left-0 w-full h-full bg-grid-pattern opacity-10 pointer-events-none" />
            <div className="absolute top-[-10%] right-[-10%] w-[500px] h-[500px] bg-solo-cyan/20 rounded-full blur-3xl pointer-events-none" />

            {/* Header */}
            <header className="relative z-10 flex items-center justify-between mb-8 border-b border-solo-cyan/50 pb-4">
                <div className="flex items-center">
                    <div className="ml-12"> {/* Spacing for Back button */}
                        <h1 className="text-3xl font-black italic tracking-wider text-transparent bg-clip-text bg-gradient-to-r from-white to-solo-cyan">
                            SYSTEM STORE
                        </h1>
                        <p className="text-sm text-solo-cyan font-mono tracking-widest">Buy equipment and upgrades.</p>
                    </div>
                </div>

                {/* Currency Display */}
                <div className="flex items-center glass-panel border border-solo-cyan/50 px-4 py-2 rounded-none system-glow">
                    <ShoppingBag size={20} className="text-solo-gold mr-2" />
                    <div className="text-right">
                        <p className="text-[10px] text-gray-400 uppercase tracking-widest">Current Funds</p>
                        <p className="text-xl font-bold text-solo-gold font-mono">{playerGold.toLocaleString()} G</p>
                    </div>
                </div>
            </header>

            {/* Content */}
            <div className="relative z-10 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
                {items.map((item) => {
                    const locked = isRankLocked(item.rankRequirement);
                    return (
                        <ShopItemCard
                            key={item.itemId}
                            name={item.name}
                            description={item.description}
                            cost={item.cost}
                            baseCost={item.baseCost}
                            rankRequirement={item.rankRequirement}
                            stockLimit={item.stockLimit}
                            isLocked={locked}
                            onPurchase={() => handlePurchase(item)}
                        />
                    );
                })}
            </div>
        </div>
    );
};
