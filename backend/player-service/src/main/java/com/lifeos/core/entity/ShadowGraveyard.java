package com.lifeos.core.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "shadow_graveyard")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShadowGraveyard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "graveyard_id")
    private Long graveyardId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dungeon_id", nullable = false)
    private DungeonProject dungeon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private PlayerState player;

    @Column(name = "failed_at", nullable = false)
    private LocalDateTime failedAt;

    @Column(name = "resurrected_at", nullable = false)
    private LocalDateTime resurrectedAt;

    @Column(name = "shadow_deadline", nullable = false)
    private LocalDateTime shadowDeadline;
}
