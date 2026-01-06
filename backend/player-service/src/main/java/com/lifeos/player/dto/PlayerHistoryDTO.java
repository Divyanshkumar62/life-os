package com.lifeos.player.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerHistoryDTO {
    private LocalDateTime lastEgoBreakerAt;
    private java.util.List<String> completedQuests;
    private java.util.List<String> failedQuests;
    private java.util.List<String> notableEvents;
}
