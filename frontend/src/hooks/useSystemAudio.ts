import useSound from 'use-sound';

export function useSystemAudio() {
    const [playLevelUp] = useSound('/sounds/level-up.mp3', { volume: 0.8 });
    const [playSystemAlert] = useSound('/sounds/system-alert.mp3', { volume: 0.7 });
    const [playRedGateAlarm] = useSound('/sounds/red-gate-alarm.mp3', { volume: 0.9 });
    const [playUiClick] = useSound('/sounds/ui-click.mp3', { volume: 0.5, interrupt: true });

    return {
        playLevelUp,
        playSystemAlert,
        playRedGateAlarm,
        playUiClick
    };
}
