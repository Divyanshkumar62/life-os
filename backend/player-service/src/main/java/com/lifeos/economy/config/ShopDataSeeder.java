package com.lifeos.economy.config;

import com.lifeos.economy.domain.ShopItem;
import com.lifeos.economy.domain.enums.ShopCategory;
import com.lifeos.economy.repository.ShopItemRepository;
import com.lifeos.player.domain.enums.PlayerRank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ShopDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ShopDataSeeder.class);

    private final ShopItemRepository shopItemRepository;

    public ShopDataSeeder(ShopItemRepository shopItemRepository) {
        this.shopItemRepository = shopItemRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Initializing Shop Catalog (v1.2) to ensure System Integrity...");

        // Remove legacy items
        shopItemRepository.findByCode("XP_BOOSTER").ifPresent(item -> {
            log.info("Removing legacy XP_BOOSTER from catalog.");
            shopItemRepository.delete(item);
        });
        
        shopItemRepository.findByCode("XP_SCROLL").ifPresent(item -> {
            log.info("Removing legacy XP_SCROLL from catalog.");
            shopItemRepository.delete(item);
        });

        // Seed 6 core items
        seedItemIfNotExists("INSURANCE_SCROLL", "Penalty Shield",
                "Temporary protection against system penalties. Prevents one Daily Quest reset failure penalty.",
                2500, ShopCategory.CONSUMABLE, PlayerRank.F);

        seedItemIfNotExists("FATIGUE_REMEDY", "Fatigue Remedy",
                "A sweet elixir that completely restores Fatigue and vitality.",
                500, ShopCategory.CONSUMABLE, PlayerRank.F);

        seedItemIfNotExists("RUNE_OF_SWIFTNESS", "Rune of Swiftness",
                "A glowing rune that permanently increases Agility (AGI) by +1.",
                10000, ShopCategory.UPGRADE, PlayerRank.F);

        seedItemIfNotExists("RUNE_OF_BOUNTY", "Rune of Bounty",
                "A golden rune that permanently increases Sensory Perception (SEN) by +1.",
                5000, ShopCategory.UPGRADE, PlayerRank.F);

        seedItemIfNotExists("RUNE_OF_PRESENCE", "Rune of Presence",
                "A mysterious rune that permanently increases Intelligence (INT) by +1.",
                3000, ShopCategory.UPGRADE, PlayerRank.F);

        seedItemIfNotExists("MONARCH_EXEMPTION", "Monarch's Exemption",
                "A token of absolute authority. Permanently decreases daily quest completion requirements.",
                20000, ShopCategory.UPGRADE, PlayerRank.E);

        // Seed other required items for LootBoxService / dungeons (non-purchasable in catalog, but must exist in DB)
        seedItemIfNotExists("RUNESTONE_STEALTH", "Runestone: Stealth",
                "Advanced rune. (Effect: Pauses daily quest generation and streak tracking for 3 days).",
                25000, ShopCategory.UPGRADE, PlayerRank.B);

        seedItemIfNotExists("RUNESTONE_MUTILATE", "Runestone: Mutilate",
                "Violent intervention. (Effect: Instantly completes and bypasses 1 active Dungeon Floor).",
                15000, ShopCategory.UPGRADE, PlayerRank.C);

        seedItemIfNotExists("COMMAND_ARISE", "Command: Arise",
                "The Sovereign's authority. (Effect: Converts a historically failed Project into a completed 'Shadow' status).",
                100000, ShopCategory.UPGRADE, PlayerRank.S);

        seedItemIfNotExists("RANDOM_BOX", "Random Box",
                "A reward box containing various supplies.",
                0, ShopCategory.CONSUMABLE, PlayerRank.F);

        seedItemIfNotExists("BLESSED_RANDOM_BOX", "Blessed Random Box",
                "A rare reward box containing high-grade items.",
                0, ShopCategory.CONSUMABLE, PlayerRank.F);

        seedItemIfNotExists("CURSED_RANDOM_BOX", "Cursed Random Box",
                "A dangerous box radiating sinister energy.",
                0, ShopCategory.CONSUMABLE, PlayerRank.F);

        seedItemIfNotExists("BANDAGES", "Bandage",
                "Basic medical supplies. Restores fatigue.",
                100, ShopCategory.CONSUMABLE, PlayerRank.F);

        seedItemIfNotExists("SYSTEM_OVERRIDE", "System Override",
                "Auto-completes a quest.",
                10000, ShopCategory.CONSUMABLE, PlayerRank.C);

        seedItemIfNotExists("KEY_E_RANK", "E-Rank Key",
                "Access to an E-Rank gate.",
                200, ShopCategory.KEY, PlayerRank.F);

        seedItemIfNotExists("KEY_D_RANK", "D-Rank Key",
                "Access to a D-Rank gate.",
                500, ShopCategory.KEY, PlayerRank.F);

        seedItemIfNotExists("KEY_C_RANK", "C-Rank Key",
                "Access to a C-Rank gate.",
                1000, ShopCategory.KEY, PlayerRank.F);

        seedItemIfNotExists("KEY_A_RANK", "A-Rank Key",
                "Access to an A-Rank gate.",
                5000, ShopCategory.KEY, PlayerRank.C);

        seedItemIfNotExists("KEY_S_RANK", "S-Rank Key",
                "Access to an S-Rank gate.",
                10000, ShopCategory.KEY, PlayerRank.B);

        seedItemIfNotExists("RED_GATE_KEY", "Red Gate Key",
                "Forces entry into a Red Gate.",
                0, ShopCategory.KEY, PlayerRank.F);

        seedItemIfNotExists("S_RANK_RED_GATE_KEY", "S-Rank Red Gate Key",
                "A key pulsating with dark energy. (Effect: Forces entry into an S-Rank 'System Anomaly' Red Gate. High death risk).",
                50000, ShopCategory.KEY, PlayerRank.A);

        seedItemIfNotExists("SHADOW_EXTRACTION", "Shadow Extraction",
                "Extracts a shadow from a defeated foe.",
                0, ShopCategory.CONSUMABLE, PlayerRank.A);

        log.info("Shop Catalog Seeding Complete.");
    }

    private void seedItemIfNotExists(String code, String name, String description, long cost, ShopCategory category, PlayerRank rankRequirement) {
        var existingOpt = shopItemRepository.findByCode(code);
        if (existingOpt.isEmpty()) {
            ShopItem item = ShopItem.builder()
                    .code(code)
                    .name(name)
                    .description(description)
                    .cost(cost)
                    .category(category)
                    .rankRequirement(rankRequirement)
                    .build();
            shopItemRepository.save(item);
            log.info("Seeded new item: {}", name);
        } else {
            ShopItem item = existingOpt.get();
            boolean dirty = false;
            if (item.getCost() != cost) {
                item.setCost(cost);
                dirty = true;
            }
            if (!name.equals(item.getName())) {
                item.setName(name);
                dirty = true;
            }
            if (!description.equals(item.getDescription())) {
                item.setDescription(description);
                dirty = true;
            }
            if (item.getCategory() != category) {
                item.setCategory(category);
                dirty = true;
            }
            if (item.getRankRequirement() != rankRequirement) {
                item.setRankRequirement(rankRequirement);
                dirty = true;
            }
            if (dirty) {
                shopItemRepository.save(item);
                log.info("Updated existing item: {}", name);
            }
        }
    }
}