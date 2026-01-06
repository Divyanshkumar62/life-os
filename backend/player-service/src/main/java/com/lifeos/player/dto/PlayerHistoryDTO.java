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
    private String completedQuestsJson;
    private String failedQuestsJson;
    private String notableEventsJson;
}
