package com.lifeos.progression.repository;

import com.lifeos.progression.domain.RankExamAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

@Repository
public interface RankExamAttemptRepository extends JpaRepository<RankExamAttempt, UUID> {
    
    @Query("SELECT r FROM RankExamAttempt r WHERE r.player.playerId = :playerId ORDER BY r.unlockedAt DESC LIMIT 1")
    Optional<RankExamAttempt> findLatestByPlayerId(@Param("playerId") UUID playerId);
}
