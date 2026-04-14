package com.altomedia.altoindoapp.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.altomedia.altoindoapp.databinding.ActivityAdminDashboardBinding;
import com.google.firebase.auth.FirebaseAuth;

public class AdminDashboardActivity extends AppCompatActivity {
    private ActivityAdminDashboardBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (!AdminGuard.isAdmin(this)) {
            finish();
            return;
        }

        binding.btnManageUsers.setOnClickListener(v ->
            startActivity(new Intent(this, AdminUserManagementActivity.class)));

        binding.btnManageTransactions.setOnClickListener(v ->
            startActivity(new Intent(this, AdminTransactionManagementActivity.class)));

        binding.btnManageNotifications.setOnClickListener(v ->
            startActivity(new Intent(this, AdminNotificationManagementActivity.class)));

        binding.btnManageProducts.setOnClickListener(v ->
            startActivity(new Intent(this, AdminProductManagementActivity.class)));

        binding.btnManageSettings.setOnClickListener(v ->
            startActivity(new Intent(this, AdminSettingsActivity.class)));

        binding.btnManageBonus.setOnClickListener(v ->
            startActivity(new Intent(this, BonusManagementActivity.class)));

        binding.btnManageIdCard.setOnClickListener(v ->
            startActivity(new Intent(this, AdminIdCardManagementActivity.class)));

        binding.btnAdminLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}
