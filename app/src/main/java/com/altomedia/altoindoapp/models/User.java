package com.altomedia.altoindoapp.models;

public class User {
    public String uid, member_id, upline_id, full_name, email, phone;
    public String account_type, account_name, account_owner, account_number;
    public String address, dropship_name;
    public String role, tier, security_pin;
    public boolean is_active;
    public long balance_wallet, points_personal, points_group;

    public User() {}

    public User(String uid, String member_id, String email, String full_name, String upline_id,
                String phone, String account_type, String account_name, String account_owner, String account_number) {
        this.uid = uid;
        this.member_id = member_id;
        this.email = email;
        this.full_name = full_name;
        this.upline_id = upline_id;
        this.phone = phone;
        this.account_type = account_type;
        this.account_name = account_name;
        this.account_owner = account_owner;
        this.account_number = account_number;
        this.address = "";
        this.dropship_name = "";
        this.role = "member";
        this.tier = "bronze";
        this.is_active = false;
        this.balance_wallet = 0;
        this.points_personal = 0;
        this.points_group = 0;
        this.security_pin = "123456";
    }

    public boolean isProfileComplete() {
        return full_name != null && !full_name.isEmpty()
            && phone != null && !phone.isEmpty()
            && address != null && !address.isEmpty()
            && account_name != null && !account_name.isEmpty()
            && account_owner != null && !account_owner.isEmpty()
            && account_number != null && !account_number.isEmpty();
    }
}
