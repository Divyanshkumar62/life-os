package com.lifeos.player.repository;

import com.lifeos.player.domain.PlayerProgression;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlayerProgressionRepository extends JpaRepository<PlayerProgression, Long> {
    Optional<PlayerProgression> findByPlayerPlayerId(UUID playerId);
}
