package com.lifeos.player.repository;

import com.lifeos.player.domain.PlayerAttribute;
import com.lifeos.player.domain.enums.AttributeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlayerAttributeRepository extends JpaRepository<PlayerAttribute, Long> {
    List<PlayerAttribute> findByPlayerPlayerId(UUID playerId);
    Optional<PlayerAttribute> findByPlayerPlayerIdAndAttributeType(UUID playerId, AttributeType attributeType);
}
