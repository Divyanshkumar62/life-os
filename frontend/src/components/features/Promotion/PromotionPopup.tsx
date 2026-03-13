import React from 'react';
import { motion, AnimatePresence } from 'framer-motion';

interface PromotionPopupProps {
    isOpen: boolean;
    onClose: () => void;
}

export const PromotionPopup: React.FC<PromotionPopupProps> = ({ isOpen, onClose }) => {
    return (
        <AnimatePresence>
            {isOpen && (
                <motion.div
                    initial={{ backdropFilter: "blur(0px)", backgroundColor: "rgba(0,0,0,0)" }}
                    animate={{ backdropFilter: "blur(10px)", backgroundColor: "rgba(0,0,0,0.8)" }}
                    exit={{ backdropFilter: "blur(0px)", backgroundColor: "rgba(0,0,0,0)" }}
                    className="fixed inset-0 flex items-center justify-center z-50"
                >
                    <motion.div
                        initial={{ y: "-100vh", opacity: 0 }}
                        animate={{ y: 0, opacity: 1 }}
                        exit={{ scale: 0.8, opacity: 0 }}
                        transition={{ type: "spring", bounce: 0.4 }}
                        className="bg-blue-900/40 border border-blue-500 p-6 rounded text-blue-100"
                    >
                        <h2 className="text-xl font-bold mb-4">PROMOTION AVAILABLE</h2>
                        <button onClick={onClose} className="bg-blue-600 px-4 py-2 rounded">Close</button>
                    </motion.div>
                </motion.div>
            )}
        </AnimatePresence>
    );
};
