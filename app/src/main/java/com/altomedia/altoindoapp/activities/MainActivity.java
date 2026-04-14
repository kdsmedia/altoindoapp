package com.altomedia.altoindoapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.altomedia.altoindoapp.R;
import com.altomedia.altoindoapp.databinding.ActivityMainBinding;
import com.altomedia.altoindoapp.models.User;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.NumberFormat;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-6881903056221433/5741161893";

    private ActivityMainBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private InterstitialAd interstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        MobileAds.initialize(this, initializationStatus -> {});
        loadInterstitialAd();
        loadUserData();
        setupNavigation();
        setupBottomNav();
    }

    private void loadInterstitialAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this, INTERSTITIAL_AD_UNIT_ID, adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(InterstitialAd ad) {
                interstitialAd = ad;
                interstitialAd.show(MainActivity.this);
            }
            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                interstitialAd = null;
            }
        });
    }

    private void setupNavigation() {
        binding.btnTransfer.setOnClickListener(v ->
            startActivity(new Intent(this, TransferActivity.class)));

        binding.btnTopup.setOnClickListener(v ->
            startActivity(new Intent(this, TopUpActivity.class)));

        binding.btnWithdraw.setOnClickListener(v ->
            startActivity(new Intent(this, WithdrawActivity.class)));

        binding.btnHistory.setOnClickListener(v ->
            startActivity(new Intent(this, TransactionHistoryActivity.class)));

        binding.btnNotifications.setOnClickListener(v ->
            startActivity(new Intent(this, NotificationsActivity.class)));

        binding.btnSettings.setOnClickListener(v ->
            startActivity(new Intent(this, SettingsActivity.class)));

        binding.btnRanking.setOnClickListener(v ->
            startActivity(new Intent(this, RankingActivity.class)));

        binding.btnIdCard.setOnClickListener(v ->
            startActivity(new Intent(this, IdCardActivity.class)));

        binding.btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void setupBottomNav() {
        findViewById(R.id.navHome).setOnClickListener(v -> {});
        findViewById(R.id.navBonus).setOnClickListener(v -> {
            startActivity(new Intent(this, BonusActivity.class));
            finish();
        });
        findViewById(R.id.navProduk).setOnClickListener(v -> {
            startActivity(new Intent(this, ProductsActivity.class));
            finish();
        });
        findViewById(R.id.navSaldo).setOnClickListener(v -> {
            startActivity(new Intent(this, SaldoActivity.class));
            finish();
        });
        findViewById(R.id.navProfil).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
        });
        highlightNav(R.id.navHome);
    }

    private void highlightNav(int navId) {
        int[] navIds = {R.id.navHome, R.id.navBonus, R.id.navProduk, R.id.navSaldo, R.id.navProfil};
        for (int id : navIds) {
            View v = findViewById(id);
            if (v != null) v.setAlpha(id == navId ? 1.0f : 0.5f);
        }
    }

    private void loadUserData() {
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).addSnapshotListener((value, error) -> {
            if (value != null && value.exists()) {
                User user = value.toObject(User.class);
                if (user != null) {
                    binding.tvMemberName.setText(user.full_name != null && !user.full_name.isEmpty()
                        ? user.full_name : "Member");
                    binding.tvMemberId.setText("ID: " + user.member_id);
                    binding.tvBalance.setText(formatRupiah(user.balance_wallet));
                    binding.tvPoints.setText(user.points_personal + " PV");

                    if (!user.isProfileComplete()) {
                        binding.tvProfileStatus.setVisibility(View.VISIBLE);
                        binding.tvProfileStatus.setOnClickListener(v ->
                            startActivity(new Intent(this, UserProfileActivity.class)));
                    } else {
                        binding.tvProfileStatus.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    private String formatRupiah(long amount) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        return nf.format(amount);
    }
}
