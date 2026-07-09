-- V1__init_life_os_schema.sql
-- Initializes the core state machine schema for Life-OS in PostgreSQL

CREATE TABLE player_state (
    player_id UUID PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    level INT NOT NULL DEFAULT 1,
    xp BIGINT NOT NULL DEFAULT 0,
    gold_balance BIGINT NOT NULL DEFAULT 0,
    gold_debt BIGINT NOT NULL DEFAULT 0,
    player_rank VARCHAR(1) NOT NULL DEFAULT 'E',
    stat_str INT NOT NULL DEFAULT 10,
    stat_vit INT NOT NULL DEFAULT 10,
    stat_int INT NOT NULL DEFAULT 10,
    stat_agi INT NOT NULL DEFAULT 10,
    stat_sen INT NOT NULL DEFAULT 10,
    free_stat_points INT NOT NULL DEFAULT 0,
    onboarding_completed BOOLEAN NOT NULL DEFAULT FALSE,
    timezone_offset INT NOT NULL DEFAULT 0,
    scheduled_wakeup TIME NOT NULL DEFAULT '08:00:00',
    wakeup_lock_until TIMESTAMP NULL,
    job_class VARCHAR(20) NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE temporal_modifier (
    modifier_id BIGSERIAL PRIMARY KEY,
    player_id UUID NOT NULL REFERENCES player_state(player_id) ON DELETE CASCADE,
    modifier_type VARCHAR(30) NOT NULL,
    source_item_code VARCHAR(50) NULL,
    starts_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE dungeon_project (
    dungeon_id UUID PRIMARY KEY,
    player_id UUID NOT NULL REFERENCES player_state(player_id) ON DELETE CASCADE,
    dungeon_rank VARCHAR(1) NOT NULL,
    dungeon_status VARCHAR(20) NOT NULL,
    total_floors INT NOT NULL DEFAULT 5,
    completed_floors INT NOT NULL DEFAULT 0,
    mutilate_count INT NOT NULL DEFAULT 0,
    hard_deadline TIMESTAMP NOT NULL,
    speedrun_eligible BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE shadow_graveyard (
    graveyard_id BIGSERIAL PRIMARY KEY,
    dungeon_id UUID NOT NULL REFERENCES dungeon_project(dungeon_id) ON DELETE CASCADE,
    player_id UUID NOT NULL REFERENCES player_state(player_id) ON DELETE CASCADE,
    failed_at TIMESTAMP NOT NULL,
    resurrected_at TIMESTAMP NOT NULL,
    shadow_deadline TIMESTAMP NOT NULL
);

-- Composite index for fast temporal active status lookup
CREATE INDEX idx_temporal_player_active ON temporal_modifier(player_id, modifier_type, is_active);
