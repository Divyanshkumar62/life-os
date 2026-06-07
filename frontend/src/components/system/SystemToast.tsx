import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { systemMessageEmitter } from '../../api/api';

interface ToastMessage {
    id: string;
    text: string;
}

export const SystemToast: React.FC = () => {
    const [toasts, setToasts] = useState<ToastMessage[]>([]);

    useEffect(() => {
        const unsubscribe = systemMessageEmitter.subscribe((messages) => {
            const newToasts = messages.map((msg) => ({
                id: Math.random().toString(36).substring(2, 9),
                text: msg,
            }));
            setToasts((prev) => [...prev, ...newToasts]);
        });

        return () => {
            unsubscribe();
        };
    }, []);

    const removeToast = (id: string) => {
        setToasts((prev) => prev.filter((t) => t.id !== id));
    };

    return (
        <div className="fixed top-4 right-4 z-[9999] flex flex-col gap-3 max-w-sm w-full pointer-events-none">
            <AnimatePresence>
                {toasts.map((toast) => (
                    <SystemToastItem
                        key={toast.id}
                        toast={toast}
                        onClose={() => removeToast(toast.id)}
                    />
                ))}
            </AnimatePresence>
        </div>
    );
};

interface SystemToastItemProps {
    toast: ToastMessage;
    onClose: () => void;
}

const SystemToastItem: React.FC<SystemToastItemProps> = ({ toast, onClose }) => {
    useEffect(() => {
        const timer = setTimeout(() => {
            onClose();
        }, 5000); // Auto close after 5 seconds

        return () => clearTimeout(timer);
    }, [onClose]);

    return (
        <motion.div
            initial={{ opacity: 0, x: 100, y: -20, scale: 0.9 }}
            animate={{ opacity: 1, x: 0, y: 0, scale: 1 }}
            exit={{ opacity: 0, x: 100, scale: 0.9, transition: { duration: 0.2 } }}
            className="pointer-events-auto bg-black/90 border border-cyan-500 rounded p-4 shadow-glow-cyan flex flex-col gap-1 font-mono select-none"
        >
            <div className="flex justify-between items-center border-b border-cyan-950 pb-1.5 mb-1.5">
                <span className="text-[10px] font-bold text-cyan-400 tracking-[0.2em] uppercase">
                    [ SYSTEM ALERT ]
                </span>
                <button
                    onClick={onClose}
                    className="text-gray-500 hover:text-white transition-colors text-xs font-sans"
                >
                    ✕
                </button>
            </div>
            <p className="text-xs text-white leading-relaxed tracking-wide whitespace-pre-wrap uppercase">
                {toast.text}
            </p>
        </motion.div>
    );
};
