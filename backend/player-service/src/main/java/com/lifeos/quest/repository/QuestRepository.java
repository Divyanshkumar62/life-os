package com.lifeos.quest.repository;

import com.lifeos.quest.domain.Quest;
import com.lifeos.quest.domain.enums.QuestState;
import com.lifeos.quest.domain.enums.QuestType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface QuestRepository extends JpaRepository<Quest, UUID> {
    List<Quest> findByPlayerPlayerIdAndState(UUID playerId, QuestState state);

    @org.springframework.data.jpa.repository.Query("SELECT q FROM Quest q WHERE q.player.playerId = :playerId AND q.state = :state")
    List<Quest> findActiveQuestsWithAllCategories(@org.springframework.data.repository.query.Param("playerId") UUID playerId, @org.springframework.data.repository.query.Param("state") QuestState state);
    
    long countByProjectIdAndState(UUID projectId, QuestState state);
    
    // Find active quests that have passed their deadline
    List<Quest> findByStateAndDeadlineAtBefore(QuestState state, LocalDateTime now);

    List<Quest> findByPlayerPlayerId(UUID playerId);

    java.util.Optional<Quest> findByPlayerPlayerIdAndQuestTypeAndState(UUID playerId, QuestType type, QuestState state);

    List<Quest> findByProjectId(UUID projectId);
}
