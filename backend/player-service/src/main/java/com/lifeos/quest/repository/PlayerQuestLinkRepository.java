package com.lifeos.quest.repository;

import com.lifeos.quest.domain.PlayerQuestLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlayerQuestLinkRepository extends JpaRepository<PlayerQuestLink, Long> {
    Optional<PlayerQuestLink> findByPlayerIdAndQuestId(UUID playerId, UUID questId);
    List<PlayerQuestLink> findByPlayerId(UUID playerId);
}
