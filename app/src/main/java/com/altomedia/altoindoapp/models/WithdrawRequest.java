package com.altomedia.altoindoapp.models;

public class WithdrawRequest {
    public String id;
    public String uid;
    public long amount;
    public String bankName;
    public String accountNumber;
    public String accountHolder;
    public String status;
    public String created_at;

    public WithdrawRequest() {}

    public WithdrawRequest(String id, String uid, long amount, String bankName, String accountNumber, String accountHolder, String status, String created_at) {
        this.id = id;
        this.uid = uid;
        this.amount = amount;
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.accountHolder = accountHolder;
        this.status = status;
        this.created_at = created_at;
    }
}
