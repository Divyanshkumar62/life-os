package com.lifeos.system.repository;

import com.lifeos.system.domain.SystemEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SystemEventRepository extends JpaRepository<SystemEvent, UUID> {
    List<SystemEvent> findByPlayerId(UUID playerId);
    
    // Finds all unconsumed alerts for a specific player, ordered by oldest first
    List<SystemEvent> findByPlayerIdAndIsConsumedFalseOrderByCreatedAtAsc(UUID playerId);
}
