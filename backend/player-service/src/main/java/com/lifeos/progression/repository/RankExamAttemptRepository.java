package com.lifeos.progression.repository;

import com.lifeos.progression.domain.RankExamAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RankExamAttemptRepository extends JpaRepository<RankExamAttempt, UUID> {
}
