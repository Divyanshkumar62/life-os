package com.lifeos.analytics.dto;

import java.time.LocalDate;

public class HeatmapEntryDTO {
    private LocalDate date;
    private String status; 
    private boolean intelQuestExcluded = true;

    public HeatmapEntryDTO() {}

    public HeatmapEntryDTO(LocalDate date, String status) {
        this.date = date;
        this.status = status;
        this.intelQuestExcluded = true;
    }

    public HeatmapEntryDTO(LocalDate date, String status, boolean intelQuestExcluded) {
        this.date = date;
        this.status = status;
        this.intelQuestExcluded = intelQuestExcluded;
    }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isIntelQuestExcluded() { return intelQuestExcluded; }
    public void setIntelQuestExcluded(boolean intelQuestExcluded) { this.intelQuestExcluded = intelQuestExcluded; }
}
