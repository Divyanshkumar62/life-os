package com.lifeos.economy;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test-mysql")
@Disabled("Seeding uses ShopDataSeeder at startup - manual test only")
public class SeedingTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void seedShopCatalog() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        jdbcTemplate.execute("DELETE FROM shop_item");
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");

        jdbcTemplate.execute("INSERT INTO shop_item (item_id, code, name, description, cost, category, stock_limit, rank_requirement, image_url) VALUES (UUID(), 'STATUS_RECOVERY', 'Status Recovery', 'Instantly restores physical condition.', 2500, 'CONSUMABLE', 1, 'E', 'https://your-bucket-url/icon-status-recovery.png')");
        
        jdbcTemplate.execute("INSERT INTO shop_item (item_id, code, name, description, cost, category, stock_limit, rank_requirement, image_url) VALUES (UUID(), 'BANDAGES', 'Bandages', 'Simple healing item.', 100, 'CONSUMABLE', 5, 'E', 'https://your-bucket-url/icon-bandages.png')");
        
        jdbcTemplate.execute("INSERT INTO shop_item (item_id, code, name, description, cost, category, stock_limit, rank_requirement, image_url) VALUES (UUID(), 'MANA_POTION', 'Mana Potion', 'Restores 50 Mental Energy.', 500, 'CONSUMABLE', 10, 'E', 'https://your-bucket-url/icon-mana-potion.png')");

        jdbcTemplate.execute("INSERT INTO shop_item (item_id, code, name, description, cost, category, stock_limit, rank_requirement, image_url) VALUES (UUID(), 'KEY_INSTANT_C', 'Dungeon Key', 'Opens a C-Rank instance.', 10000, 'KEY', 3, 'D', 'https://your-bucket-url/key-instant.png')");

        jdbcTemplate.execute("INSERT INTO shop_item (item_id, code, name, description, cost, category, stock_limit, rank_requirement, image_url) VALUES (UUID(), 'WEAPON_KNIGHT_KILLER', 'Knight Killer', 'B-Rank Dagger.', 35000, 'EQUIPMENT', 1, 'B', 'https://your-bucket-url/weapon-knight-killer.png')");

        jdbcTemplate.execute("INSERT INTO shop_item (item_id, code, name, description, cost, category, stock_limit, rank_requirement, image_url) VALUES (UUID(), 'ARMOR_IRON_HELMET', 'Iron Helmet', 'C-Rank Defense Gear.', 5000, 'EQUIPMENT', 1, 'C', 'https://your-bucket-url/armor-iron-helmet.png')");

        jdbcTemplate.execute("INSERT INTO shop_item (item_id, code, name, description, cost, category, stock_limit, rank_requirement, image_url) VALUES (UUID(), 'JOB_NECROMANCER', 'Job Change', 'Unlocks Necromancer Theme.', 50000, 'COSMETIC', 1, 'C', 'https://your-bucket-url/theme-necromancer.png')");

        System.out.println("Seeding Shop Catalog: COMPLETED");
    }
}
