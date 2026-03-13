import { useState, useEffect, useRef } from 'react';

/**
 * Calculates the exact hours, minutes, and seconds remaining until a target ISO timestamp.
 * Triggers `onZero` exactly once when the countdown hits zero.
 */
export function useTemporalCountdown(deadlineAtISO: string | null | undefined, onZero?: () => void) {
    const [timeLeft, setTimeLeft] = useState('00:00:00');
    const hasTriggeredZero = useRef(false);

    useEffect(() => {
        if (!deadlineAtISO) {
            setTimeLeft('--:--:--');
            return;
        }

        const deadline = new Date(deadlineAtISO).getTime();

        const calculateTimeLeft = () => {
            const now = new Date().getTime();
            const difference = deadline - now;

            if (difference <= 0) {
                if (!hasTriggeredZero.current) {
                    hasTriggeredZero.current = true;
                    if (onZero) onZero();
                }
                return '00:00:00';
            }

            // Reset trigger if deadline is moved forward (e.g. next day)
            hasTriggeredZero.current = false;

            const hours = Math.floor((difference % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
            const minutes = Math.floor((difference % (1000 * 60 * 60)) / (1000 * 60));
            const seconds = Math.floor((difference % (1000 * 60)) / 1000);

            return `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
        };

        // Initial setup
        setTimeLeft(calculateTimeLeft());

        // Update every second
        const intervalId = setInterval(() => {
            setTimeLeft(calculateTimeLeft());
        }, 1000);

        return () => clearInterval(intervalId);
    }, [deadlineAtISO, onZero]);

    return timeLeft;
}
