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
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
}
