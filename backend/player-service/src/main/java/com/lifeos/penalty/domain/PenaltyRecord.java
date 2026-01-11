package com.lifeos.penalty.domain;

import com.lifeos.penalty.domain.enums.PenaltySeverity;
import com.lifeos.penalty.domain.enums.PenaltyType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "penalty_record")
public class PenaltyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID playerId;

    @Column(nullable = false, unique = true) // Idempotency Guard
    private UUID questId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PenaltyType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PenaltySeverity severity;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> valuePayload; // Stores amount, attr type, duration, etc.

    @Column(nullable = false)
    private LocalDateTime appliedAt;

    private LocalDateTime expiresAt; // Nullable
    
    public PenaltyRecord() {}

    public PenaltyRecord(UUID id, UUID playerId, UUID questId, PenaltyType type, PenaltySeverity severity, Map<String, Object> valuePayload, LocalDateTime appliedAt, LocalDateTime expiresAt) {
        this.id = id;
        this.playerId = playerId;
        this.questId = questId;
        this.type = type;
        this.severity = severity;
        this.valuePayload = valuePayload;
        this.appliedAt = appliedAt;
        this.expiresAt = expiresAt;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getPlayerId() { return playerId; }
    public void setPlayerId(UUID playerId) { this.playerId = playerId; }
    public UUID getQuestId() { return questId; }
    public void setQuestId(UUID questId) { this.questId = questId; }
    public PenaltyType getType() { return type; }
    public void setType(PenaltyType type) { this.type = type; }
    public PenaltySeverity getSeverity() { return severity; }
    public void setSeverity(PenaltySeverity severity) { this.severity = severity; }
    public Map<String, Object> getValuePayload() { return valuePayload; }
    public void setValuePayload(Map<String, Object> valuePayload) { this.valuePayload = valuePayload; }
    public LocalDateTime getAppliedAt() { return appliedAt; }
    public void setAppliedAt(LocalDateTime appliedAt) { this.appliedAt = appliedAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public static PenaltyRecordBuilder builder() {
        return new PenaltyRecordBuilder();
    }

    public static class PenaltyRecordBuilder {
        private UUID id;
        private UUID playerId;
        private UUID questId;
        private PenaltyType type;
        private PenaltySeverity severity;
        private Map<String, Object> valuePayload;
        private LocalDateTime appliedAt;
        private LocalDateTime expiresAt;

        public PenaltyRecordBuilder id(UUID id) { this.id = id; return this; }
        public PenaltyRecordBuilder playerId(UUID playerId) { this.playerId = playerId; return this; }
        public PenaltyRecordBuilder questId(UUID questId) { this.questId = questId; return this; }
        public PenaltyRecordBuilder type(PenaltyType type) { this.type = type; return this; }
        public PenaltyRecordBuilder severity(PenaltySeverity severity) { this.severity = severity; return this; }
        public PenaltyRecordBuilder valuePayload(Map<String, Object> valuePayload) { this.valuePayload = valuePayload; return this; }
        public PenaltyRecordBuilder appliedAt(LocalDateTime appliedAt) { this.appliedAt = appliedAt; return this; }
        public PenaltyRecordBuilder expiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; return this; }

        public PenaltyRecord build() {
            return new PenaltyRecord(id, playerId, questId, type, severity, valuePayload, appliedAt, expiresAt);
        }
    }
}
