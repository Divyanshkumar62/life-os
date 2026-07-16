package com.lifeos.event.handler;

import com.lifeos.event.concrete.LevelUpEvent;
import com.lifeos.economy.service.EconomyService;
import com.lifeos.onboarding.service.OnboardingService;
import com.lifeos.penalty.service.PenaltyService;
import com.lifeos.player.service.PlayerStateService;
import com.lifeos.player.dto.LevelUpResultDTO;
import com.lifeos.player.repository.PlayerProgressionRepository;
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
    private final PlayerProgressionRepository progressionRepository;
    private final com.lifeos.system.service.SystemVoiceService systemVoiceService;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @EventListener
    @Transactional(propagation = Propagation.REQUIRED)
    public void onLevelUp(LevelUpEvent event) {
        UUID playerId = event.getPlayerId();
        int newLevel = event.getNewLevel();
        
        log.info("Processing level-up for player {} to level {}", playerId, newLevel);
        
        try {
            // Step 1: Clear temporary debuffs
            clearTemporaryDebuffs(playerId);
            
            // Step 2: Grant free stat points
            int statPointsAwarded = 5;
            if (newLevel == 40) {
                statPointsAwarded += 20;
            }
            playerStateService.addFreeStatPoints(playerId, statPointsAwarded);
            log.debug("Granted {} free stat points to player {}", statPointsAwarded, playerId);
            
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
            
            // Fetch current progression info for DTO
            boolean xpFrozen = false;
            int rankCapLevel = 100;
            var progression = progressionRepository.findByPlayerPlayerId(playerId).orElse(null);
            if (progression != null) {
                xpFrozen = progression.isXpFrozen();
                rankCapLevel = progression.getRank().getLevelCap();
            }

            LevelUpResultDTO rewardDto = LevelUpResultDTO.builder()
                    .playerId(playerId)
                    .newLevel(newLevel)
                    .previousLevel(event.getPreviousLevel())
                    .statPointsAwarded(statPointsAwarded)
                    .goldAwarded(goldReward)
                    .debuffsCleansed(true)
                    .xpFrozen(xpFrozen)
                    .xpBurned(0L)
                    .rankCapLevel(rankCapLevel)
                    .build();

            // Step 5: Send notifications (fire-and-forget)
            sendLevelUpNotifications(playerId, rewardDto);
            
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

    private void sendLevelUpNotifications(UUID playerId, LevelUpResultDTO rewardDto) {
        String payloadJson = null;
        try {
            payloadJson = objectMapper.writeValueAsString(rewardDto);
        } catch (Exception e) {
            log.warn("Failed to serialize LevelUpResultDTO: {}", e.getMessage());
        }

        try {
            systemVoiceService.emitEvent(playerId, com.lifeos.system.domain.enums.SystemEventType.LEVEL_UP, 
                    "Congratulations! You have leveled up to Level " + rewardDto.getNewLevel(), payloadJson);
        } catch (Exception e) {
            log.warn("Failed to emit LEVEL_UP system voice event: {}", e.getMessage());
        }

        try {
            java.util.Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("newLevel", rewardDto.getNewLevel());
            payload.put("previousLevel", rewardDto.getPreviousLevel());
            payload.put("statPointsAwarded", rewardDto.getStatPointsAwarded());
            payload.put("goldAwarded", rewardDto.getGoldAwarded());
            payload.put("debuffsCleansed", rewardDto.isDebuffsCleansed());
            payload.put("xpFrozen", rewardDto.isXpFrozen());
            payload.put("xpBurned", rewardDto.getXpBurned());
            payload.put("rankCapLevel", rewardDto.getRankCapLevel());

            eventPublisher.publishEvent(VoiceSystemEvent.builder()
                    .playerId(playerId)
                    .type(SystemMessageType.LEVEL_UP)
                    .payload(payload)
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