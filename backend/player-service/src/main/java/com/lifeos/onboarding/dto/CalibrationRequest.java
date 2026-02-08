package com.lifeos.onboarding.dto;

import lombok.Data;
import java.util.Map;

@Data
public class CalibrationRequest {
    private Map<String, Integer> attributeRatings; // e.g., "DISCIPLINE": 7
}
