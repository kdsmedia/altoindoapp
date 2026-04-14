package com.altomedia.altoindoapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.altomedia.altoindoapp.R;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {
    private static final String NATIVE_AD_UNIT_ID = "ca-app-pub-6881903056221433/3989096525";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        MobileAds.initialize(this, initializationStatus -> {});
        loadNativeAd();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                if (AdminGuard.ADMIN_EMAIL.equalsIgnoreCase(email)) {
                    startActivity(new Intent(this, AdminDashboardActivity.class));
                } else {
                    startActivity(new Intent(this, MainActivity.class));
                }
            } else {
                startActivity(new Intent(this, LoginActivity.class));
            }
            finish();
        }, 3000);
    }

    private void loadNativeAd() {
        AdLoader adLoader = new AdLoader.Builder(this, NATIVE_AD_UNIT_ID)
            .forNativeAd(nativeAd -> {
                FrameLayout container = findViewById(R.id.adNativeContainer);
                if (container == null || isDestroyed()) {
                    nativeAd.destroy();
                    return;
                }
                NativeAdView adView = (NativeAdView) LayoutInflater.from(this)
                    .inflate(R.layout.ad_native_layout, container, false);
                populateNativeAd(nativeAd, adView);
                container.removeAllViews();
                container.addView(adView);
                container.setVisibility(View.VISIBLE);
            })
            .build();
        adLoader.loadAd(new AdRequest.Builder().build());
    }

    private void populateNativeAd(NativeAd nativeAd, NativeAdView adView) {
        TextView headlineView = adView.findViewById(R.id.adHeadline);
        TextView bodyView = adView.findViewById(R.id.adBody);
        Button ctaView = adView.findViewById(R.id.adCallToAction);
        ImageView iconView = adView.findViewById(R.id.adAppIcon);
        MediaView mediaView = adView.findViewById(R.id.adMedia);

        headlineView.setText(nativeAd.getHeadline());
        adView.setHeadlineView(headlineView);

        if (nativeAd.getBody() != null) {
            bodyView.setText(nativeAd.getBody());
            adView.setBodyView(bodyView);
        }

        if (nativeAd.getCallToAction() != null) {
            ctaView.setText(nativeAd.getCallToAction());
            adView.setCallToActionView(ctaView);
        }

        if (nativeAd.getIcon() != null) {
            iconView.setImageDrawable(nativeAd.getIcon().getDrawable());
            adView.setIconView(iconView);
        }

        adView.setMediaView(mediaView);
        adView.setNativeAd(nativeAd);
    }
}
