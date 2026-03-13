package com.lifeos.quest.domain;

import com.lifeos.quest.domain.enums.ConstraintType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "quest_constraint")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestConstraint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quest_id", nullable = false)
    private Quest quest;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(255)")
    private ConstraintType constraintType;

    @Column(nullable = false, name = "constraint_value")
    private String constraintValue; // Serialized value e.g. "08:00-10:00"

    @Builder.Default
    private boolean enforced = true;
}
