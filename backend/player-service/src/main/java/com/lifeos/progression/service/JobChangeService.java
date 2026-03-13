package com.lifeos.progression.service;

import com.lifeos.economy.service.EconomyService;
import com.lifeos.economy.service.InventoryService;
import com.lifeos.penalty.service.PenaltyService;
import com.lifeos.player.domain.PlayerIdentity;
import com.lifeos.player.domain.PlayerMetadata;
import com.lifeos.player.domain.PlayerProgression;
import com.lifeos.player.domain.enums.AttributeType;
import com.lifeos.player.repository.PlayerAttributeRepository;
import com.lifeos.player.repository.PlayerHistoryRepository;
import com.lifeos.player.repository.PlayerIdentityRepository;
import com.lifeos.player.repository.PlayerMetadataRepository;
import com.lifeos.player.repository.PlayerProgressionRepository;
import com.lifeos.progression.domain.JobChangeQuest;
import com.lifeos.progression.repository.JobChangeQuestRepository;
import com.lifeos.notification.service.PushNotificationService;
import com.lifeos.project.service.ProjectService;
import com.lifeos.shop.domain.ShopItem;
import com.lifeos.shop.repository.ShopItemRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JobChangeService {

    private static final Logger log = LoggerFactory.getLogger(JobChangeService.class);
    private static final String ELIXIR_ITEM_CODE = "ELIXIR_SECOND_AWAKENING";
    private static final int JOB_CHANGE_REWARD_STAT_POINTS = 20;
    private static final int JOB_CHANGE_REWARD_GOLD = 50000;
    private static final int JOB_CHANGE_REWARD_A_ITEMS = 2;

    private final PlayerIdentityRepository identityRepository;
    private final JobChangeQuestRepository jobChangeQuestRepository;
    private final JobChangeArchitect jobChangeArchitect;
    private final JobClassCalculator jobClassCalculator;
    private final PushNotificationService pushNotificationService;
    private final ProjectService projectService;
    private final PlayerAttributeRepository attributeRepository;
    private final PlayerHistoryRepository historyRepository;
    private final PlayerProgressionRepository progressionRepository;
    private final PlayerMetadataRepository metadataRepository;
    private final EconomyService economyService;
    private final InventoryService inventoryService;
    private final PenaltyService penaltyService;
    private final ShopItemRepository shopItemRepository;

    /**
     * Check if player has reached level 40 and trigger job change if needed.
     * Called during level-up event handling.
     */
    public void checkAndTriggerJobChange(UUID playerId, int newLevel) {
        if (newLevel == 40) {
            triggerJobChangeGauntlet(playerId);
        }
    }

    /**
     * Trigger the job change quest for a player.
     */
    public void triggerJobChangeGauntlet(UUID playerId) {
        PlayerIdentity identity = identityRepository.findById(playerId).orElse(null);
        if (identity == null) {
            log.warn("Player not found: {}", playerId);
            return;
        }

        if (identity.getJobClass() != null) {
            log.info("Player {} already has a job class, skipping trigger", playerId);
            return;
        }

        if (identity.getJobChangeCooldownUntil() != null && LocalDateTime.now().isBefore(identity.getJobChangeCooldownUntil())) {
            log.info("Player {} is in job change cooldown until {}", playerId, identity.getJobChangeCooldownUntil());
            return;
        }

        identity.setJobChangeStatus("AWAITING_ACCEPTANCE");
        identity.setXpFrozen(true);
        identityRepository.save(identity);

        log.info("Triggered Job Change Quest for player {}", playerId);

        pushNotificationService.sendNotification(
                playerId,
                null,
                "The Job Change Awaits",
                "The System demands your evolution. Accept the gauntlet?",
                null
        );
    }

    /**
     * Player accepts the job change quest and gauntlet begins.
     */
    @Transactional
    public void acceptJobChange(UUID playerId) {
        PlayerIdentity identity = identityRepository.findById(playerId).orElse(null);
        if (identity == null) {
            throw new IllegalArgumentException("Player not found");
        }

        if (!"AWAITING_ACCEPTANCE".equals(identity.getJobChangeStatus())) {
            throw new IllegalStateException("Job change is not awaiting acceptance");
        }

        var gauntletQuests = jobChangeArchitect.generateThreeDayGauntlet(identity);
        log.info("Generated {} gauntlet quests for player {}", gauntletQuests.size(), playerId);

        for (var quest : gauntletQuests) {
            JobChangeQuest jobQuest = JobChangeQuest.builder()
                    .player(identity)
                    .day(quest.day)
                    .title(quest.title)
                    .description(quest.description)
                    .estimatedMinutes(quest.estimatedMinutes)
                    .difficulty(quest.difficulty)
                    .questType(quest.type)
                    .state(JobChangeQuest.JobChangeQuestState.PENDING)
                    .assignedAt(LocalDateTime.now())
                    .metadata(quest.metadata.toString())
                    .build();
            jobChangeQuestRepository.save(jobQuest);
        }

        projectService.suspendPlayerProjects(playerId);
        log.info("Suspended all active projects for player {}", playerId);

        identity.setJobChangeStatus("IN_PROGRESS");
        identity.setXpFrozen(true);
        identityRepository.save(identity);

        log.info("Player {} accepted Job Change Quest", playerId);
    }

    /**
     * Player delays the job change quest for 24 hours.
     */
    @Transactional
    public void delayJobChange(UUID playerId) {
        PlayerIdentity identity = identityRepository.findById(playerId).orElse(null);
        if (identity == null) {
            throw new IllegalArgumentException("Player not found");
        }

        LocalDateTime nextReset = LocalDateTime.now()
                .withHour(4).withMinute(0).withSecond(0).withNano(0);
        if (LocalDateTime.now().isAfter(nextReset)) {
            nextReset = nextReset.plusDays(1);
        }

        identity.setJobChangeStatus("AWAITING_ACCEPTANCE");
        identity.setXpFrozen(true);
        identityRepository.save(identity);

        log.info("Player {} delayed Job Change until {}", playerId, nextReset);
    }

    /**
     * Complete a job change quest (called when player completes a day's quest).
     */
    @Transactional
    public void completeQuest(UUID questId) {
        JobChangeQuest quest = jobChangeQuestRepository.findById(questId)
                .orElseThrow(() -> new IllegalArgumentException("Quest not found"));

        if (quest.getState() != JobChangeQuest.JobChangeQuestState.PENDING) {
            throw new IllegalStateException("Quest is not pending");
        }

        quest.setState(JobChangeQuest.JobChangeQuestState.COMPLETED);
        quest.setCompletedAt(LocalDateTime.now());
        jobChangeQuestRepository.save(quest);

        log.info("Completed job change quest: {} for player {}", questId, quest.getPlayer().getPlayerId());

        checkDayCompletion(quest.getPlayer().getPlayerId(), quest.getDay());
    }

    /**
     * Fail a job change quest (called when player fails a day's quest).
     */
    @Transactional
    public void failQuest(UUID questId) {
        JobChangeQuest quest = jobChangeQuestRepository.findById(questId)
                .orElseThrow(() -> new IllegalArgumentException("Quest not found"));

        if (quest.getState() != JobChangeQuest.JobChangeQuestState.PENDING) {
            throw new IllegalStateException("Quest is not pending");
        }

        quest.setState(JobChangeQuest.JobChangeQuestState.FAILED);
        quest.setFailedAt(LocalDateTime.now());
        jobChangeQuestRepository.save(quest);

        log.info("Failed job change quest: {} for player {}", questId, quest.getPlayer().getPlayerId());

        failJobChange(quest.getPlayer().getPlayerId());
    }

    /**
     * Check if all quests for a day are completed.
     */
    private void checkDayCompletion(UUID playerId, int completedDay) {
        List<JobChangeQuest> allQuests = jobChangeQuestRepository.findByPlayerPlayerId(playerId);
        
        boolean allCompleted = allQuests.stream()
                .filter(q -> q.getDay() == completedDay)
                .allMatch(q -> q.getState() == JobChangeQuest.JobChangeQuestState.COMPLETED);

        if (!allCompleted) {
            log.info("Day {} not yet complete for player {}", completedDay, playerId);
            return;
        }

        if (completedDay < 3) {
            log.info("Day {} completed for player {}, proceeding to next day", completedDay, playerId);
        } else {
            log.info("All 3 days completed for player {}, awarding job class", playerId);
            completeJobChange(playerId, true);
        }
    }

    /**
     * Complete the job change quest on Day 3 completion.
     */
    @Transactional
    public void completeJobChange(UUID playerId, boolean perfectClearance) {
        PlayerIdentity identity = identityRepository.findById(playerId).orElse(null);
        if (identity == null) {
            throw new IllegalArgumentException("Player not found");
        }

        int str = getAttributeValue(playerId, AttributeType.STRENGTH);
        int intel = getAttributeValue(playerId, AttributeType.INTELLIGENCE);
        int vit = getAttributeValue(playerId, AttributeType.VITALITY);
        int sen = getAttributeValue(playerId, AttributeType.SENSE);

        double physicalRatio = calculatePhysicalQuestRatio(playerId);

        JobClassCalculator.JobClassResult result = jobClassCalculator.calculateJobClass(
                str, intel, vit, sen,
                physicalRatio,
                perfectClearance
        );

        identity.setJobClass(result.jobTitle);
        identity.setClassMultiplier(result.classMultiplierJson);
        identity.setClassUnlockedAt(LocalDateTime.now());
        identity.setJobChangeStatus("COMPLETED");
        identity.setXpFrozen(false);
        identityRepository.save(identity);

        projectService.resumePlayerProjects(playerId, 3);

        awardRewards(playerId, result.jobTitle);

        log.info("Player {} completed Job Change Quest with class: {}", playerId, result.jobTitle);
    }

    /**
     * Award rewards: 20 stat points, 2 A-rank items, 50k gold, theme unlock.
     */
    private void awardRewards(UUID playerId, String jobClass) {
        PlayerProgression progression = progressionRepository.findByPlayerPlayerId(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Progression not found"));
        
        progression.setFreeStatPoints(progression.getFreeStatPoints() + JOB_CHANGE_REWARD_STAT_POINTS);
        progressionRepository.save(progression);
        log.info("Awarded {} stat points to player {}", JOB_CHANGE_REWARD_STAT_POINTS, playerId);

        economyService.addGold(playerId, JOB_CHANGE_REWARD_GOLD, "Job Change Completion Reward");
        log.info("Awarded {} gold to player {}", JOB_CHANGE_REWARD_GOLD, playerId);

        List<ShopItem> aRankItems = shopItemRepository.findAll().stream()
                .filter(item -> "A-RANK".equals(item.getRarity()) && item.isActive())
                .limit(JOB_CHANGE_REWARD_A_ITEMS)
                .toList();

        for (ShopItem item : aRankItems) {
            inventoryService.addItem(playerId, item.getItemId(), 1);
            log.info("Awarded A-rank item: {} to player {}", item.getItemName(), playerId);
        }

        String themeCode = getThemeForClass(jobClass);
        PlayerIdentity player = identityRepository.findById(playerId).orElse(null);
        PlayerMetadata metadata = metadataRepository.findByPlayerPlayerId(playerId)
                .orElseGet(() -> {
                    PlayerMetadata newMeta = PlayerMetadata.builder()
                            .player(player)
                            .build();
                    return metadataRepository.save(newMeta);
                });

        String unlockedThemes = metadata.getUnlockedThemes();
        if (unlockedThemes == null || unlockedThemes.isEmpty()) {
            unlockedThemes = "[\"" + themeCode + "\"]";
        } else {
            unlockedThemes = unlockedThemes.replace("]", ",\"" + themeCode + "\"]");
        }
        metadata.setUnlockedThemes(unlockedThemes);
        metadata.setUiTheme(themeCode);
        metadataRepository.save(metadata);

        log.info("Unlocked theme: {} for player {}", themeCode, playerId);

        pushNotificationService.sendNotification(
                playerId,
                null,
                "Job Change Complete!",
                "You have evolved into: " + jobClass + "!",
                null
        );
    }

    private String getThemeForClass(String jobClass) {
        return switch (jobClass) {
            case "Silver Knight", "Berserker" -> "vanguard_red";
            case "Grand Architect", "Arcane Mage" -> "scholar_blue";
            case "Shadow Necromancer" -> "shadow_purple";
            default -> "shadow_purple";
        };
    }

    /**
     * Handle job change quest failure.
     */
    @Transactional
    public void failJobChange(UUID playerId) {
        PlayerIdentity identity = identityRepository.findById(playerId).orElse(null);
        if (identity == null) {
            throw new IllegalArgumentException("Player not found");
        }

        LocalDateTime cooldownExpires = LocalDateTime.now()
                .plusDays(7)
                .withHour(4).withMinute(0).withSecond(0).withNano(0);

        identity.setJobChangeStatus("COOLDOWN");
        identity.setJobChangeCooldownUntil(cooldownExpires);
        identity.setXpFrozen(true);
        identityRepository.save(identity);

        projectService.resumePlayerProjects(playerId, 3);

        penaltyService.enterPenaltyZone(playerId, "Job Change Gauntlet Failed");

        pushNotificationService.sendNotification(
                playerId,
                null,
                "Job Change Failed",
                "The gauntlet has broken you. 7-day cooldown begins.",
                null
        );

        log.info("Player {} failed Job Change Quest, cooldown until {}", playerId, cooldownExpires);
    }

    /**
     * Skip the job change cooldown using premium item.
     */
    @Transactional
    public void skipCooldown(UUID playerId) {
        PlayerIdentity identity = identityRepository.findById(playerId).orElse(null);
        if (identity == null) {
            throw new IllegalArgumentException("Player not found");
        }

        List<ShopItem> elixirItems = shopItemRepository.findAll().stream()
                .filter(item -> ELIXIR_ITEM_CODE.equals(item.getItemCode()) && item.isActive())
                .toList();

        if (elixirItems.isEmpty()) {
            throw new IllegalStateException("Elixir of Second Awakening not available in shop");
        }

        ShopItem elixir = elixirItems.get(0);

        Optional<Object> inventoryItem = Optional.empty();
        try {
            var method = InventoryService.class.getMethod("hasItem", UUID.class, UUID.class);
            inventoryItem = Optional.ofNullable(method.invoke(inventoryService, playerId, elixir.getItemId()));
        } catch (Exception e) {
            log.warn("Could not check inventory, assuming no item: {}", e.getMessage());
        }

        if (inventoryItem.isEmpty()) {
            throw new IllegalStateException("Player does not have Elixir of Second Awakening");
        }

        inventoryService.addItem(playerId, elixir.getItemId(), -1);

        identity.setJobChangeCooldownUntil(LocalDateTime.now().minusMinutes(1));
        identity.setJobChangeStatus("AWAITING_ACCEPTANCE");
        identity.setXpFrozen(true);
        identityRepository.save(identity);

        log.info("Player {} used Elixir to skip Job Change cooldown", playerId);

        triggerJobChangeGauntlet(playerId);
    }

    private int getAttributeValue(UUID playerId, AttributeType type) {
        return attributeRepository.findByPlayerPlayerIdAndAttributeType(playerId, type)
                .map(attr -> (int) attr.getCurrentValue())
                .orElse(10);
    }

    private double calculatePhysicalQuestRatio(UUID playerId) {
        return historyRepository.findByPlayerPlayerId(playerId)
                .map(history -> {
                    List<String> completed = history.getCompletedQuests();
                    if (completed == null || completed.isEmpty()) {
                        return 0.5;
                    }
                    long physicalCount = completed.stream()
                            .filter(q -> q.toLowerCase().contains("physical") || q.toLowerCase().contains("strength"))
                            .count();
                    return (double) physicalCount / completed.size();
                })
                .orElse(0.5);
    }
}
