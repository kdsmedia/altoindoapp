package com.altomedia.altoindoapp.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import com.altomedia.altoindoapp.R;
import com.altomedia.altoindoapp.models.Product;
import com.altomedia.altoindoapp.models.User;
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.List;

public class ProductsActivity extends AppCompatActivity {
    private static final String BANNER_AD_UNIT_ID = "ca-app-pub-6881903056221433/3584734431";

    private LinearLayout productListContainer;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private SwitchCompat switchDropship;
    private LinearLayout layoutDropshipName;
    private EditText etDropshipName;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);

        productListContainer = findViewById(R.id.product_list_container);
        switchDropship = findViewById(R.id.switchDropship);
        layoutDropshipName = findViewById(R.id.layoutDropshipName);
        etDropshipName = findViewById(R.id.etDropshipName);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        loadBannerAd();
        loadCurrentUser();
        loadProducts();
        setupBottomNav();

        switchDropship.setOnCheckedChangeListener((buttonView, isChecked) -> {
            layoutDropshipName.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (isChecked && currentUser != null && currentUser.dropship_name != null
                && !currentUser.dropship_name.isEmpty()) {
                etDropshipName.setText(currentUser.dropship_name);
            }
        });
    }

    private void loadBannerAd() {
        FrameLayout adContainer = findViewById(R.id.adBannerContainer);
        AdView adView = new AdView(this);
        adView.setAdUnitId(BANNER_AD_UNIT_ID);
        adView.setAdSize(AdSize.BANNER);
        adContainer.addView(adView);
        adView.loadAd(new AdRequest.Builder().build());
    }

    private void loadCurrentUser() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                currentUser = doc.toObject(User.class);
            }
        });
    }

    private void loadProducts() {
        db.collection("products").whereEqualTo("active", true).get().addOnSuccessListener(querySnapshot -> {
            productListContainer.removeAllViews();
            for (QueryDocumentSnapshot doc : querySnapshot) {
                Product product = doc.toObject(Product.class);
                LinearLayout item = createProductItem(product);
                productListContainer.addView(item);
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "Gagal memuat produk", Toast.LENGTH_SHORT).show());
    }

    private LinearLayout createProductItem(Product product) {
        LinearLayout parent = new LinearLayout(this);
        parent.setOrientation(LinearLayout.VERTICAL);
        parent.setPadding(16, 16, 16, 16);
        parent.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 16);
        parent.setLayoutParams(params);

        // Product Image
        if (product.imageUrls != null && !product.imageUrls.isEmpty()) {
            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 200));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Glide.with(this).load(product.imageUrls.get(0)).into(imageView);
            parent.addView(imageView);
        }

        TextView title = new TextView(this);
        title.setText(product.name + " - Rp " + product.price);
        title.setTextSize(16f);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        parent.addView(title);

        TextView body = new TextView(this);
        StringBuilder description = new StringBuilder(product.description);
        if (product.discountPrice > 0) {
            description.append("\nDiskon: Rp ").append(product.discountPrice);
        }
        if (product.variants != null && !product.variants.isEmpty()) {
            description.append("\nVarian: ").append(String.join(", ", product.variants));
        }
        description.append("\nKomisi: ").append(product.commissionPercent).append("%");
        body.setText(description.toString());
        parent.addView(body);

        LinearLayout buttonLayout = new LinearLayout(this);
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);

        Button btnBuy = new Button(this);
        btnBuy.setText("Beli");
        btnBuy.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        btnBuy.setOnClickListener(v -> {
            if (currentUser == null || !currentUser.isProfileComplete()) {
                Toast.makeText(this, "Lengkapi data profil dan alamat terlebih dahulu", Toast.LENGTH_LONG).show();
                startActivity(new Intent(this, UserProfileActivity.class));
                return;
            }
            String senderName = switchDropship.isChecked()
                ? etDropshipName.getText().toString().trim()
                : currentUser.full_name;
            String details = "Produk: " + product.name
                + "\nHarga: Rp " + product.price
                + "\nPengirim: " + senderName
                + "\nAlamat: " + currentUser.address;
            Toast.makeText(this, details, Toast.LENGTH_LONG).show();
        });
        buttonLayout.addView(btnBuy);

        Button btnShare = new Button(this);
        btnShare.setText("Share");
        btnShare.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        btnShare.setOnClickListener(v -> {
            Intent send = new Intent();
            send.setAction(Intent.ACTION_SEND);
            send.putExtra(Intent.EXTRA_TEXT, "Lihat produk: " + product.name + " - " + product.description);
            send.setType("text/plain");
            startActivity(Intent.createChooser(send, "Bagikan produk"));
        });
        buttonLayout.addView(btnShare);

        parent.addView(buttonLayout);

        return parent;
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
        findViewById(R.id.navProduk).setOnClickListener(v -> {});
        findViewById(R.id.navSaldo).setOnClickListener(v -> {
            startActivity(new Intent(this, SaldoActivity.class));
            finish();
        });
        findViewById(R.id.navProfil).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
        });
        highlightNav(R.id.navProduk);
    }

    private void highlightNav(int navId) {
        int[] navIds = {R.id.navHome, R.id.navBonus, R.id.navProduk, R.id.navSaldo, R.id.navProfil};
        for (int id : navIds) {
            View v = findViewById(id);
            if (v != null) v.setAlpha(id == navId ? 1.0f : 0.5f);
        }
    }
}
