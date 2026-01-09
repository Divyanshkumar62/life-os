package com.lifeos.player.domain;

import com.lifeos.player.domain.enums.PlayerRank;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "player_progression")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerProgression {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false, unique = true)
    private PlayerIdentity player;

    @Column(nullable = false)
    private int level;

    @Column(name = "current_xp", nullable = false)
    private long currentXp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayerRank rank;

    @Column(name = "rank_progress_score")
    private double rankProgressScore;

    @Column(name = "boss_keys", nullable = false)
    @Builder.Default
    private int bossKeys = 0;

    @Column(name = "xp_frozen", nullable = false)
    @Builder.Default
    private boolean xpFrozen = false;
}
