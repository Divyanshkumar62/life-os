package com.lifeos.player.state;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PlayerStateRepository extends JpaRepository<PlayerStateSnapshot, UUID> {
}
