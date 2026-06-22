package com.lifeos.quest.controller;

import com.lifeos.quest.domain.Quest;
import com.lifeos.quest.domain.enums.QuestState;
import com.lifeos.quest.dto.QuestRequest;
import com.lifeos.quest.service.QuestLifecycleService;
import com.lifeos.quest.service.RedGateService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/quests")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class QuestController {

    private static final Logger log = LoggerFactory.getLogger(QuestController.class);

    private final QuestLifecycleService questService;
    private final RedGateService redGateService;
    private final com.lifeos.system.repository.SystemEventRepository systemEventRepository;

    @GetMapping("/red-gate/{playerId}/status")
    public ResponseEntity<RedGateStatusResponse> getRedGateStatus(@PathVariable UUID playerId) {
        boolean active = redGateService.isRedGateActive(playerId);
        Quest quest = redGateService.getActiveRedGateQuest(playerId);
        return ResponseEntity.ok(new RedGateStatusResponse(active, quest));
    }

    @PostMapping("/red-gate/{playerId}/trigger-key")
    public ResponseEntity<Void> triggerRedGateWithKey(@PathVariable UUID playerId) {
        try {
            redGateService.triggerRedGateWithKey(playerId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error triggering Red Gate with key: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/red-gate/{playerId}/complete")
    public ResponseEntity<Void> completeRedGate(@PathVariable UUID playerId) {
        redGateService.completeRedGate(playerId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/red-gate/{playerId}/fail")
    public ResponseEntity<Void> failRedGate(@PathVariable UUID playerId) {
        redGateService.failRedGate(playerId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/active")
    public ResponseEntity<com.lifeos.quest.dto.QuestResponse> getActiveQuests(@RequestParam UUID playerId) {
        log.info("Fetching active quests for player: {}", playerId);
        List<Quest> activeQuests = questService.getActiveQuests(playerId);
        
        List<com.lifeos.system.domain.SystemEvent> unconsumedEvents = systemEventRepository.findByPlayerIdAndIsConsumedFalseOrderByCreatedAtAsc(playerId);
        List<String> systemMessages = new java.util.ArrayList<>();
        if (unconsumedEvents != null && !unconsumedEvents.isEmpty()) {
            for (com.lifeos.system.domain.SystemEvent event : unconsumedEvents) {
                systemMessages.add(event.getMessage());
                event.setConsumed(true);
            }
            systemEventRepository.saveAll(unconsumedEvents);
        }

        return ResponseEntity.ok(com.lifeos.quest.dto.QuestResponse.builder()
                .quests(activeQuests)
                .systemMessages(systemMessages)
                .build());
    }

    @PostMapping
    public ResponseEntity<com.lifeos.quest.dto.QuestResponse> assignQuest(@RequestBody QuestRequest request) {
        log.info("Assigning quest: {} to player: {}", request.getTitle(), request.getPlayerId());
        Quest assigned = questService.assignQuest(request);
        return ResponseEntity.ok(com.lifeos.quest.dto.QuestResponse.builder()
                .quest(assigned)
                .build());
    }

    @PatchMapping("/{questId}/status")
    public ResponseEntity<?> updateQuestStatus(
            @PathVariable String questId,
            @RequestParam(required = false) String action,
            @RequestBody(required = false) java.util.Map<String, String> body) {
        
        log.info("=== API: Updating status of quest {} with action: {}, body: {}", questId, action, body);
        
        String targetAction = null;
        if (action != null) {
            targetAction = action.toUpperCase();
        } else if (body != null) {
            if (body.containsKey("status")) {
                String status = body.get("status").toUpperCase();
                if ("COMPLETED".equals(status)) targetAction = "COMPLETE";
                else if ("FAILED".equals(status)) targetAction = "FAIL";
                else if ("EXPIRED".equals(status)) targetAction = "EXPIRE";
            } else if (body.containsKey("action")) {
                targetAction = body.get("action").toUpperCase();
            }
        }
        
        if (targetAction == null) {
            log.error("=== API ERROR: No valid action or status provided for quest update");
            return ResponseEntity.badRequest().body("Action or status is required");
        }
        
        UUID uuid = parseUUID(questId);
        if (uuid == null) {
            log.error("Invalid quest ID format: {}", questId);
            return ResponseEntity.badRequest().body("Invalid quest ID format");
        }
        
        try {
            switch (targetAction) {
                case "COMPLETE":
                    com.lifeos.quest.dto.QuestResponse compResponse = questService.completeQuest(uuid);
                    return ResponseEntity.ok(compResponse);
                case "FAIL":
                    com.lifeos.quest.dto.QuestResponse failResponse = questService.failQuest(uuid);
                    return ResponseEntity.ok(failResponse);
                case "EXPIRE":
                    questService.expireQuest(uuid);
                    return ResponseEntity.ok().build();
                default:
                    return ResponseEntity.badRequest().body("Unsupported action: " + targetAction);
            }
        } catch (Exception e) {
            log.error("=== API ERROR updating quest status {}: {} - {}", questId, e.getClass().getSimpleName(), e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private UUID parseUUID(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        // Remove any dashes and try to parse
        String cleaned = input.replaceAll("-", "");
        if (cleaned.length() != 32) {
            // Try with standard UUID format
            try {
                return UUID.fromString(input);
            } catch (Exception e) {
                return null;
            }
        }
        // Format: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
        try {
            return UUID.fromString(
                cleaned.substring(0, 8) + "-" +
                cleaned.substring(8, 12) + "-" +
                cleaned.substring(12, 16) + "-" +
                cleaned.substring(16, 20) + "-" +
                cleaned.substring(20, 32)
            );
        } catch (Exception e) {
            log.error("Failed to parse UUID: {}", input);
            return null;
        }
    }

    public static class RedGateStatusResponse {
        public boolean active;
        public Quest quest;

        public RedGateStatusResponse(boolean active, Quest quest) {
            this.active = active;
            this.quest = quest;
        }
    }
}
