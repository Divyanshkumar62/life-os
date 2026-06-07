package com.lifeos.penalty.repository;

import com.lifeos.penalty.domain.PlayerJournal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PlayerJournalRepository extends JpaRepository<PlayerJournal, Long> {
    List<PlayerJournal> findByPlayerId(UUID playerId);
}
