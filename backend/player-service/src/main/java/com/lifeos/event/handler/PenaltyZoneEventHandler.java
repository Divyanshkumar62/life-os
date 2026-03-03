package com.lifeos.event.handler;

import com.lifeos.ai.service.AIQuestService;
import com.lifeos.event.concrete.PenaltyAppliedEvent;
import com.lifeos.quest.dto.QuestRequest;
import com.lifeos.quest.service.QuestLifecycleService;
import com.lifeos.player.service.PlayerStateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PenaltyZoneEventHandler {

    private static final Logger log = LoggerFactory.getLogger(PenaltyZoneEventHandler.class);

    private final AIQuestService aiQuestService;
    private final QuestLifecycleService questLifecycleService;
    private final PlayerStateService playerStateService;

    public PenaltyZoneEventHandler(AIQuestService aiQuestService, QuestLifecycleService questLifecycleService, PlayerStateService playerStateService) {
        this.aiQuestService = aiQuestService;
        this.questLifecycleService = questLifecycleService;
        this.playerStateService = playerStateService;
    }

    @EventListener
    @Transactional
    public void handlePenaltyApplied(PenaltyAppliedEvent event) {
        log.info("Handling PenaltyAppliedEvent for player {}", event.getPlayerId());
        
        try {
            // 1. Generate AI Penalty Quest
            QuestRequest penaltyRequest = aiQuestService.generatePenaltyQuest(event.getPlayerId(), event.getQuestId());
            
            if (penaltyRequest != null) {
                // 2. Extra XP Deduction determined by AI (if any)
                // successXp is used as a signal in the previous step
                long aiXpPenalty = -penaltyRequest.getSuccessXp();
                if (aiXpPenalty > 0) {
                    playerStateService.applyXpDeduction(event.getPlayerId(), aiXpPenalty);
                    log.info("Applied additional AI-determined XP penalty: {}", aiXpPenalty);
                }

                // Clean up the request for assignment
                penaltyRequest.setSuccessXp(0);
                
                // 3. Assign the Penalty Quest
                questLifecycleService.assignQuest(penaltyRequest);
                log.info("Assigned AI-designed Penalty Quest to player {}", event.getPlayerId());
            }
        } catch (Exception e) {
            log.error("Failed to process penalty quest for player {}", event.getPlayerId(), e);
        }
    }
}
