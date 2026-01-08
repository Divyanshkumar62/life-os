package com.lifeos.reward.repository;

import com.lifeos.reward.domain.RewardRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RewardRecordRepository extends JpaRepository<RewardRecord, UUID> {
    boolean existsByQuestId(UUID questId);
}
