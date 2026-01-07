package com.lifeos.quest.repository;

import com.lifeos.quest.domain.Quest;
import com.lifeos.quest.domain.enums.QuestState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface QuestRepository extends JpaRepository<Quest, UUID> {
    List<Quest> findByPlayerPlayerIdAndState(UUID playerId, QuestState state);
    
    // Find active quests that have passed their deadline
    List<Quest> findByStateAndDeadlineAtBefore(QuestState state, LocalDateTime now);
}
