-- 1. CLEAN SLATE
TRUNCATE TABLE shop_item CASCADE;

-- 2. CONSUMABLES
INSERT INTO shop_item (item_id, code, name, description, cost, category, stock_limit, rank_requirement, image_url) VALUES
(gen_random_uuid(), 'STATUS_RECOVERY', 'Status Recovery', 'Instantly restores physical condition. (Effect: Repairs broken streak from last 24h).', 2500, 'CONSUMABLE', 1, 'E_RANK', 'https://your-bucket-url/icon-status-recovery.png'),
(gen_random_uuid(), 'BANDAGES', 'Bandages', 'Simple healing item. (Effect: Reduces current Penalty Zone duration by 30 mins).', 100, 'CONSUMABLE', 5, 'E_RANK', 'https://your-bucket-url/icon-bandages.png'),
(gen_random_uuid(), 'RASAKA_VENOM', 'Rasaka''s Venom', 'A dangerous buff. (Effect: "Hardening" - Grants immunity to Penalty Zone for 24h, but -20% XP gain).', 8000, 'CONSUMABLE', 1, 'C_RANK', 'https://your-bucket-url/icon-rasaka-venom.png'),
(gen_random_uuid(), 'MANA_POTION', 'Mana Potion', 'Restores 50 Mental Energy (MP).', 500, 'CONSUMABLE', 10, 'E_RANK', 'https://your-bucket-url/icon-mana-potion.png');

-- 3. KEYS
INSERT INTO shop_item (item_id, code, name, description, cost, category, stock_limit, rank_requirement, image_url) VALUES
(gen_random_uuid(), 'KEY_INSTANT_C', 'Dungeon Key: Instant Dungeon', 'Opens a C-Rank instance. (Effect: Unlocks a special "Deep Work" Quest for high XP).', 10000, 'KEY', 3, 'D_RANK', 'https://your-bucket-url/key-instant.png'),
(gen_random_uuid(), 'KEY_DEMON_CASTLE', 'Demon Castle Key', 'Access to the upper floors of the Demon Castle.', 50000, 'KEY', 1, 'S_RANK', 'https://your-bucket-url/key-demon-castle.png');

-- 4. EQUIPMENT & RUNES
INSERT INTO shop_item (item_id, code, name, description, cost, category, stock_limit, rank_requirement, image_url) VALUES
(gen_random_uuid(), 'WEAPON_KNIGHT_KILLER', 'Knight Killer', 'B-Rank Dagger. +75 Attack, +25% Damage vs Armored Types.', 35000, 'EQUIPMENT', 1, 'B_RANK', 'https://your-bucket-url/weapon-knight-killer.png'),
(gen_random_uuid(), 'ARMOR_IRON_HELMET', 'Iron Helmet', 'C-Rank Defense Gear. -5% Physical Damage taken.', 5000, 'EQUIPMENT', 1, 'C_RANK', 'https://your-bucket-url/armor-iron-helmet.png'),
(gen_random_uuid(), 'RUNE_STEALTH', 'Stealth Stone (Rune)', 'Grants the skill "Stealth". (Effect: Hides your online status/activity for 7 days).', 20000, 'UPGRADE', 1, 'B_RANK', 'https://your-bucket-url/rune-stealth.png');

-- 5. COSMETICS
INSERT INTO shop_item (item_id, code, name, description, cost, category, stock_limit, rank_requirement, image_url) VALUES
(gen_random_uuid(), 'JOB_NECROMANCER', 'Job Change: Necromancer', 'Unlocks the Necromancer UI Theme (Blue/Black). "Arise...".', 50000, 'COSMETIC', 1, 'C_RANK', 'https://your-bucket-url/theme-necromancer.png'),
(gen_random_uuid(), 'TITLE_WOLF_SLAYER', 'Title: Wolf Slayer', 'Display badge for your profile. "For those who hunt the hunters."', 5000, 'COSMETIC', 1, 'D_RANK', 'https://your-bucket-url/badge-wolf-slayer.png'),
(gen_random_uuid(), 'PET_IGRIS', 'Shadow Soldier: Igris', 'Adds Igris as your avatar/mascot in the dashboard.', 100000, 'COSMETIC', 1, 'B_RANK', 'https://your-bucket-url/avatar-igris.png');
