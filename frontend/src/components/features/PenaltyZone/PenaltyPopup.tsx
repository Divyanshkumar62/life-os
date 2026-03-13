import React from 'react';
import { motion, AnimatePresence } from 'framer-motion';

interface PenaltyPopupProps {
    isOpen: boolean;
    onClose: () => void;
}

export const PenaltyPopup: React.FC<PenaltyPopupProps> = ({ isOpen, onClose: _onClose }) => {
    return (
        <AnimatePresence>
            {isOpen && (
                <motion.div
                    initial={{ backdropFilter: "blur(0px)", backgroundColor: "rgba(58,6,6,0)" }}
                    animate={{ backdropFilter: "blur(10px)", backgroundColor: "rgba(58,6,6,0.95)" }}
                    exit={{ backdropFilter: "blur(0px)", backgroundColor: "rgba(58,6,6,0)" }}
                    className="fixed inset-0 flex flex-col items-center justify-center z-50 animate-pulse-glow"
                >
                    <motion.div
                        initial={{ y: "-100vh", opacity: 0 }}
                        animate={{ y: 0, opacity: 1 }}
                        exit={{ scale: 0.8, opacity: 0 }}
                        transition={{ type: "spring", bounce: 0.4 }}
                        className="bg-black/80 border border-solo-red-600 p-10 rounded text-solo-red-100 max-w-lg text-center shadow-[0_0_50px_rgba(220,38,38,0.3)]"
                    >
                        <h2 className="text-4xl font-black mb-2 tracking-[0.3em] text-solo-red-500">PENALTY ZONE</h2>
                        <h3 className="text-xl font-bold mb-6 tracking-widest text-gray-300">ACTIVE</h3>

                        <p className="mb-8 text-gray-400 leading-relaxed font-mono text-sm">
                            You have failed to complete the daily requirements.
                            The System is imposing a penalty. You will be transported to the Penalty Zone.
                            <br /><br />
                            Survive for the required duration to return to the real world.
                        </p>

                        <button
                            onClick={() => console.log('Initiating Survival Quest...')}
                            className="w-full bg-solo-red-900/30 hover:bg-solo-red-600/50 border border-solo-red-500 text-solo-red-100 font-bold py-4 px-6 rounded transition-all tracking-widest uppercase hover:shadow-[0_0_15px_rgba(220,38,38,0.5)]"
                        >
                            Accept Survival Quest
                        </button>
                    </motion.div>
                </motion.div>
            )}
        </AnimatePresence>
    );
};
