package com.lifeos.quest.repository;

import com.lifeos.quest.domain.QuestOutcomeProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuestOutcomeProfileRepository extends JpaRepository<QuestOutcomeProfile, Long> {
    Optional<QuestOutcomeProfile> findByQuestQuestId(UUID questId);
}
