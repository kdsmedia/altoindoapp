package com.altomedia.altoindoapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.altomedia.altoindoapp.R;
import com.altomedia.altoindoapp.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.NumberFormat;
import java.util.Locale;

public class SaldoActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView tvSaldoBalance, tvSaldoPvPersonal, tvSaldoPvGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saldo);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvSaldoBalance = findViewById(R.id.tvSaldoBalance);
        tvSaldoPvPersonal = findViewById(R.id.tvSaldoPvPersonal);
        tvSaldoPvGroup = findViewById(R.id.tvSaldoPvGroup);

        loadUserData();
        setupButtons();
        setupBottomNav();
    }

    private void loadUserData() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).addSnapshotListener((value, error) -> {
            if (value != null && value.exists()) {
                User user = value.toObject(User.class);
                if (user != null) {
                    NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                    tvSaldoBalance.setText(nf.format(user.balance_wallet));
                    tvSaldoPvPersonal.setText(user.points_personal + " PV");
                    tvSaldoPvGroup.setText(user.points_group + " PV");
                }
            }
        });
    }

    private void setupButtons() {
        findViewById(R.id.btnSaldoTopup).setOnClickListener(v ->
            startActivity(new Intent(this, TopUpActivity.class)));

        findViewById(R.id.btnSaldoWithdraw).setOnClickListener(v ->
            startActivity(new Intent(this, WithdrawActivity.class)));

        findViewById(R.id.btnSaldoTransfer).setOnClickListener(v ->
            startActivity(new Intent(this, TransferActivity.class)));

        findViewById(R.id.btnSaldoHistory).setOnClickListener(v ->
            startActivity(new Intent(this, TransactionHistoryActivity.class)));
    }

    private void setupBottomNav() {
        findViewById(R.id.navHome).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
        findViewById(R.id.navBonus).setOnClickListener(v -> {
            startActivity(new Intent(this, BonusActivity.class));
            finish();
        });
        findViewById(R.id.navProduk).setOnClickListener(v -> {
            startActivity(new Intent(this, ProductsActivity.class));
            finish();
        });
        findViewById(R.id.navSaldo).setOnClickListener(v -> {});
        findViewById(R.id.navProfil).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
        });
        highlightNav(R.id.navSaldo);
    }

    private void highlightNav(int navId) {
        int[] navIds = {R.id.navHome, R.id.navBonus, R.id.navProduk, R.id.navSaldo, R.id.navProfil};
        for (int id : navIds) {
            View v = findViewById(id);
            if (v != null) v.setAlpha(id == navId ? 1.0f : 0.5f);
        }
    }
}
