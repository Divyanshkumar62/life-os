package com.lifeos.reward.domain;

import com.lifeos.reward.domain.enums.RewardComponentType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "reward_record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RewardRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID playerId;

    // Idempotency Key: Unique per Quest
    @Column(nullable = false, unique = true)
    private UUID questId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> rewardPayload; 
    // Key: RewardComponentType.name(), Value: Object (Integer, Map, etc.)
    // We use String key for JSON serialization ease, but logic uses Enum.

    @Column(nullable = false)
    private LocalDateTime appliedAt;

    @PrePersist
    protected void onCreate() {
        if (appliedAt == null) {
            appliedAt = LocalDateTime.now();
        }
    }
}
