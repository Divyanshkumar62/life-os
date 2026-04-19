package com.lifeos.player.repository;

import com.lifeos.player.domain.PlayerMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlayerMetadataRepository extends JpaRepository<PlayerMetadata, UUID> {
    Optional<PlayerMetadata> findByPlayerPlayerId(UUID playerId);
}
