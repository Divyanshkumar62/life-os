package com.lifeos.analytics.dto;

import java.time.LocalDate;

public class HeatmapEntryDTO {
    private LocalDate date;
    private String status; // "ALL_CLEARED", "PARTIAL", "FAILED", "NO_QUESTS"

    public HeatmapEntryDTO() {}

    public HeatmapEntryDTO(LocalDate date, String status) {
        this.date = date;
        this.status = status;
    }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
