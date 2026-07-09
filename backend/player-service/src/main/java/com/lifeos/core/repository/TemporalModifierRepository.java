package com.lifeos.core.repository;

import com.lifeos.core.entity.TemporalModifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TemporalModifierRepository extends JpaRepository<TemporalModifier, Long> {
    List<TemporalModifier> findByPlayerPlayerId(UUID playerId);
    List<TemporalModifier> findByPlayerPlayerIdAndIsActive(UUID playerId, boolean isActive);
}
