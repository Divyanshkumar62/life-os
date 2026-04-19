package com.lifeos.system.domain;

import com.lifeos.system.domain.enums.SystemEventType;
import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "system_event")
public class SystemEvent {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "VARCHAR(36)", updatable = false)
    private UUID eventId;

    @Column(nullable = false, columnDefinition = "VARCHAR(36)")
    private UUID playerId;

    @Column(nullable = false, length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(255)")
    private SystemEventType eventType;

    @Column(nullable = false)
    private boolean isConsumed = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    public SystemEvent() {
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public UUID getEventId() { return eventId; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }
    
    public UUID getPlayerId() { return playerId; }
    public void setPlayerId(UUID playerId) { this.playerId = playerId; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public SystemEventType getEventType() { return eventType; }
    public void setEventType(SystemEventType eventType) { this.eventType = eventType; }
    
    public boolean isConsumed() { return isConsumed; }
    public void setConsumed(boolean consumed) { isConsumed = consumed; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
