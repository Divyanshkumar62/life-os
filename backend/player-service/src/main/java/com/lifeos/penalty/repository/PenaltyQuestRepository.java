package com.lifeos.penalty.repository;

import com.lifeos.penalty.domain.PenaltyQuest;
import com.lifeos.penalty.domain.enums.PenaltyQuestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PenaltyQuestRepository extends JpaRepository<PenaltyQuest, UUID> {
    
    Optional<PenaltyQuest> findByPlayerIdAndStatus(UUID playerId, PenaltyQuestStatus status);

    boolean existsByPlayerIdAndStatus(UUID playerId, PenaltyQuestStatus status);
}
