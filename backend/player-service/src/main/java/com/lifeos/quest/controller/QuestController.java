package com.lifeos.quest.controller;

import com.lifeos.quest.domain.Quest;
import com.lifeos.quest.dto.QuestRequest;
import com.lifeos.quest.service.QuestLifecycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/quests")
@RequiredArgsConstructor
public class QuestController {

    private final QuestLifecycleService questService;

    @PostMapping
    public ResponseEntity<Quest> assignQuest(@RequestBody QuestRequest request) {
        return ResponseEntity.ok(questService.assignQuest(request));
    }

    @PostMapping("/{questId}/complete")
    public ResponseEntity<Void> completeQuest(@PathVariable UUID questId) {
        questService.completeQuest(questId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{questId}/fail")
    public ResponseEntity<Void> failQuest(@PathVariable UUID questId) {
        questService.failQuest(questId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{questId}/expire")
    public ResponseEntity<Void> expireQuest(@PathVariable UUID questId) {
        questService.expireQuest(questId);
        return ResponseEntity.ok().build();
    }
}
