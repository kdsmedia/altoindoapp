package com.altomedia.altoindoapp.activities;

import android.content.Context;
import android.content.Intent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminGuard {
    public static final String ADMIN_EMAIL = "appsidhanie@gmail.com";
    public static final String ADMIN_MEMBER_ID = "14061993";
    public static final String ADMIN_PASSWORD = "Kdsmedia@123";

    public static boolean isAdmin(Context context) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return false;
        }
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        return ADMIN_EMAIL.equalsIgnoreCase(email);
    }

    public static void routeAdminIfNeeded(Context context, String email) {
        if (ADMIN_EMAIL.equalsIgnoreCase(email)) {
            context.startActivity(new Intent(context, AdminDashboardActivity.class));
        }
    }
}
