package com.lifeos.economy.service;

import com.lifeos.economy.dto.LootBoxResult;
import com.lifeos.economy.domain.ShopItem;
import com.lifeos.economy.repository.ShopItemRepository;
import com.lifeos.economy.service.InventoryService;
import com.lifeos.player.domain.enums.AttributeType;
import com.lifeos.player.dto.PlayerAttributeDTO;
import com.lifeos.player.dto.PlayerStateResponse;
import com.lifeos.player.service.PlayerStateService;
import com.lifeos.player.domain.enums.StatusFlagType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Service handling the opening of loot boxes and determining RNG drops.
 * SEN stat shifts the RANDOM_BOX drop table: each SEN point moves 0.2% probability
 * from the Common Gold tier to the Dungeon Key tier, capped at 30% total shift.
 */
@Service
@Slf4j
public class LootBoxService {

    private final InventoryService inventoryService;
    private final ShopItemRepository shopItemRepository;
    private final PlayerStateService playerStateService;
    private final EconomyService economyService;
    private final Random random = new Random();

    public LootBoxService(InventoryService inventoryService, ShopItemRepository shopItemRepository,
                         PlayerStateService playerStateService, EconomyService economyService) {
        this.inventoryService = inventoryService;
        this.shopItemRepository = shopItemRepository;
        this.playerStateService = playerStateService;
        this.economyService = economyService;
    }

    /**
     * Opens a loot box for the given player.
     * Consumes the box item and returns the resulting reward.
     */
    @Transactional
    public LootBoxResult openBox(UUID playerId, String boxCode) {
        // Verify player owns the box
        if (!inventoryService.hasItem(playerId, boxCode)) {
            throw new IllegalArgumentException("Player does not own box: " + boxCode);
        }

        // Resolve the ShopItem for the box to ensure it is a consumable box
        ShopItem boxItem = shopItemRepository.findByCode(boxCode)
                .orElseThrow(() -> new IllegalArgumentException("Box definition not found: " + boxCode));

        // Consume one box
        inventoryService.consumeItem(playerId, boxItem.getItemId(), 1);

        // Determine drop based on box type
        return switch (boxCode) {
            case "RANDOM_BOX" -> handleRandomBox(playerId);
            case "BLESSED_RANDOM_BOX" -> handleBlessedBox(playerId);
            case "CURSED_RANDOM_BOX" -> handleCursedBox(playerId);
            default -> LootBoxResult.builder()
                    .boxType(boxCode)
                    .dropType("ITEM")
                    .itemCode(boxCode)
                    .itemName(boxItem.getName())
                    .message("You used the item, but it had no special effect.")
                    .systemMessages(List.of())
                    .build();
        };
    }

    private LootBoxResult handleRandomBox(UUID playerId) {
        List<String> systemMessages = new ArrayList<>();

        // --- SEN ATTRIBUTE UTILITY: Shift drop probabilities ---
        int senValue = getSenValue(playerId);
        // Each SEN point shifts 0.2% from Gold to Key; capped at 30% (=SEN 150 max shift)
        double senShift = Math.min(senValue * 0.002, 0.30); // 0.0 – 0.30
        double goldThreshold = 0.60 - senShift;   // normally 60%, reduced by SEN
        double keyThreshold  = 1.0 - senShift;    // normally 5% key window, expanded by SEN

        if (senValue > 0) {
            systemMessages.add(String.format(
                "[SYSTEM] High SEN stat (%d) altered drop probabilities. Key chance +%.1f%%.",
                senValue, senShift * 100
            ));
        }

        double roll = random.nextDouble(); // 0.0 – 1.0

        // Gold window (normally 60%, shrinks with SEN)
        if (roll < goldThreshold) {
            int amount = 50 + random.nextInt(151); // 50-200 inclusive
            economyService.addGold(playerId, amount, "Random Box Reward");
            return LootBoxResult.builder()
                    .boxType("RANDOM_BOX")
                    .dropType("GOLD")
                    .amount((long) amount)
                    .message("You found " + amount + " gold!")
                    .systemMessages(systemMessages)
                    .build();
        }
        // 25% Bandage (fixed)
        if (roll < goldThreshold + 0.25) {
            addItemToInventory(playerId, "BANDAGES", "Bandage");
            return LootBoxResult.builder()
                    .boxType("RANDOM_BOX")
                    .dropType("ITEM")
                    .itemCode("BANDAGES")
                    .itemName("Bandage")
                    .message("You received a Bandage. Restores fatigue.")
                    .systemMessages(systemMessages)
                    .build();
        }
        // 10% Penalty Shield (fixed)
        if (roll < goldThreshold + 0.35) {
            addItemToInventory(playerId, "INSURANCE_SCROLL", "Penalty Shield");
            return LootBoxResult.builder()
                    .boxType("RANDOM_BOX")
                    .dropType("ITEM")
                    .itemCode("INSURANCE_SCROLL")
                    .itemName("Penalty Shield")
                    .message("You received a Penalty Shield. Grants temporary protection.")
                    .systemMessages(systemMessages)
                    .build();
        }
        // Key window (normally 5%, expands with SEN)
        String[] lowKeys = {"KEY_E_RANK", "KEY_D_RANK", "KEY_C_RANK"};
        String selected = lowKeys[random.nextInt(lowKeys.length)];
        addItemToInventory(playerId, selected, "Low-Tier Key");
        return LootBoxResult.builder()
                .boxType("RANDOM_BOX")
                .dropType("ITEM")
                .itemCode(selected)
                .itemName("Low-Tier Key")
                .message("You received a " + selected + ". Use it at a Red Gate.")
                .systemMessages(systemMessages)
                .build();
    }

    private LootBoxResult handleBlessedBox(UUID playerId) {
        double roll = random.nextDouble();
        // 55% Gold (1000-2500)
        if (roll < 0.55) {
            int amount = 1000 + random.nextInt(1501);
            economyService.addGold(playerId, amount, "Blessed Random Box Reward");
            return LootBoxResult.builder()
                    .boxType("BLESSED_RANDOM_BOX")
                    .dropType("GOLD")
                    .amount((long) amount)
                    .message("Blessed gold: " + amount)
                    .systemMessages(List.of())
                    .build();
        }
        // 40% High-Tier Key (A or S)
        if (roll < 0.95) {
            String[] highKeys = {"KEY_A_RANK", "KEY_S_RANK"};
            String selected = highKeys[random.nextInt(highKeys.length)];
            addItemToInventory(playerId, selected, "High-Tier Key");
            return LootBoxResult.builder()
                    .boxType("BLESSED_RANDOM_BOX")
                    .dropType("ITEM")
                    .itemCode(selected)
                    .itemName("High-Tier Key")
                    .message("You received a " + selected + ".")
                    .systemMessages(List.of())
                    .build();
        }
        // 5% System Override
        addItemToInventory(playerId, "SYSTEM_OVERRIDE", "System Override");
        return LootBoxResult.builder()
                .boxType("BLESSED_RANDOM_BOX")
                .dropType("ITEM")
                .itemCode("SYSTEM_OVERRIDE")
                .itemName("System Override")
                .message("You received a System Override. Use to auto-complete a quest.")
                .systemMessages(List.of())
                .build();
    }

    private LootBoxResult handleCursedBox(UUID playerId) {
        double roll = random.nextDouble();
        // 40% 1 Gold
        if (roll < 0.40) {
            economyService.addGold(playerId, 1L, "Cursed Random Box Reward");
            return LootBoxResult.builder()
                    .boxType("CURSED_RANDOM_BOX")
                    .dropType("GOLD")
                    .amount(1L)
                    .message("A meager 1 gold.")
                    .systemMessages(List.of())
                    .build();
        }
        // 40% Red Gate Key
        if (roll < 0.80) {
            addItemToInventory(playerId, "RED_GATE_KEY", "Red Gate Key");
            return LootBoxResult.builder()
                    .boxType("CURSED_RANDOM_BOX")
                    .dropType("ITEM")
                    .itemCode("RED_GATE_KEY")
                    .itemName("Red Gate Key")
                    .message("You received a Red Gate Key. Use at your peril.")
                    .systemMessages(List.of())
                    .build();
        }
        // 20% Shadow Extraction
        addItemToInventory(playerId, "SHADOW_EXTRACTION", "Shadow Extraction");
        return LootBoxResult.builder()
                .boxType("CURSED_RANDOM_BOX")
                .dropType("ITEM")
                .itemCode("SHADOW_EXTRACTION")
                .itemName("Shadow Extraction")
                .message("You received a Shadow Extraction. Its effect is mysterious.")
                .systemMessages(List.of())
                .build();
    }

    /**
     * Retrieves the player's SEN attribute value for drop table modification.
     * Returns 0 safely if not found.
     */
    private int getSenValue(UUID playerId) {
        try {
            PlayerStateResponse state = playerStateService.getPlayerState(playerId);
            if (state.getAttributes() == null) return 0;
            return state.getAttributes().stream()
                    .filter(a -> AttributeType.SEN == a.getAttributeType())
                    .mapToInt(a -> (int) a.getCurrentValue())
                    .findFirst()
                    .orElse(0);
        } catch (Exception e) {
            log.warn("Could not fetch SEN for player {}: {}", playerId, e.getMessage());
            return 0;
        }
    }

    private void addItemToInventory(UUID playerId, String code, String name) {
        ShopItem item = shopItemRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Item not found: " + code));
        inventoryService.addItem(playerId, item.getItemId(), 1);
    }
}
