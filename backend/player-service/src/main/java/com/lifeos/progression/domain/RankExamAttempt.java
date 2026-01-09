package com.lifeos.progression.domain;

import com.lifeos.player.domain.PlayerIdentity;
import com.lifeos.player.domain.enums.PlayerRank;
import com.lifeos.progression.domain.enums.ExamStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "rank_exam_attempts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RankExamAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private PlayerIdentity player;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayerRank fromRank;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayerRank toRank;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExamStatus status;

    @Column(nullable = false)
    private int requiredKeys;

    @Column(nullable = false)
    private int consumedKeys;

    @Column(nullable = false)
    private int attemptNumber;

    private LocalDateTime unlockedAt;
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        if (status == null) status = ExamStatus.LOCKED;
    }
}
