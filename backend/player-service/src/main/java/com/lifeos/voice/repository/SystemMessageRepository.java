package com.lifeos.voice.repository;

import com.lifeos.voice.domain.SystemMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface SystemMessageRepository extends JpaRepository<SystemMessage, UUID> {
    
    // Fetch unread for UI
    List<SystemMessage> findByPlayerIdAndIsReadFalseOrderByCreatedAtDesc(UUID playerId);
    
    // History
    List<SystemMessage> findByPlayerIdOrderByCreatedAtDesc(UUID playerId);

    void deleteByCreatedAtBefore(LocalDateTime cutoff);
}
