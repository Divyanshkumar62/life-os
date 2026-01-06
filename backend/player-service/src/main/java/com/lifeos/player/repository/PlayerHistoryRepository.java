package com.lifeos.player.repository;

import com.lifeos.player.domain.PlayerHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlayerHistoryRepository extends JpaRepository<PlayerHistory, Long> {
    Optional<PlayerHistory> findByPlayerPlayerId(UUID playerId);
}
