package com.lifeos.quest.domain;

import com.lifeos.quest.domain.enums.*;
import com.lifeos.player.domain.PlayerIdentity;
import com.lifeos.player.domain.enums.AttributeType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "quest")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID questId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private PlayerIdentity player;

    @Column(name = "project_id")
    private UUID projectId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestType questType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "primary_attribute")
    private AttributeType primaryAttribute;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DifficultyTier difficultyTier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestState state;

    private LocalDateTime assignedAt;
    private LocalDateTime startsAt;
    private LocalDateTime deadlineAt;
    private LocalDateTime lastModifiedAt;

    @Builder.Default
    private boolean systemMutable = true;

    private boolean egoBreakerFlag;

    private double expectedFailureProbability;

    @PrePersist
    protected void onCreate() {
        if (assignedAt == null) assignedAt = LocalDateTime.now();
        if (lastModifiedAt == null) lastModifiedAt = LocalDateTime.now();
        if (category == null) category = QuestCategory.NORMAL;
        // Invariant: RED difficulty implies egoBreaker
        if (difficultyTier == DifficultyTier.RED) {
            egoBreakerFlag = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastModifiedAt = LocalDateTime.now();
        if (difficultyTier == DifficultyTier.RED) {
            egoBreakerFlag = true;
        }
    }
}
