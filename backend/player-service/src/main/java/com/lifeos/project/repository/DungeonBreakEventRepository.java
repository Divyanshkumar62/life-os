package com.lifeos.project.repository;

import com.lifeos.project.domain.DungeonBreakEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DungeonBreakEventRepository extends JpaRepository<DungeonBreakEvent, UUID> {
    Optional<DungeonBreakEvent> findFirstByPlayerIdAndAcknowledgedFalseOrderByTriggeredAtDesc(UUID playerId);
    Optional<DungeonBreakEvent> findByProjectIdAndAcknowledgedFalse(UUID projectId);
}
