package com.lifeos.analytics.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

public class StatDataPointDTO {
    private LocalDate date;

    @JsonProperty("STR")
    private double str;

    @JsonProperty("INT")
    private double intel;

    @JsonProperty("VIT")
    private double vit;

    @JsonProperty("AGI")
    private double agi;

    @JsonProperty("SEN")
    private double sen;

    private int level;
    private String rank;
    private boolean isMilestone;
    private String milestoneLabel;

    public StatDataPointDTO() {}

    public StatDataPointDTO(LocalDate date, double str, double intel, double vit, double agi, double sen) {
        this.date = date;
        this.str = str;
        this.intel = intel;
        this.vit = vit;
        this.agi = agi;
        this.sen = sen;
        this.level = 1;
        this.rank = "E";
        this.isMilestone = false;
        this.milestoneLabel = null;
    }

    public StatDataPointDTO(LocalDate date, double str, double intel, double vit, double agi, double sen,
                            int level, String rank, boolean isMilestone, String milestoneLabel) {
        this.date = date;
        this.str = str;
        this.intel = intel;
        this.vit = vit;
        this.agi = agi;
        this.sen = sen;
        this.level = level;
        this.rank = rank;
        this.isMilestone = isMilestone;
        this.milestoneLabel = milestoneLabel;
    }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public double getStr() { return str; }
    public void setStr(double str) { this.str = str; }

    public double getIntel() { return intel; }
    public void setIntel(double intel) { this.intel = intel; }

    public double getVit() { return vit; }
    public void setVit(double vit) { this.vit = vit; }

    public double getAgi() { return agi; }
    public void setAgi(double agi) { this.agi = agi; }

    public double getSen() { return sen; }
    public void setSen(double sen) { this.sen = sen; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public String getRank() { return rank; }
    public void setRank(String rank) { this.rank = rank; }

    public boolean isMilestone() { return isMilestone; }
    public void setMilestone(boolean milestone) { isMilestone = milestone; }

    public String getMilestoneLabel() { return milestoneLabel; }
    public void setMilestoneLabel(String milestoneLabel) { this.milestoneLabel = milestoneLabel; }
}
