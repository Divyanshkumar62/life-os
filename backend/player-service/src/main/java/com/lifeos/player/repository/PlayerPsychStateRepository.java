package com.lifeos.player.repository;

import com.lifeos.player.domain.PlayerPsychState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlayerPsychStateRepository extends JpaRepository<PlayerPsychState, Long> {
    Optional<PlayerPsychState> findByPlayerPlayerId(UUID playerId);
}
