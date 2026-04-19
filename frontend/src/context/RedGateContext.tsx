import React, { createContext, useContext, useState, useEffect, useCallback } from "react";
import { RedGateAPI, JobChangeAPI } from "../api/api";

interface RedGateState {
    isActive: boolean;
    quest: any | null;
    remainingSeconds: number | null;
    loading: boolean;
}

interface JobChangeState {
    jobClass: string | null;
    status: string | null;
    xpFrozen: boolean;
    cooldownUntil: string | null;
    loading: boolean;
}

interface RedGateContextType {
    redGate: RedGateState;
    jobChange: JobChangeState;
    checkRedGateStatus: () => Promise<void>;
    checkJobChangeStatus: () => Promise<void>;
    triggerRedGateWithKey: () => Promise<void>;
    completeRedGate: () => Promise<void>;
    failRedGate: () => Promise<void>;
    acceptJobChange: () => Promise<void>;
    delayJobChange: () => Promise<void>;
    isShopLocked: boolean;
    isInventoryLocked: boolean;
}

const RedGateContext = createContext<RedGateContextType | undefined>(undefined);

export const useRedGateContext = () => {
    const context = useContext(RedGateContext);
    if (!context) {
        throw new Error("useRedGateContext must be used within a RedGateProvider");
    }
    return context;
};

export const RedGateProvider: React.FC<{
    children: React.ReactNode;
    playerId: string;
}> = ({ children, playerId }) => {
    const [redGate, setRedGate] = useState<RedGateState>({
        isActive: false,
        quest: null,
        remainingSeconds: null,
        loading: true
    });

    const [jobChange, setJobChange] = useState<JobChangeState>({
        jobClass: null,
        status: null,
        xpFrozen: false,
        cooldownUntil: null,
        loading: true
    });

    const checkRedGateStatus = useCallback(async () => {
        if (!playerId) return;
        try {
            const data = await RedGateAPI.getStatus(playerId);
            setRedGate({
                isActive: data.active,
                quest: data.quest,
                remainingSeconds: data.quest?.deadlineAt ? 
                    Math.max(0, new Date(data.quest.deadlineAt).getTime() - Date.now()) / 1000 : null,
                loading: false
            });
        } catch (err) {
            console.error("Failed to check Red Gate status:", err);
            setRedGate(prev => ({ ...prev, loading: false }));
        }
    }, [playerId]);

    const checkJobChangeStatus = useCallback(async () => {
        if (!playerId) return;
        try {
            const data = await JobChangeAPI.getStatus(playerId);
            setJobChange({
                jobClass: data.jobClass,
                status: data.status,
                xpFrozen: data.xpFrozen,
                cooldownUntil: data.cooldownUntil,
                loading: false
            });
        } catch (err) {
            console.error("Failed to check Job Change status:", err);
            setJobChange(prev => ({ ...prev, loading: false }));
        }
    }, [playerId]);

    const triggerRedGateWithKey = async () => {
        try {
            await RedGateAPI.triggerWithKey(playerId);
            await checkRedGateStatus();
        } catch (err) {
            console.error("Failed to trigger Red Gate:", err);
            throw err;
        }
    };

    const completeRedGate = async () => {
        try {
            await RedGateAPI.complete(playerId);
            await checkRedGateStatus();
        } catch (err) {
            console.error("Failed to complete Red Gate:", err);
            throw err;
        }
    };

    const failRedGate = async () => {
        try {
            await RedGateAPI.fail(playerId);
            await checkRedGateStatus();
        } catch (err) {
            console.error("Failed to fail Red Gate:", err);
            throw err;
        }
    };

    const acceptJobChange = async () => {
        try {
            await JobChangeAPI.accept(playerId);
            await checkJobChangeStatus();
        } catch (err) {
            console.error("Failed to accept Job Change:", err);
            throw err;
        }
    };

    const delayJobChange = async () => {
        try {
            await JobChangeAPI.delay(playerId);
            await checkJobChangeStatus();
        } catch (err) {
            console.error("Failed to delay Job Change:", err);
            throw err;
        }
    };

    useEffect(() => {
        checkRedGateStatus();
        checkJobChangeStatus();
        
        const interval = setInterval(() => {
            if (redGate.isActive && redGate.remainingSeconds !== null) {
                setRedGate(prev => ({
                    ...prev,
                    remainingSeconds: prev.remainingSeconds !== null ? Math.max(0, prev.remainingSeconds - 1) : null
                }));
            }
        }, 1000);

        return () => clearInterval(interval);
    }, [playerId, redGate.isActive]);

    return (
        <RedGateContext.Provider
            value={{
                redGate,
                jobChange,
                checkRedGateStatus,
                checkJobChangeStatus,
                triggerRedGateWithKey,
                completeRedGate,
                failRedGate,
                acceptJobChange,
                delayJobChange,
                isShopLocked: redGate.isActive,
                isInventoryLocked: redGate.isActive
            }}
        >
            {children}
        </RedGateContext.Provider>
    );
};
