package com.lifeos.event.handler;

import com.lifeos.event.concrete.LevelUpEvent;
import com.lifeos.penalty.service.PenaltyService;
import com.lifeos.player.service.PlayerStateService;
import com.lifeos.progression.service.JobChangeService;
import com.lifeos.voice.domain.enums.SystemMessageType;
import com.lifeos.voice.event.VoiceSystemEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class LevelUpRewardHandler {

    private final PlayerStateService playerStateService;
    private final PenaltyService penaltyService;
    private final JobChangeService jobChangeService;
    private final ApplicationEventPublisher eventPublisher;

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onLevelUp(LevelUpEvent event) {
        UUID playerId = event.getPlayerId();
        int newLevel = event.getNewLevel();
        
        log.info("Processing level-up for player {} to level {}", playerId, newLevel);
        
        try {
            clearTemporaryDebuffs(playerId);
            playerStateService.addFreeStatPoints(playerId, 3);
            log.debug("Granted 3 free stat points to player {}", playerId);
            
            if (newLevel == 40) {
                jobChangeService.triggerJobChangeGauntlet(playerId);
                log.info("Triggered job change gauntlet for player {}", playerId);
            }
            
            sendLevelUpNotifications(playerId);
            
            log.info("Level-up processing complete for player {} at level {}", playerId, newLevel);
            
        } catch (Exception e) {
            log.error("Level-up failed for player {}: {}. Rolling back all changes.", playerId, e.getMessage(), e);
            throw new RuntimeException("Level-up failed: " + e.getMessage(), e);
        }
    }

    private void clearTemporaryDebuffs(UUID playerId) {
        penaltyService.clearTemporaryDebuffs(playerId);
        log.debug("Cleared temporary debuffs for player {}", playerId);
    }

    private void sendLevelUpNotifications(UUID playerId) {
        try {
            eventPublisher.publishEvent(VoiceSystemEvent.builder()
                    .playerId(playerId)
                    .type(SystemMessageType.LEVEL_UP)
                    .build());
        } catch (Exception e) {
            log.warn("Failed to send LEVEL_UP notification: {}", e.getMessage());
        }
        
        try {
            eventPublisher.publishEvent(VoiceSystemEvent.builder()
                    .playerId(playerId)
                    .type(SystemMessageType.LEVEL_UP_REWARD)
                    .build());
        } catch (Exception e) {
            log.warn("Failed to send LEVEL_UP_REWARD notification: {}", e.getMessage());
        }
    }
}