package com.lifeos.penalty.repository;

import com.lifeos.penalty.domain.PenaltyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PenaltyRecordRepository extends JpaRepository<PenaltyRecord, UUID> {
    boolean existsByQuestId(UUID questId);

    @org.springframework.data.jpa.repository.Query("SELECT p FROM PenaltyRecord p WHERE p.playerId = :playerId AND p.type = 'STAT_DEBUFF' AND p.expiresAt > CURRENT_TIMESTAMP")
    java.util.List<PenaltyRecord> findActiveDebuffs(UUID playerId);
}
