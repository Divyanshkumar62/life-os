package com.lifeos.player.repository;

import com.lifeos.player.domain.PlayerTemporalState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlayerTemporalStateRepository extends JpaRepository<PlayerTemporalState, Long> {
    Optional<PlayerTemporalState> findByPlayerPlayerId(UUID playerId);
}
