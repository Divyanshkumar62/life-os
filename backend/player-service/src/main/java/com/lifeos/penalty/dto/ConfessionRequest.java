package com.lifeos.penalty.dto;

public class ConfessionRequest {
    private String text;

    public ConfessionRequest() {}
    public ConfessionRequest(String text) { this.text = text; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}
