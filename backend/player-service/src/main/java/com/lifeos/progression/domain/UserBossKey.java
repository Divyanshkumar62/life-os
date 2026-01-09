package com.lifeos.progression.domain;

import com.lifeos.player.domain.PlayerIdentity;
import com.lifeos.player.domain.enums.PlayerRank;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "user_boss_keys", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"player_id", "rank"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBossKey {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private PlayerIdentity player;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayerRank rank; // The rank this key allows promotion FROM (e.g., E means E->D exam)

    @Column(nullable = false)
    @Builder.Default
    private int keyCount = 0;
}
