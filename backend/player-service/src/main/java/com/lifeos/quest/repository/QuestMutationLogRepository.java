package com.lifeos.quest.repository;

import com.lifeos.quest.domain.QuestMutationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuestMutationLogRepository extends JpaRepository<QuestMutationLog, Long> {
    List<QuestMutationLog> findByQuestQuestIdOrderByMutatedAtDesc(UUID questId);
}
