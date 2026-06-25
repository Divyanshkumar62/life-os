package com.lifeos.quest.controller;

import com.lifeos.quest.domain.Quest;
import com.lifeos.quest.dto.QuestResponse;
import com.lifeos.quest.service.QuestLifecycleService;
import com.lifeos.system.domain.SystemEvent;
import com.lifeos.system.repository.SystemEventRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PlayerQuestController {

    private static final Logger log = LoggerFactory.getLogger(PlayerQuestController.class);

    private final QuestLifecycleService questService;
    private final SystemEventRepository systemEventRepository;

    @GetMapping("/{playerId}/quests")
    public ResponseEntity<QuestResponse> getQuests(
            @PathVariable UUID playerId,
            @RequestParam(required = false) String status) {
        
        log.info("Fetching quests for player: {}, status: {}", playerId, status);
        
        com.lifeos.quest.domain.enums.QuestState state = null;
        if (status != null && !status.isBlank()) {
            try {
                state = com.lifeos.quest.domain.enums.QuestState.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid quest status filter: {}", status);
                return ResponseEntity.badRequest().build();
            }
        }
        
        List<Quest> quests = questService.getQuests(playerId, state);
        
        // Retrieve and consume system alerts to be returned as toast messages
        List<SystemEvent> unconsumedEvents = systemEventRepository.findByPlayerIdAndIsConsumedFalseOrderByCreatedAtAsc(playerId);
        List<String> systemMessages = new ArrayList<>();
        if (unconsumedEvents != null && !unconsumedEvents.isEmpty()) {
            for (SystemEvent event : unconsumedEvents) {
                systemMessages.add(event.getMessage());
                event.setConsumed(true);
            }
            systemEventRepository.saveAll(unconsumedEvents);
        }

        return ResponseEntity.ok(QuestResponse.builder()
                .quests(quests)
                .systemMessages(systemMessages)
                .build());
    }
}
