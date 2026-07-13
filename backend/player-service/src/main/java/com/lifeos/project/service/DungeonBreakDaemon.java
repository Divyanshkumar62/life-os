package com.lifeos.project.service;

import com.lifeos.project.domain.Project;
import com.lifeos.project.domain.DungeonBreakEvent;
import com.lifeos.project.domain.enums.ProjectStability;
import com.lifeos.project.domain.enums.ProjectStatus;
import com.lifeos.project.repository.ProjectRepository;
import com.lifeos.project.repository.DungeonBreakEventRepository;
import com.lifeos.penalty.service.PenaltyService;
import com.lifeos.penalty.repository.PenaltyRecordRepository;
import com.lifeos.penalty.domain.enums.PenaltySeverity;
import com.lifeos.penalty.domain.enums.PenaltyType;
import com.lifeos.penalty.domain.PenaltyRecord;
import com.lifeos.player.service.PlayerStateService;
import com.lifeos.core.repository.PlayerStateRepository;
import com.lifeos.economy.service.EconomyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DungeonBreakDaemon {

    private static final Logger log = LoggerFactory.getLogger(DungeonBreakDaemon.class);

    private final ProjectRepository projectRepository;
    private final DungeonBreakEventRepository dungeonBreakEventRepository;
    private final PenaltyService penaltyService;
    private final PenaltyRecordRepository penaltyRecordRepository;
    private final PlayerStateService playerStateService;
    private final PlayerStateRepository playerStateRepository;
    private final EconomyService economyService;
    private final DungeonStabilityService dungeonStabilityService;

    public DungeonBreakDaemon(ProjectRepository projectRepository,
                              DungeonBreakEventRepository dungeonBreakEventRepository,
                              PenaltyService penaltyService,
                              PenaltyRecordRepository penaltyRecordRepository,
                              PlayerStateService playerStateService,
                              PlayerStateRepository playerStateRepository,
                              EconomyService economyService,
                              DungeonStabilityService dungeonStabilityService) {
        this.projectRepository = projectRepository;
        this.dungeonBreakEventRepository = dungeonBreakEventRepository;
        this.penaltyService = penaltyService;
        this.penaltyRecordRepository = penaltyRecordRepository;
        this.playerStateService = playerStateService;
        this.playerStateRepository = playerStateRepository;
        this.economyService = economyService;
        this.dungeonStabilityService = dungeonStabilityService;
    }

    @Scheduled(fixedRate = 1800000) // Every 30 minutes
    @Transactional
    public void scanAndTriggerDungeonBreaks() {
        log.info("DungeonBreakDaemon: Scanning for expired hard deadlines...");
        LocalDateTime now = LocalDateTime.now();
        List<Project> expiredProjects = projectRepository.findAll().stream()
                .filter(p -> (p.getStatus() == ProjectStatus.ACTIVE || p.getStatus() == ProjectStatus.SHADOW)
                        && p.getHardDeadline() != null
                        && p.getHardDeadline().isBefore(now))
                .toList();

        for (Project project : expiredProjects) {
            try {
                triggerDungeonBreak(project);
            } catch (Exception e) {
                log.error("DungeonBreakDaemon: Failed to trigger break for project {}", project.getProjectId(), e);
            }
        }
    }

    @Transactional
    public void triggerDungeonBreak(Project project) {
        UUID playerId = project.getPlayer().getPlayerId();
        UUID projectId = project.getProjectId();
        String title = project.getTitle();
        String rank = project.getRankRequirement() != null ? project.getRankRequirement().name() : "E";

        log.warn("DungeonBreakDaemon: Hard deadline expired for project {}. Triggering Dungeon Break!", title);

        // Transition project status
        if (project.getStatus() == ProjectStatus.SHADOW) {
            project.setStatus(ProjectStatus.PERMADEATH);
        } else {
            project.setStatus(ProjectStatus.FAILED);
        }
        projectRepository.save(project);

        // Trigger Dungeon Break instability in DungeonStabilityService
        dungeonStabilityService.triggerDungeonBreak(project);

        // Fetch player core state
        com.lifeos.core.entity.PlayerState playerState = playerStateRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player state not found: " + playerId));

        // Fetch economy state
        com.lifeos.economy.domain.PlayerEconomy economy = economyService.getEconomyState(playerId);
        long goldBalance = economy != null && economy.getGoldBalance() != null ? economy.getGoldBalance().longValue() : 0L;

        // Calculate gold penalty (30% base, VIT-mitigated)
        double vitMitigation = Math.min((double) playerState.getStatVit() / 100.0, 0.50);
        double drainPercentage = 0.30 * (1.0 - vitMitigation);
        long goldPenaltyAmount = Math.max((long) (goldBalance * drainPercentage), 0L);

        long goldAfter = 0;
        if (goldBalance >= goldPenaltyAmount) {
            goldAfter = goldBalance - goldPenaltyAmount;
            if (economy != null && goldPenaltyAmount > 0) {
                economyService.deductGold(playerId, goldPenaltyAmount, "Dungeon Break Gold Penalty");
            }
        } else {
            goldAfter = 0;
            if (economy != null && goldBalance > 0) {
                economyService.deductGold(playerId, goldBalance, "Dungeon Break Gold Penalty");
            }
            long remainder = goldPenaltyAmount - goldBalance;
            playerState.setGoldDebt(playerState.getGoldDebt() + remainder);
        }

        playerState.setGoldBalance(goldAfter);
        playerStateRepository.save(playerState);

        // Apply 10% stat debuff to all 5 attributes for 24 hours
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);
        List<String> debuffsApplied = new java.util.ArrayList<>();
        for (String attrName : List.of("STR", "INT", "VIT", "AGI", "SEN")) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("debuffAttr", attrName);
            payload.put("debuffAmount", 10.0);
            payload.put("durationHours", 24);

            PenaltyRecord record = PenaltyRecord.builder()
                    .playerId(playerId)
                    .questId(UUID.randomUUID())
                    .type(PenaltyType.STAT_DEBUFF)
                    .severity(PenaltySeverity.LOW)
                    .valuePayload(payload)
                    .appliedAt(LocalDateTime.now())
                    .expiresAt(expiresAt)
                    .build();
            penaltyRecordRepository.save(record);
            debuffsApplied.add("STAT_DEBUFF_" + attrName + "_10PCT");
        }

        // Check if player is already in Penalty Zone
        boolean alreadyInPenalty = playerStateService.getPlayerState(playerId).getActiveFlags().stream()
                .anyMatch(f -> f.getFlag() == com.lifeos.player.domain.enums.StatusFlagType.PENALTY_ZONE);

        boolean penaltyZoneTriggered = false;
        boolean doublePenaltyResolution = false;

        if (alreadyInPenalty) {
            doublePenaltyResolution = true;
            log.info("DungeonBreakDaemon: Player {} already in Penalty Zone. Double Penalty Applied (Gold Drain to Debt). Active Survival Task preserved.", playerId);
        } else {
            penaltyZoneTriggered = true;
            penaltyService.enterPenaltyZone(playerId, "Dungeon Break: " + title);
        }

        DungeonBreakEvent event = DungeonBreakEvent.builder()
                .projectId(projectId)
                .playerId(playerId)
                .projectTitle(title)
                .dungeonRank(rank)
                .goldBefore(goldBalance)
                .goldPenaltyAmount(goldPenaltyAmount)
                .goldAfter(goldAfter)
                .vitMitigationPercent(vitMitigation * 100.0)
                .debuffsApplied(debuffsApplied)
                .debuffDurationHours(24)
                .penaltyZoneTriggered(penaltyZoneTriggered)
                .doublePenaltyResolution(doublePenaltyResolution)
                .triggeredAt(LocalDateTime.now())
                .acknowledged(false)
                .build();
        dungeonBreakEventRepository.save(event);
    }
}
