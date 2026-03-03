package com.lifeos.economy.repository;

import com.lifeos.economy.domain.PurchaseCooldown;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PurchaseCooldownRepository extends JpaRepository<PurchaseCooldown, UUID> {
    Optional<PurchaseCooldown> findByPlayerPlayerIdAndItemItemId(UUID playerId, UUID itemId);
}
