package com.lifeos.penalty.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfessionResponse {
    private boolean accepted;
    private String feedback;
    private int attemptsRemaining;
    private LocalDateTime lockoutUntil;
}
