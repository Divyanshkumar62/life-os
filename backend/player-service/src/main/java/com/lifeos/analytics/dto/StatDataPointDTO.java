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

    public StatDataPointDTO() {}

    public StatDataPointDTO(LocalDate date, double str, double intel, double vit, double agi, double sen) {
        this.date = date;
        this.str = str;
        this.intel = intel;
        this.vit = vit;
        this.agi = agi;
        this.sen = sen;
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
}
