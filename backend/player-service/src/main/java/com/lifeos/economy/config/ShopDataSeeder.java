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
        log.info("Initializing Shop Catalog (v1.1) to ensure System Integrity...");

        shopItemRepository.findByCode("XP_BOOSTER").ifPresent(item -> {
            log.info("Removing legacy XP_BOOSTER from catalog.");
            shopItemRepository.delete(item);
        });
        
        shopItemRepository.findByCode("XP_SCROLL").ifPresent(item -> {
            log.info("Removing legacy XP_SCROLL from catalog.");
            shopItemRepository.delete(item);
        });

        seedItemIfNotExists("RUNESTONE_STEALTH", "Runestone: Stealth",
                "Advanced rune. (Effect: Pauses daily quest generation and streak tracking for 3 days).",
                25000, ShopCategory.UPGRADE, PlayerRank.B);

        seedItemIfNotExists("RUNESTONE_MUTILATE", "Runestone: Mutilate",
                "Violent intervention. (Effect: Instantly completes and bypasses 1 active Dungeon Floor).",
                15000, ShopCategory.UPGRADE, PlayerRank.C);

        seedItemIfNotExists("S_RANK_RED_GATE_KEY", "S-Rank Red Gate Key",
                "A key pulsating with dark energy. (Effect: Forces entry into an S-Rank 'System Anomaly' Red Gate. High death risk).",
                50000, ShopCategory.KEY, PlayerRank.A);

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

        seedItemIfNotExists("INSURANCE_SCROLL", "Penalty Shield",
                "Temporary protection against system penalties.",
                500, ShopCategory.CONSUMABLE, PlayerRank.E);

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

        seedItemIfNotExists("SHADOW_EXTRACTION", "Shadow Extraction",
                "Extracts a shadow from a defeated foe.",
                0, ShopCategory.CONSUMABLE, PlayerRank.A);

        log.info("Shop Catalog Seeding Complete.");
    }

    private void seedItemIfNotExists(String code, String name, String description, long cost, ShopCategory category, PlayerRank rankRequirement) {
        if (shopItemRepository.findByCode(code).isEmpty()) {
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
        }
    }
}