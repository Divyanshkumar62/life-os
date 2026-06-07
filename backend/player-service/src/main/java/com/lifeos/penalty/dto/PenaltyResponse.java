package com.lifeos.penalty.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO wrapper for penalty actions including system notifications.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PenaltyResponse {
    private boolean penaltyActive;
    private List<String> systemMessages = new ArrayList<>();
}
