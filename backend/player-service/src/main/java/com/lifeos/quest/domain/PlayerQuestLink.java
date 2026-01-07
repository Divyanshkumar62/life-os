package com.lifeos.quest.domain;

import com.lifeos.quest.domain.enums.QuestState;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

// Projection entity for efficient querying of player's quest history/status
@Entity
@Table(name = "player_quest_link")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerQuestLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private UUID playerId;

    @Column(nullable = false)
    private UUID questId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestState state;

    private LocalDateTime activatedAt;
    private LocalDateTime completedAt;
    private LocalDateTime failedAt;
}
