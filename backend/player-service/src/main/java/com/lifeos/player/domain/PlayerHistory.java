package com.lifeos.player.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "player_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false, unique = true)
    private PlayerIdentity player;

    @Column(name = "last_ego_breaker_at")
    private LocalDateTime lastEgoBreakerAt;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(name = "completed_quests_json")
    private java.util.List<String> completedQuests; // Changed from String to List

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(name = "failed_quests_json")
    private java.util.List<String> failedQuests;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(name = "notable_events_json")
    private java.util.List<String> notableEvents;
}
