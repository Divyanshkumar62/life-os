package com.lifeos.core.repository;

import com.lifeos.core.entity.DungeonProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DungeonProjectRepository extends JpaRepository<DungeonProject, UUID> {
    List<DungeonProject> findByPlayerPlayerId(UUID playerId);
    List<DungeonProject> findByPlayerPlayerIdAndDungeonStatus(UUID playerId, String status);
}
