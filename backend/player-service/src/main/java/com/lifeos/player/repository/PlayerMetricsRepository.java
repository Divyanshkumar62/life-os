package com.lifeos.player.repository;

import com.lifeos.player.domain.PlayerMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlayerMetricsRepository extends JpaRepository<PlayerMetrics, Long> {
    Optional<PlayerMetrics> findByPlayerPlayerId(UUID playerId);
}
