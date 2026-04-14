package com.altomedia.altoindoapp.models;

public class IdCardRequest {
    public String id, uid, memberId, fullName, address, status, createdAt;
    public long amount;

    public IdCardRequest() {}

    public IdCardRequest(String id, String uid, String memberId, String fullName,
                         String address, String status, String createdAt) {
        this.id = id;
        this.uid = uid;
        this.memberId = memberId;
        this.fullName = fullName;
        this.address = address;
        this.status = status;
        this.createdAt = createdAt;
        this.amount = 50000;
    }
}
