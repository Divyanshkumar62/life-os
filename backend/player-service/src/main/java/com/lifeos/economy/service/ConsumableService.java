package com.lifeos.economy.service;

import com.lifeos.economy.domain.ShopItem;
import com.lifeos.economy.repository.ShopItemRepository;
import com.lifeos.player.domain.PlayerMetadata;
import com.lifeos.player.domain.enums.StatusFlagType;
import com.lifeos.player.repository.PlayerIdentityRepository;
import com.lifeos.player.repository.PlayerMetadataRepository;
import com.lifeos.player.service.PlayerStateService;
import com.lifeos.streak.service.StreakService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ConsumableService {

    private final InventoryService inventoryService;
    private final ShopItemRepository shopItemRepository;
    private final StreakService streakService;
    private final PlayerStateService playerStateService;
    private final PlayerMetadataRepository playerMetadataRepository;
    private final PlayerIdentityRepository playerIdentityRepository;
    
    // Lazy imports to break potential circular dependencies if they exist, or standard injection.
    // Using org.springframework.context.annotation.Lazy to be safe since Consumable interacts with many systems.
    @org.springframework.context.annotation.Lazy
    private final com.lifeos.quest.service.QuestLifecycleService questLifecycleService;
    @org.springframework.context.annotation.Lazy
    private final com.lifeos.project.service.DungeonArchitectService dungeonArchitectService;
    @org.springframework.context.annotation.Lazy
    private final com.lifeos.player.service.PlayerHistoryService playerHistoryService;
    @org.springframework.context.annotation.Lazy
    private final com.lifeos.project.repository.ProjectRepository projectRepository;
    @org.springframework.context.annotation.Lazy
    private final com.lifeos.quest.repository.QuestRepository questRepository;

    public ConsumableService(InventoryService inventoryService, ShopItemRepository shopItemRepository,
                           StreakService streakService, PlayerStateService playerStateService,
                           PlayerMetadataRepository playerMetadataRepository,
                           PlayerIdentityRepository playerIdentityRepository,
                           @org.springframework.context.annotation.Lazy com.lifeos.quest.service.QuestLifecycleService questLifecycleService,
                           @org.springframework.context.annotation.Lazy com.lifeos.project.service.DungeonArchitectService dungeonArchitectService,
                           @org.springframework.context.annotation.Lazy com.lifeos.player.service.PlayerHistoryService playerHistoryService,
                           @org.springframework.context.annotation.Lazy com.lifeos.project.repository.ProjectRepository projectRepository,
                           @org.springframework.context.annotation.Lazy com.lifeos.quest.repository.QuestRepository questRepository) {
        this.inventoryService = inventoryService;
        this.shopItemRepository = shopItemRepository;
        this.streakService = streakService;
        this.playerStateService = playerStateService;
        this.playerMetadataRepository = playerMetadataRepository;
        this.playerIdentityRepository = playerIdentityRepository;
        this.questLifecycleService = questLifecycleService;
        this.dungeonArchitectService = dungeonArchitectService;
        this.playerHistoryService = playerHistoryService;
        this.projectRepository = projectRepository;
        this.questRepository = questRepository;
    }

    @Transactional
    public void useConsumable(UUID playerId, String itemCode) {
        // ... (rest is unchanged above)
        // Note: the replace text is just what we want to inject at the top and the bottom method change.
        // Let's do two replacements to be safe. I'll replace the top injection block first.

        // 1. Validate Ownership
        if (!inventoryService.hasItem(playerId, itemCode)) {
            throw new IllegalArgumentException("You do not own this item: " + itemCode);
        }

        ShopItem item = shopItemRepository.findByCode(itemCode)
                .orElseThrow(() -> new IllegalArgumentException("Item definition not found: " + itemCode));

        // 1.5 System Embargo (Penalty Zone Check)
        if (playerStateService.hasActiveFlag(playerId, StatusFlagType.PENALTY_ZONE)) {
            // Only allow penalty-specific items if any exist in the future, for now absolute lock.
            throw new IllegalStateException("System Store and Consumables unavailable in Penalty Zone.");
        }

        // 2. Dispatch Logic
        boolean success = false;
        switch (itemCode) {
            case "ELIXIR_OF_LIFE" -> {
                applyElixirOfLife(playerId);
                success = true;
            }
            case "INSURANCE_SCROLL" -> {
                applyInsuranceScroll(playerId);
                success = true;
            }
            case "SHADOW_THEME" -> {
                applyShadowTheme(playerId);
                success = true;
            }
            case "RUNESTONE_STEALTH" -> {
                applyRunestoneStealth(playerId);
                success = true;
            }
            case "RUNESTONE_MUTILATE" -> {
                applyRunestoneMutilate(playerId);
                success = true;
            }
            case "S_RANK_RED_GATE_KEY" -> {
                applyRedGateKey(playerId);
                success = true;
            }
            case "COMMAND_ARISE" -> {
                applyCommandArise(playerId);
                success = true;
            }
            // Add more cases here (e.g. HEALING_POTION, XP_SCROLL)
            default -> throw new IllegalArgumentException("This item cannot be used directly: " + itemCode);
        }

        // 3. Decrement Inventory (only if successful)
        if (success) {
            inventoryService.consumeItem(playerId, item.getItemId(), 1);
        }
    }

    private void applyElixirOfLife(UUID playerId) {
        // Delegated to StreakService which handles validation (48h window, not in penalty, etc.)
        streakService.applyStreakRepair(playerId);
    }

    private void applyInsuranceScroll(UUID playerId) {
        // Validation: Cannot use if already in Penalty Zone
        if (playerStateService.hasActiveFlag(playerId, StatusFlagType.PENALTY_ZONE)) {
            throw new IllegalStateException("Cannot use Insurance Scroll while in Penalty Zone.");
        }

        // Apply Shield Flag (30 Days duration)
        playerStateService.applyStatusFlag(
                playerId,
                StatusFlagType.PENALTY_SHIELD,
                LocalDateTime.now().plusDays(30)
        );
    }

    private void applyShadowTheme(UUID playerId) {
        // Permanent unlock -> Update Metadata
        PlayerMetadata metadata = playerMetadataRepository.findByPlayerPlayerId(playerId)
                .orElse(PlayerMetadata.builder()
                        .player(playerIdentityRepository.getReferenceById(playerId))
                        .build());
        
        metadata.setUiTheme("shadow_purple");
        playerMetadataRepository.save(metadata);
    }

    private void applyRunestoneStealth(UUID playerId) {
        // Pauses Daily Quest generation for 3 days
        playerStateService.applyStatusFlag(
                playerId,
                StatusFlagType.RECOVERING, // Using RECOVERING or a new flag like STEALTH
                LocalDateTime.now().plusDays(3)
        );
        // Note: DailyQuestService needs to respect RECOVERING flag to skip daily generation.
        // We will add that check in DailyQuestService.
    }

    private void applyRunestoneMutilate(UUID playerId) {
        // Instantly completes 1 active Sub-Quest (Floor) in a Dungeon
        var activeSubQuests = questRepository.findByPlayerPlayerIdAndState(playerId, com.lifeos.quest.domain.enums.QuestState.ACTIVE)
            .stream()
            .filter(q -> q.getProjectId() != null)
            .toList();

        if (activeSubQuests.isEmpty()) {
            throw new IllegalStateException("No active Dungeon Floors to Mutilate.");
        }

        // Complete the first one found (instantly skipping it)
        questLifecycleService.completeQuest(activeSubQuests.get(0).getQuestId());
    }

    private void applyRedGateKey(UUID playerId) {
        // Triggers the DungeonArchitectService to generate a brutal S-Rank Dungeon
        dungeonArchitectService.generateDungeon(playerId, "Survive and conquer a spontaneous high-threat instance.", "S");
    }

    private void applyCommandArise(UUID playerId) {
        // Converts a historically failed Project into a "Shadow" (Completed) state
        // For now, throw UnsupportedOperationException until getFailedDungeons is implemented
        throw new UnsupportedOperationException("applyCommandArise not yet implemented - requires getFailedDungeons method");
    }
}
