package com.lifeos.core.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "temporal_modifier", indexes = {
    @Index(name = "idx_temporal_player_active", columnList = "player_id, modifier_type, is_active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemporalModifier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "modifier_id")
    private Long modifierId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private PlayerState player;

    @Column(name = "modifier_type", length = 30, nullable = false)
    private String modifierType;

    @Column(name = "source_item_code", length = 50)
    private String sourceItemCode;

    @Column(name = "starts_at", nullable = false)
    private LocalDateTime startsAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;
}
