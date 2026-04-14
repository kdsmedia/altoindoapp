package com.altomedia.altoindoapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.altomedia.altoindoapp.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Random;

public class BonusActivity extends AppCompatActivity {
    private static final String REWARDED_AD_UNIT_ID = "ca-app-pub-6881903056221433/2876283260";
    private static final String BANNER_AD_UNIT_ID = "ca-app-pub-6881903056221433/3584734431";

    private RewardedAd rewardedAd;
    private Button btnTonton;
    private TextView tvBonusResult;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bonus);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnTonton = findViewById(R.id.btnTonton);
        tvBonusResult = findViewById(R.id.tvBonusResult);

        loadBannerAd();
        loadRewardedAd();
        setupBottomNav();

        btnTonton.setOnClickListener(v -> showRewardedAd());
    }

    private void loadBannerAd() {
        FrameLayout adContainer = findViewById(R.id.adBannerContainer);
        AdView adView = new AdView(this);
        adView.setAdUnitId(BANNER_AD_UNIT_ID);
        adView.setAdSize(AdSize.BANNER);
        adContainer.addView(adView);
        adView.loadAd(new AdRequest.Builder().build());
    }

    private void loadRewardedAd() {
        btnTonton.setEnabled(false);
        btnTonton.setText("Memuat iklan...");
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(this, REWARDED_AD_UNIT_ID, adRequest, new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(RewardedAd ad) {
                rewardedAd = ad;
                btnTonton.setEnabled(true);
                btnTonton.setText("\u25B6 Tonton untuk Dapatkan Bonus");
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                rewardedAd = null;
                btnTonton.setEnabled(true);
                btnTonton.setText("\u25B6 Tonton untuk Dapatkan Bonus");
            }
        });
    }

    private void showRewardedAd() {
        if (rewardedAd != null) {
            rewardedAd.show(this, rewardItem -> {
                grantBonus();
                rewardedAd = null;
                loadRewardedAd();
            });
        } else {
            Toast.makeText(this, "Iklan belum siap, coba lagi", Toast.LENGTH_SHORT).show();
            loadRewardedAd();
        }
    }

    private void grantBonus() {
        long bonusAmount = new Random().nextInt(91) + 10;
        String uid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (uid == null) return;

        db.collection("users").document(uid)
            .update("balance_wallet", FieldValue.increment(bonusAmount))
            .addOnSuccessListener(aVoid -> {
                tvBonusResult.setVisibility(View.VISIBLE);
                tvBonusResult.setText("Selamat! Anda mendapat bonus Rp." + bonusAmount);
                Toast.makeText(this, "Bonus Rp." + bonusAmount + " berhasil dikreditkan!", Toast.LENGTH_LONG).show();
            })
            .addOnFailureListener(e ->
                Toast.makeText(this, "Gagal mengkreditkan bonus", Toast.LENGTH_SHORT).show());
    }

    private void setupBottomNav() {
        findViewById(R.id.navHome).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
        findViewById(R.id.navBonus).setOnClickListener(v -> {});
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
        highlightNav(R.id.navBonus);
    }

    private void highlightNav(int navId) {
        int[] navIds = {R.id.navHome, R.id.navBonus, R.id.navProduk, R.id.navSaldo, R.id.navProfil};
        for (int id : navIds) {
            View v = findViewById(id);
            if (v != null) v.setAlpha(id == navId ? 1.0f : 0.5f);
        }
    }
}
