package com.lifeos.quest.domain;

import com.lifeos.quest.domain.enums.MutationType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "quest_mutation_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestMutationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quest_id", nullable = false)
    private Quest quest;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MutationType mutationType;

    private String reason;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> oldValueJson;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> newValueJson;

    private LocalDateTime mutatedAt;

    @PrePersist
    protected void onCreate() {
        if (mutatedAt == null) mutatedAt = LocalDateTime.now();
    }
}
