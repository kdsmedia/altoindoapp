package com.altomedia.altoindoapp.models;

public class AppSetting {
    public String id;
    public String contactEmail;
    public String contactPhone;
    public String socialUrl;
    public String otherUrl;

    public AppSetting() {}

    public AppSetting(String id, String contactEmail, String contactPhone, String socialUrl, String otherUrl) {
        this.id = id;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.socialUrl = socialUrl;
        this.otherUrl = otherUrl;
    }
}
