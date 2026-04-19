package com.lifeos.onboarding.dto;

import lombok.Data;
import java.util.Map;

public class CalibrationRequest {
    private Map<String, Integer> attributeRatings; // e.g., "DISCIPLINE": 7

    public CalibrationRequest() {}

    public Map<String, Integer> getAttributeRatings() { return attributeRatings; }
    public void setAttributeRatings(Map<String, Integer> attributeRatings) { this.attributeRatings = attributeRatings; }
}
