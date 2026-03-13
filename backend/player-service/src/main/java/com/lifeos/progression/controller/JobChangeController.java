package com.lifeos.progression.controller;

import com.lifeos.player.domain.PlayerIdentity;
import com.lifeos.player.repository.PlayerIdentityRepository;
import com.lifeos.progression.domain.JobChangeQuest;
import com.lifeos.progression.repository.JobChangeQuestRepository;
import com.lifeos.progression.service.JobChangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/player/job-change")
@RequiredArgsConstructor
public class JobChangeController {

    private final JobChangeService jobChangeService;
    private final PlayerIdentityRepository identityRepository;
    private final JobChangeQuestRepository jobChangeQuestRepository;

    @GetMapping("/{playerId}/status")
    public ResponseEntity<JobChangeStatusResponse> getJobChangeStatus(@PathVariable UUID playerId) {
        PlayerIdentity identity = identityRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        return ResponseEntity.ok(new JobChangeStatusResponse(
                identity.getJobClass(),
                identity.getJobChangeStatus(),
                identity.isXpFrozen(),
                identity.getJobChangeCooldownUntil()
        ));
    }

    @GetMapping("/{playerId}/quests")
    public ResponseEntity<List<JobChangeQuest>> getJobChangeQuests(@PathVariable UUID playerId) {
        List<JobChangeQuest> quests = jobChangeQuestRepository.findByPlayerPlayerId(playerId);
        return ResponseEntity.ok(quests);
    }

    @PostMapping("/{playerId}/accept")
    public ResponseEntity<Void> acceptJobChange(@PathVariable UUID playerId) {
        jobChangeService.acceptJobChange(playerId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{playerId}/delay")
    public ResponseEntity<Void> delayJobChange(@PathVariable UUID playerId) {
        jobChangeService.delayJobChange(playerId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/quest/{questId}/complete")
    public ResponseEntity<Void> completeQuest(@PathVariable UUID questId) {
        jobChangeService.completeQuest(questId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/quest/{questId}/fail")
    public ResponseEntity<Void> failQuest(@PathVariable UUID questId) {
        jobChangeService.failQuest(questId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{playerId}/skip-cooldown")
    public ResponseEntity<Void> skipCooldown(@PathVariable UUID playerId) {
        jobChangeService.skipCooldown(playerId);
        return ResponseEntity.ok().build();
    }

    public static class JobChangeStatusResponse {
        public String jobClass;
        public String status;
        public boolean xpFrozen;
        public Object cooldownUntil;

        public JobChangeStatusResponse(String jobClass, String status, boolean xpFrozen, Object cooldownUntil) {
            this.jobClass = jobClass;
            this.status = status;
            this.xpFrozen = xpFrozen;
            this.cooldownUntil = cooldownUntil;
        }
    }
}
