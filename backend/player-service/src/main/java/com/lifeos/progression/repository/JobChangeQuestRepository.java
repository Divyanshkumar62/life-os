package com.lifeos.progression.repository;

import com.lifeos.progression.domain.JobChangeQuest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JobChangeQuestRepository extends JpaRepository<JobChangeQuest, UUID> {
    List<JobChangeQuest> findByPlayerPlayerIdAndDay(UUID playerId, int day);
    List<JobChangeQuest> findByPlayerPlayerIdAndState(UUID playerId, JobChangeQuest.JobChangeQuestState state);
    List<JobChangeQuest> findByPlayerPlayerId(UUID playerId);
    void deleteByPlayerPlayerId(UUID playerId);
}
