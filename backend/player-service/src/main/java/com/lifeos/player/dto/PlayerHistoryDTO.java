package com.lifeos.player.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
public class PlayerHistoryDTO {
    private LocalDateTime lastEgoBreakerAt;
    private java.util.List<String> completedQuests;
    private java.util.List<String> failedQuests;
    private java.util.List<String> notableEvents;
    
    public PlayerHistoryDTO() {}

    public PlayerHistoryDTO(LocalDateTime lastEgoBreakerAt, java.util.List<String> completedQuests, java.util.List<String> failedQuests, java.util.List<String> notableEvents) {
        this.lastEgoBreakerAt = lastEgoBreakerAt;
        this.completedQuests = completedQuests;
        this.failedQuests = failedQuests;
        this.notableEvents = notableEvents;
    }

    // Getters and Setters
    public LocalDateTime getLastEgoBreakerAt() { return lastEgoBreakerAt; }
    public void setLastEgoBreakerAt(LocalDateTime lastEgoBreakerAt) { this.lastEgoBreakerAt = lastEgoBreakerAt; }
    public java.util.List<String> getCompletedQuests() { return completedQuests; }
    public void setCompletedQuests(java.util.List<String> completedQuests) { this.completedQuests = completedQuests; }
    public java.util.List<String> getFailedQuests() { return failedQuests; }
    public void setFailedQuests(java.util.List<String> failedQuests) { this.failedQuests = failedQuests; }
    public java.util.List<String> getNotableEvents() { return notableEvents; }
    public void setNotableEvents(java.util.List<String> notableEvents) { this.notableEvents = notableEvents; }

    public static PlayerHistoryDTOBuilder builder() {
        return new PlayerHistoryDTOBuilder();
    }

    public static class PlayerHistoryDTOBuilder {
        private LocalDateTime lastEgoBreakerAt;
        private java.util.List<String> completedQuests;
        private java.util.List<String> failedQuests;
        private java.util.List<String> notableEvents;

        public PlayerHistoryDTOBuilder lastEgoBreakerAt(LocalDateTime lastEgoBreakerAt) { this.lastEgoBreakerAt = lastEgoBreakerAt; return this; }
        public PlayerHistoryDTOBuilder completedQuests(java.util.List<String> completedQuests) { this.completedQuests = completedQuests; return this; }
        public PlayerHistoryDTOBuilder failedQuests(java.util.List<String> failedQuests) { this.failedQuests = failedQuests; return this; }
        public PlayerHistoryDTOBuilder notableEvents(java.util.List<String> notableEvents) { this.notableEvents = notableEvents; return this; }

        public PlayerHistoryDTO build() {
            return new PlayerHistoryDTO(lastEgoBreakerAt, completedQuests, failedQuests, notableEvents);
        }
    }
}
