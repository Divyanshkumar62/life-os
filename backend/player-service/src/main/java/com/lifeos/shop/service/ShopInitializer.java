package com.lifeos.shop.service;

import com.lifeos.shop.domain.ShopItem;
import com.lifeos.shop.repository.ShopItemRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ShopInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ShopInitializer.class);
    private final ShopItemRepository shopItemRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Initializing shop items...");

        // Add Elixir of Second Awakening (Job Change Cooldown Skip)
        if (shopItemRepository.findByItemCode("ELIXIR_SECOND_AWAKENING").isEmpty()) {
            ShopItem elixir = ShopItem.builder()
                    .itemCode("ELIXIR_SECOND_AWAKENING")
                    .itemName("Elixir of Second Awakening")
                    .description("Instantly resets the Job Change Quest cooldown and re-triggers the prompt. Grants one more chance at the gauntlet.")
                    .goldCost(75000)
                    .itemType("CONSUMABLE")
                    .rarity("S-RANK")
                    .active(true)
                    .build();
            shopItemRepository.save(elixir);
            log.info("Added Elixir of Second Awakening to shop");
        }

        // Add Tear of the World Tree (Scholar item)
        if (shopItemRepository.findByItemCode("TEAR_WORLD_TREE").isEmpty()) {
            ShopItem tear = ShopItem.builder()
                    .itemCode("TEAR_WORLD_TREE")
                    .itemName("Tear of the World Tree")
                    .description("Instantly completes one active Intel Quest and grants +2 INT. Recommended for Scholars and Arcane Mages.")
                    .goldCost(45000)
                    .itemType("CONSUMABLE")
                    .rarity("A-RANK")
                    .active(true)
                    .build();
            shopItemRepository.save(tear);
            log.info("Added Tear of the World Tree to shop");
        }

        // Add Behemoth's Marrow (Vanguard item)
        if (shopItemRepository.findByItemCode("BEHEMOTH_MARROW").isEmpty()) {
            ShopItem marrow = ShopItem.builder()
                    .itemCode("BEHEMOTH_MARROW")
                    .itemName("Behemoth's Marrow")
                    .description("Grants a 24-hour 'Shield' that absorbs one missed physical daily quest without breaking a streak. Recommended for Vanguards and Berserkers.")
                    .goldCost(40000)
                    .itemType("CONSUMABLE")
                    .rarity("A-RANK")
                    .active(true)
                    .build();
            shopItemRepository.save(marrow);
            log.info("Added Behemoth's Marrow to shop");
        }

        // Add Vessel of the Monarch (Shadow item)
        if (shopItemRepository.findByItemCode("VESSEL_MONARCH").isEmpty()) {
            ShopItem vessel = ShopItem.builder()
                    .itemCode("VESSEL_MONARCH")
                    .itemName("Vessel of the Monarch")
                    .description("Passive artifact: Reduces the cost of 'Command: Arise' shop items by 20%. Recommended for Shadow Necromancers and future Monarchs.")
                    .goldCost(60000)
                    .itemType("ARTIFACT")
                    .rarity("S-RANK")
                    .active(true)
                    .build();
            shopItemRepository.save(vessel);
            log.info("Added Vessel of the Monarch to shop");
        }

        log.info("Shop initialization complete");
    }
}
