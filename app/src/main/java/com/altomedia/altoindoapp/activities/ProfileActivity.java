package com.altomedia.altoindoapp.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.altomedia.altoindoapp.R;
import com.altomedia.altoindoapp.databinding.ActivityProfileBinding;
import com.altomedia.altoindoapp.models.User;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import net.glxn.qrgen.android.QRCode;

public class ProfileActivity extends AppCompatActivity {
    private static final String BANNER_AD_UNIT_ID = "ca-app-pub-6881903056221433/3584734431";

    private ActivityProfileBinding binding;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        loadProfile();
        loadBannerAd();
        setupBottomNav();

        binding.btnEditProfile.setOnClickListener(v ->
            startActivity(new Intent(this, UserProfileActivity.class)));
    }

    private void loadBannerAd() {
        FrameLayout adContainer = binding.adBannerContainer;
        AdView adView = new AdView(this);
        adView.setAdUnitId(BANNER_AD_UNIT_ID);
        adView.setAdSize(AdSize.BANNER);
        adContainer.addView(adView);
        adView.loadAd(new AdRequest.Builder().build());
    }

    private void loadProfile() {
        String uid = FirebaseAuth.getInstance().getUid();
        db.collection("users").document(uid).addSnapshotListener((doc, error) -> {
            if (doc != null && doc.exists()) {
                User user = doc.toObject(User.class);
                if (user != null) {
                    binding.tvNameProfile.setText(user.full_name != null ? user.full_name : "");
                    binding.tvIdProfile.setText("ID: " + user.member_id);
                    binding.tvEmailProfile.setText(user.email != null ? user.email : "");

                    if (user.member_id != null && !user.member_id.isEmpty()) {
                        Bitmap qrBitmap = QRCode.from(user.member_id).withSize(512, 512).bitmap();
                        binding.ivQrCode.setImageBitmap(qrBitmap);
                    }

                    if (!user.isProfileComplete()) {
                        binding.cardProfileWarning.setVisibility(View.VISIBLE);
                    } else {
                        binding.cardProfileWarning.setVisibility(View.GONE);
                    }
                }
            }
        });
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
        findViewById(R.id.navSaldo).setOnClickListener(v -> {
            startActivity(new Intent(this, SaldoActivity.class));
            finish();
        });
        findViewById(R.id.navProfil).setOnClickListener(v -> {});
        highlightNav(R.id.navProfil);
    }

    private void highlightNav(int navId) {
        int[] navIds = {R.id.navHome, R.id.navBonus, R.id.navProduk, R.id.navSaldo, R.id.navProfil};
        for (int id : navIds) {
            View v = findViewById(id);
            if (v != null) v.setAlpha(id == navId ? 1.0f : 0.5f);
        }
    }
}
