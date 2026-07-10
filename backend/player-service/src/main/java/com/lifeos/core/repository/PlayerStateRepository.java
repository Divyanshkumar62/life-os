package com.lifeos.core.repository;

import com.lifeos.core.entity.PlayerState;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository("corePlayerStateRepository")
public interface PlayerStateRepository extends JpaRepository<PlayerState, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PlayerState p WHERE p.playerId = :id")
    Optional<PlayerState> findAndLockById(@Param("id") UUID id);

    @Query("SELECT COUNT(q) > 0 FROM Quest q WHERE q.player.playerId = :playerId AND q.category = com.lifeos.quest.domain.enums.QuestCategory.SYSTEM_DAILY AND q.state IN (com.lifeos.quest.domain.enums.QuestState.ACTIVE, com.lifeos.quest.domain.enums.QuestState.ASSIGNED)")
    boolean hasFailedActiveDailies(@Param("playerId") UUID playerId);
}
