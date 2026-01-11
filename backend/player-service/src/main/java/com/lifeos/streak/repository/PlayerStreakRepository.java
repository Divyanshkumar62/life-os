package com.lifeos.streak.repository;

import com.lifeos.streak.domain.PlayerStreak;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlayerStreakRepository extends JpaRepository<PlayerStreak, UUID> {
    Optional<PlayerStreak> findByPlayerId(UUID playerId);
}
