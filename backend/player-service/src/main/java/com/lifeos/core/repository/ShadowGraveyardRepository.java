package com.lifeos.core.repository;

import com.lifeos.core.entity.ShadowGraveyard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ShadowGraveyardRepository extends JpaRepository<ShadowGraveyard, Long> {
    List<ShadowGraveyard> findByPlayerPlayerId(UUID playerId);
    List<ShadowGraveyard> findByDungeonDungeonId(UUID dungeonId);
}
