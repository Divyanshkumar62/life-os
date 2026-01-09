package com.lifeos.player.repository;

import com.lifeos.player.domain.PlayerIdentity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlayerIdentityRepository extends JpaRepository<PlayerIdentity, UUID> {
    Optional<PlayerIdentity> findByUsername(String username);
    
    @Query("SELECT p.playerId FROM PlayerIdentity p")
    List<UUID> findAllIds();
}
