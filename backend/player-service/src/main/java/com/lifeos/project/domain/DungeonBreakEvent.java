package com.lifeos.project.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "dungeon_break_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DungeonBreakEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "player_id", nullable = false)
    private UUID playerId;

    @Column(name = "project_title", nullable = false)
    private String projectTitle;

    @Column(name = "dungeon_rank", nullable = false)
    private String dungeonRank;

    @Column(name = "gold_before", nullable = false)
    private long goldBefore;

    @Column(name = "gold_penalty_amount", nullable = false)
    private long goldPenaltyAmount;

    @Column(name = "gold_after", nullable = false)
    private long goldAfter;

    @Column(name = "vit_mitigation_percent", nullable = false)
    private double vitMitigationPercent;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "debuffs_applied", columnDefinition = "JSON")
    private List<String> debuffsApplied;

    @Column(name = "debuff_duration_hours", nullable = false)
    private int debuffDurationHours;

    @Column(name = "penalty_zone_triggered", nullable = false)
    private boolean penaltyZoneTriggered;

    @Column(name = "double_penalty_resolution", nullable = false)
    private boolean doublePenaltyResolution;

    @Column(name = "triggered_at", nullable = false)
    private LocalDateTime triggeredAt;

    @Column(name = "acknowledged", nullable = false)
    private boolean acknowledged;
}
