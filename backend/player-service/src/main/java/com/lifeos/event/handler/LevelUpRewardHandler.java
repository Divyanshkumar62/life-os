package com.lifeos.event.handler;

import com.lifeos.event.concrete.LevelUpEvent;
import com.lifeos.economy.service.EconomyService;
import com.lifeos.onboarding.service.OnboardingService;
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

    private static final long GOLD_PER_LEVEL = 100L;

    private final PlayerStateService playerStateService;
    private final PenaltyService penaltyService;
    private final JobChangeService jobChangeService;
    private final OnboardingService onboardingService;
    private final EconomyService economyService;
    private final ApplicationEventPublisher eventPublisher;

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onLevelUp(LevelUpEvent event) {
        UUID playerId = event.getPlayerId();
        int newLevel = event.getNewLevel();
        
        log.info("Processing level-up for player {} to level {}", playerId, newLevel);
        
        try {
            // Step 1: Clear temporary debuffs
            clearTemporaryDebuffs(playerId);
            
            // Step 2: Grant free stat points
            playerStateService.addFreeStatPoints(playerId, 3);
            log.debug("Granted 3 free stat points to player {}", playerId);
            
            // Step 3: Grant gold reward based on level
            long goldReward = newLevel * GOLD_PER_LEVEL;
            economyService.addGold(playerId, goldReward, "Level Up Reward: Level " + newLevel);
            log.debug("Granted {} gold to player {}", goldReward, playerId);
            
            // Step 4: Level 40 - Trigger job change and unlock onboarding
            if (newLevel == 40) {
                jobChangeService.triggerJobChangeGauntlet(playerId);
                log.info("Triggered job change gauntlet for player {}", playerId);
                
                onboardingService.unlockJobChangeStage(playerId);
                log.info("Unlocked JOB_CHANGE stage in onboarding for player {}", playerId);
            }
            
            // Step 5: Send notifications (fire-and-forget)
            sendLevelUpNotifications(playerId);
            
            log.info("Level-up processing complete for player {} at level {} ({} gold awarded)", 
                    playerId, newLevel, goldReward);
            
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