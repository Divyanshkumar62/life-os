package com.lifeos.player.repository;

import com.lifeos.player.domain.PlayerStatusFlag;
import com.lifeos.player.domain.enums.StatusFlagType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PlayerStatusFlagRepository extends JpaRepository<PlayerStatusFlag, Long> {
    List<PlayerStatusFlag> findByPlayerPlayerId(UUID playerId);
    List<PlayerStatusFlag> findByPlayerPlayerIdAndFlag(UUID playerId, StatusFlagType flag);
}
