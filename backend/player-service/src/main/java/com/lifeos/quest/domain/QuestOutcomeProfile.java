package com.lifeos.quest.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Entity
@Table(name = "quest_outcome_profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestOutcomeProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quest_id", nullable = false)
    private Quest quest;

    private long successXp;
    private long failureXp;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Double> attributeDeltaJson; // e.g. {"STRENGTH": 0.5}

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> statusFlagsOnSuccess;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> statusFlagsOnFailure;

    // Penalty Tier logic moved to service/logic layer or simple string field if needed
    // Assuming simple mapping for now
    private String penaltyTier;
}
