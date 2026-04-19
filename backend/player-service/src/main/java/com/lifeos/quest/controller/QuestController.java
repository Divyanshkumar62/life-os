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
    public ResponseEntity<List<Quest>> getActiveQuests(@RequestParam UUID playerId) {
        log.info("Fetching active quests for player: {}", playerId);
        List<Quest> activeQuests = questService.getActiveQuests(playerId);
        return ResponseEntity.ok(activeQuests);
    }

    @PostMapping
    public ResponseEntity<Quest> assignQuest(@RequestBody QuestRequest request) {
        log.info("Assigning quest: {} to player: {}", request.getTitle(), request.getPlayerId());
        return ResponseEntity.ok(questService.assignQuest(request));
    }

    @PostMapping("/{questId}/complete")
    public ResponseEntity<Void> completeQuest(@PathVariable String questId) {
        log.info("=== API: Completing quest: {}", questId);
        try {
            UUID uuid = parseUUID(questId);
            if (uuid == null) {
                log.error("Invalid quest ID format: {}", questId);
                return ResponseEntity.badRequest().build();
            }
            questService.completeQuest(uuid);
            log.info("=== API: Quest completed successfully: {}", uuid);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("=== API ERROR completing quest {}: {} - {}", questId, e.getClass().getSimpleName(), e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{questId}/fail")
    public ResponseEntity<Void> failQuest(@PathVariable String questId) {
        log.info("=== API: Failing quest: {}", questId);
        try {
            UUID uuid = parseUUID(questId);
            if (uuid == null) {
                log.error("Invalid quest ID format: {}", questId);
                return ResponseEntity.badRequest().build();
            }
            questService.failQuest(uuid);
            log.info("=== API: Quest failed successfully: {}", uuid);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("=== API ERROR failing quest {}: {} - {}", questId, e.getClass().getSimpleName(), e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{questId}/expire")
    public ResponseEntity<Void> expireQuest(@PathVariable String questId) {
        log.warn("Expiring quest: {}", questId);
        try {
            UUID uuid = parseUUID(questId);
            if (uuid == null) {
                return ResponseEntity.badRequest().build();
            }
            questService.expireQuest(uuid);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error expiring quest: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
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
