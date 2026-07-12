package com.lifeos.analytics.dto;

import java.time.LocalDateTime;

public class GraveyardEntryDTO {
    private Long id;
    private String text;
    private boolean accepted;
    private LocalDateTime timestamp;
    private String feedback;
    private int strikeCount;
    private Integer lockoutDurationHours;
    private String entryType;

    public GraveyardEntryDTO() {}

    public GraveyardEntryDTO(Long id, String text, boolean accepted, LocalDateTime timestamp, String feedback) {
        this.id = id;
        this.text = text;
        this.accepted = accepted;
        this.timestamp = timestamp;
        this.feedback = feedback;
        this.strikeCount = 0;
        this.lockoutDurationHours = null;
        this.entryType = "CONFESSION";
    }

    public GraveyardEntryDTO(Long id, String text, boolean accepted, LocalDateTime timestamp, String feedback,
                             int strikeCount, Integer lockoutDurationHours, String entryType) {
        this.id = id;
        this.text = text;
        this.accepted = accepted;
        this.timestamp = timestamp;
        this.feedback = feedback;
        this.strikeCount = strikeCount;
        this.lockoutDurationHours = lockoutDurationHours;
        this.entryType = entryType;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public boolean isAccepted() { return accepted; }
    public void setAccepted(boolean accepted) { this.accepted = accepted; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public int getStrikeCount() { return strikeCount; }
    public void setStrikeCount(int strikeCount) { this.strikeCount = strikeCount; }

    public Integer getLockoutDurationHours() { return lockoutDurationHours; }
    public void setLockoutDurationHours(Integer lockoutDurationHours) { this.lockoutDurationHours = lockoutDurationHours; }

    public String getEntryType() { return entryType; }
    public void setEntryType(String entryType) { this.entryType = entryType; }
}
