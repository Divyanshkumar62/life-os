package com.lifeos.event.handler;

import com.lifeos.event.DomainEvent;
import com.lifeos.event.DomainEventHandler;
import com.lifeos.event.concrete.*;
import com.lifeos.voice.domain.enums.SystemMessageType;
import com.lifeos.voice.service.SystemVoiceService;
import com.lifeos.player.service.PlayerStateService;
import com.lifeos.player.domain.enums.StatusFlagType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class VoiceEventHandler implements DomainEventHandler<DomainEvent> {

    private final SystemVoiceService voiceService;
    private final PlayerStateService playerStateService;

    @Override
    public boolean supports(DomainEvent event) {
        return event instanceof PenaltyZoneEnteredEvent ||
               event instanceof PenaltyQuestCompletedEvent ||
               event instanceof DailyQuestFailedEvent ||
               event instanceof QuestCompletedEvent;
    }

    @Override
    public void handle(DomainEvent event) {
        if (!supports(event)) return;

        // Local Suppression for Standard Quests during Penalty
        // (Because Publisher allows them through for Work Recording, but Voice should be silent)
        if (event instanceof QuestCompletedEvent) {
            boolean inPenalty = playerStateService.hasActiveFlag(event.getPlayerId(), StatusFlagType.PENALTY_ZONE);
            if (inPenalty) return;
        }

        if (event instanceof PenaltyZoneEnteredEvent) {
             voiceService.generateMessage(
                 event.getPlayerId(), 
                 SystemMessageType.PENALTY_ZONE_ENTRY, 
                 Map.of(), 
                 event.getEventId()
             );
        } else if (event instanceof PenaltyQuestCompletedEvent) {
             voiceService.generateMessage(
                 event.getPlayerId(), 
                 SystemMessageType.PENALTY_QUEST_COMPLETED, 
                 Map.of(), 
                 event.getEventId()
             );
        } else if (event instanceof DailyQuestFailedEvent) {
             voiceService.generateMessage(
                 event.getPlayerId(), 
                 SystemMessageType.DAILY_INCOMPLETE, 
                 Map.of(), 
                 event.getEventId()
             );
        } else if (event instanceof QuestCompletedEvent) {
             voiceService.generateMessage(
                 event.getPlayerId(), 
                 SystemMessageType.QUEST_COMPLETED, 
                 Map.of(), 
                 event.getEventId()
             );
        }
    }
}
