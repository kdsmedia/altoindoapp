package com.altomedia.altoindoapp.models;

public class Transaction {
    public String id;
    public String uid;
    public String type;
    public long amount;
    public String fromMemberId;
    public String toMemberId;
    public String status;
    public String createdAt;
    public String description;

    public Transaction() {}

    public Transaction(String id, String uid, String type, long amount,
                       String fromMemberId, String toMemberId,
                       String status, String createdAt, String description) {
        this.id = id;
        this.uid = uid;
        this.type = type;
        this.amount = amount;
        this.fromMemberId = fromMemberId;
        this.toMemberId = toMemberId;
        this.status = status;
        this.createdAt = createdAt;
        this.description = description;
    }
}
