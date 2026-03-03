package com.lifeos.economy.repository;

import com.lifeos.economy.domain.UserInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserInventoryRepository extends JpaRepository<UserInventory, UUID> {
    
    Optional<UserInventory> findByPlayerPlayerIdAndItemItemId(UUID playerId, UUID itemId);
    
    List<UserInventory> findByPlayerPlayerId(UUID playerId);
    
    @Query("SELECT ui FROM UserInventory ui WHERE ui.player.playerId = :playerId AND ui.item.code = :itemCode")
    Optional<UserInventory> findByPlayerIdAndItemCode(UUID playerId, String itemCode);
}
