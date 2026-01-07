package com.lifeos.quest.repository;

import com.lifeos.quest.domain.QuestConstraint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuestConstraintRepository extends JpaRepository<QuestConstraint, Long> {
    List<QuestConstraint> findByQuestQuestId(UUID questId);
}
