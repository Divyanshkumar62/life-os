package com.lifeos.quest.dto;

import com.lifeos.quest.domain.enums.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestRequest {
    private UUID playerId;
    private String title;
    private String description;
    private QuestType questType;
    private DifficultyTier difficultyTier;
    private Priority priority;
    private LocalDateTime deadlineAt;
    
    // Outcome Profile Data
    private long successXp;
    private long failureXp;
    private Map<String, Double> attributeDeltas; // e.g., {"STRENGTH": 1.0}
    
    // Optional
    private boolean systemMutable;
}
