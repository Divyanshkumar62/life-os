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
                25000L, ShopCategory.UPGRADE, 1, PlayerRank.B, "https://example.com/icons/rune-stealth.png");

        seedItemIfNotExists("RUNESTONE_MUTILATE", "Runestone: Mutilate",
                "Violent intervention. (Effect: Instantly completes and bypasses 1 active Dungeon Floor).",
                15000L, ShopCategory.UPGRADE, 3, PlayerRank.C, "https://example.com/icons/rune-mutilate.png");

        seedItemIfNotExists("S_RANK_RED_GATE_KEY", "S-Rank Red Gate Key",
                "A key pulsating with dark energy. (Effect: Forces entry into an S-Rank 'System Anomaly' Red Gate. High death risk).",
                50000L, ShopCategory.KEY, 1, PlayerRank.A, "https://example.com/icons/key-red-gate.png");

        seedItemIfNotExists("COMMAND_ARISE", "Command: Arise",
                "The Sovereign's authority. (Effect: Converts a historically failed Project into a completed 'Shadow' status).",
                100000L, ShopCategory.UPGRADE, 1, PlayerRank.S, "https://example.com/icons/command-arise.png");

        log.info("Shop Catalog Seeding Complete.");
    }

    private void seedItemIfNotExists(String code, String name, String description, long cost, 
                                     ShopCategory category, Integer stockLimit, PlayerRank rank, String imageUrl) {
        if (shopItemRepository.findByCode(code).isEmpty()) {
            ShopItem item = ShopItem.builder()
                    .code(code)
                    .name(name)
                    .description(description)
                    .cost(cost)
                    .category(category)
                    .stockLimit(stockLimit)
                    .rankRequirement(rank)
                    .imageUrl(imageUrl)
                    .build();
            shopItemRepository.save(item);
            log.info("Seeded new item: {}", name);
        }
    }
}
