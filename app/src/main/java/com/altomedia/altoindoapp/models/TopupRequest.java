package com.altomedia.altoindoapp.models;

public class TopupRequest {
    public String id;
    public String uid;
    public long amount;
    public String status;
    public String trx_id;
    public String sender_name;
    public String sender_message;
    public String created_at;

    public TopupRequest() {}

    public TopupRequest(String id, String uid, long amount, String status, String trx_id, String sender_name, String sender_message, String created_at) {
        this.id = id;
        this.uid = uid;
        this.amount = amount;
        this.status = status;
        this.trx_id = trx_id;
        this.sender_name = sender_name;
        this.sender_message = sender_message;
        this.created_at = created_at;
    }
}
