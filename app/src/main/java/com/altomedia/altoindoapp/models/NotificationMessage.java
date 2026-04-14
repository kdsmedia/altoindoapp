package com.altomedia.altoindoapp.models;

public class NotificationMessage {
    public String id;
    public String title;
    public String body;
    public String createdAt;

    public NotificationMessage() {}

    public NotificationMessage(String id, String title, String body, String createdAt) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.createdAt = createdAt;
    }
}
