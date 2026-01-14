package com.lifeos.voice.service;

import com.lifeos.voice.event.VoiceSystemEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SystemEventListener {

    private static final Logger log = LoggerFactory.getLogger(SystemEventListener.class);
    private final SystemVoiceService voiceService;
    private final com.lifeos.player.service.PlayerStateService playerStateService;

    @Async // Decouple from main transaction where possible
    @EventListener
    public void handleVoiceSystemEvent(VoiceSystemEvent event) {
        try {
            // 1. SUPPRESSION GUARD (Penalty Override)
            // If player is in Penalty Zone, ONLY Critical messages are allowed.
            boolean inPenalty = playerStateService.hasActiveFlag(event.getPlayerId(), com.lifeos.player.domain.enums.StatusFlagType.PENALTY_ZONE);
            
            if (inPenalty && !event.getType().isCritical()) {
                log.info("Voice suppressed (Penalty Override): Type={} Player={}", event.getType(), event.getPlayerId());
                return;
            }

            // 2. Delegate to Service
            voiceService.generateMessage(
                event.getPlayerId(), 
                event.getType(), 
                event.getPayload(),
                event.getEventId()
            );
        } catch (Exception e) {
            log.error("Failed to process System Voice Event for player {}", event.getPlayerId(), e);
        }
    }
}
