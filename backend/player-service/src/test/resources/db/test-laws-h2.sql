-- Register H2 Triggers
DROP TRIGGER IF EXISTS TRG_PENALTY_EMBARGO;
CREATE TRIGGER TRG_PENALTY_EMBARGO
BEFORE UPDATE ON player_progression
FOR EACH ROW
CALL "com.lifeos.db.H2SystemLawsTriggers";

DROP TRIGGER IF EXISTS TRG_RANK_CEILING;
CREATE TRIGGER TRG_RANK_CEILING
BEFORE INSERT, UPDATE ON player_progression
FOR EACH ROW
CALL "com.lifeos.db.H2SystemLawsTriggers";

DROP TRIGGER IF EXISTS TRG_STATE_EXCLUSIVITY;
CREATE TRIGGER TRG_STATE_EXCLUSIVITY
BEFORE INSERT, UPDATE ON rank_exam_attempts
FOR EACH ROW
CALL "com.lifeos.db.H2SystemLawsTriggers";

-- Note: Column indices in Java Trigger must match Hibernate generated schema.
-- PlayerProgression: id (0), current_xp, level, rank, rank_progress_score, xp_frozen, player_id
