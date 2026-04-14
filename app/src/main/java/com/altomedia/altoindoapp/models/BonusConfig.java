package com.altomedia.altoindoapp.models;

public class BonusConfig {
    public String id;
    public double affiliateBonusPercent;
    public double checkinBonusPercent;
    public double sponsorBonusPercent;
    public double videoBonusPercent;

    public BonusConfig() {}

    public BonusConfig(String id, double affiliateBonusPercent, double checkinBonusPercent, double sponsorBonusPercent, double videoBonusPercent) {
        this.id = id;
        this.affiliateBonusPercent = affiliateBonusPercent;
        this.checkinBonusPercent = checkinBonusPercent;
        this.sponsorBonusPercent = sponsorBonusPercent;
        this.videoBonusPercent = videoBonusPercent;
    }
}
