-- Law 1: Penalty Embargo
-- Prevent XP/Gold increase if player is in penalty zone
CREATE OR REPLACE FUNCTION check_penalty_embargo() RETURNS TRIGGER AS $$
BEGIN
    -- Check if player is in penalty zone using the Read Model
    IF EXISTS (SELECT 1 FROM player_state_snapshot WHERE player_id = NEW.player_id AND in_penalty_zone = TRUE) THEN
        -- Allow updates that do NOT increase XP (e.g., deductions logic might rely on UPDATE)
        -- But strict embargo says "XP must not increase"
        IF NEW.current_xp > OLD.current_xp THEN
            RAISE EXCEPTION 'Law 1 Violation: Cannot increase XP while in Penalty Zone';
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_penalty_embargo
BEFORE UPDATE ON player_progression
FOR EACH ROW
EXECUTE FUNCTION check_penalty_embargo();


-- Law 2: Rank Ceiling
-- Prevent level from exceeding rank cap without promotion
CREATE OR REPLACE FUNCTION check_rank_ceiling() RETURNS TRIGGER AS $$
DECLARE
    rank_cap INTEGER;
BEGIN
    -- Determine cap based on rank (Hardcoded values or lookup, assuming standard hex logic)
    -- Rank F=10, E=20, D=30, C=40, B=50, A=60, S=70, SS=80, SSS=90, X=100
    rank_cap := CASE NEW.rank
        WHEN 'F' THEN 10
        WHEN 'E' THEN 20
        WHEN 'D' THEN 30
        WHEN 'C' THEN 40
        WHEN 'B' THEN 50
        WHEN 'A' THEN 60
        WHEN 'S' THEN 70
        WHEN 'SS' THEN 80
        WHEN 'SSS' THEN 90
        WHEN 'X' THEN 100
        ELSE 10 -- Fallback safety
    END;

    IF NEW.level > rank_cap THEN
        RAISE EXCEPTION 'Law 2 Violation: Level % exceeds cap % for Rank %', NEW.level, rank_cap, NEW.rank;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_rank_ceiling
BEFORE INSERT OR UPDATE ON player_progression
FOR EACH ROW
EXECUTE FUNCTION check_rank_ceiling();


-- Law 3: Slot Limits
-- Limit active projects based on rank
CREATE OR REPLACE FUNCTION check_project_slots() RETURNS TRIGGER AS $$
DECLARE
    active_count INTEGER;
    slot_limit INTEGER;
    player_rank VARCHAR;
BEGIN
    IF NEW.status = 'ACTIVE' THEN
        -- Get Player Rank
        SELECT rank INTO player_rank FROM player_progression WHERE player_id = NEW.player_id;
        
        -- Define Limits (F=3, E=4, D=5, etc.)
        slot_limit := CASE player_rank
            WHEN 'F' THEN 3
            WHEN 'E' THEN 4
            WHEN 'D' THEN 5
            ELSE 6 -- Higher ranks get more
        END;

        -- Count existing active projects (excluding current if update)
        SELECT COUNT(*) INTO active_count 
        FROM project 
        WHERE player_id = NEW.player_id 
          AND status = 'ACTIVE' 
          AND project_id != NEW.project_id;

        IF active_count >= slot_limit THEN
             RAISE EXCEPTION 'Law 3 Violation: Project slot limit (%) reached for Rank %', slot_limit, player_rank;
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_project_slots
BEFORE INSERT OR UPDATE ON project
FOR EACH ROW
EXECUTE FUNCTION check_project_slots();


-- Law 5: Key Purity
-- Prevent direct modification of Boss Keys except via authorized procedure (simulated by source check)
-- Since we can't easily force a "procedure-only" update without complex roles, 
-- we will use a strict trigger that requires a specific "Context" variable or similar, 
-- BUT for V1, we will implement the "Source of Truth" table approach or just a simpler check.
-- "Allowed: Project Completion". 
-- Impl: We add a 'last_source' column to UserBossKey entity? No, we shouldn't modify entity just for this.
-- Constraint: Since we can't modify the Schema of existing tables easily in this tool step without entity sync,
-- We will implement a function `grant_boss_key` and make the trigger RAISE EXCEPTION if the update
-- didn't come from it?
-- How to detect? Transaction local variables (SET LOCAL lifeos.allow_key_update = true).

CREATE OR REPLACE FUNCTION grant_boss_key(p_player_id UUID, p_rank VARCHAR, p_amount INTEGER, p_source VARCHAR) 
RETURNS VOID AS $$
BEGIN
    IF p_source != 'PROJECT_COMPLETION' THEN
         RAISE EXCEPTION 'Law 5 Violation: Boss Keys can only be granted via PROJECT_COMPLETION';
    END IF;

    -- Set session variable to allow trigger
    PERFORM set_config('lifeos.allow_key_update', 'true', true);

    INSERT INTO user_boss_keys (id, player_id, rank, key_count)
    VALUES (gen_random_uuid(), p_player_id, p_rank, p_amount)
    ON CONFLICT (player_id, rank) 
    DO UPDATE SET key_count = user_boss_keys.key_count + p_amount;
    
    -- Reset (optional, transaction scoped anyway)
    -- PERFORM set_config('lifeos.allow_key_update', 'false', true);
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION protect_boss_keys() RETURNS TRIGGER AS $$
BEGIN
    -- Check if session variable is set
    IF current_setting('lifeos.allow_key_update', true) IS DISTINCT FROM 'true' THEN
        RAISE EXCEPTION 'Law 5 Violation: Direct update to Boss Keys prohibited. Use grant_boss_key procedure.';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_block_direct_key_insert
BEFORE INSERT OR UPDATE ON user_boss_keys
FOR EACH ROW
EXECUTE FUNCTION protect_boss_keys();
