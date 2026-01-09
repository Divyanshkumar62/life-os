package com.lifeos.progression.repository;

import com.lifeos.progression.domain.UserBossKey;
import com.lifeos.player.domain.enums.PlayerRank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserBossKeyRepository extends JpaRepository<UserBossKey, UUID> {
    Optional<UserBossKey> findByPlayerPlayerIdAndRank(UUID playerId, PlayerRank rank);
}
