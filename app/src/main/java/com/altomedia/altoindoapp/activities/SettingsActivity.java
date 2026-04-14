package com.altomedia.altoindoapp.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.altomedia.altoindoapp.databinding.ActivitySettingsBinding;
import com.altomedia.altoindoapp.models.AppSetting;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingsActivity extends AppCompatActivity {
    private ActivitySettingsBinding binding;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        loadSettings();

        binding.tvSocialUrl.setOnClickListener(v -> openUrl(binding.tvSocialUrl.getText().toString()));
        binding.tvOtherUrl.setOnClickListener(v -> openUrl(binding.tvOtherUrl.getText().toString()));
    }

    private void loadSettings() {
        db.collection("app_settings").document("default").get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    AppSetting setting = documentSnapshot.toObject(AppSetting.class);
                    if (setting != null) {
                        binding.tvContactEmail.setText(setting.contactEmail != null ? setting.contactEmail : "-");
                        binding.tvContactPhone.setText(setting.contactPhone != null ? setting.contactPhone : "-");
                        binding.tvSocialUrl.setText(setting.socialUrl != null ? setting.socialUrl : "-");
                        binding.tvOtherUrl.setText(setting.otherUrl != null ? setting.otherUrl : "-");
                    }
                } else {
                    binding.tvContactEmail.setText("Belum diset");
                    binding.tvContactPhone.setText("Belum diset");
                    binding.tvSocialUrl.setText("Belum diset");
                    binding.tvOtherUrl.setText("Belum diset");
                }
            })
            .addOnFailureListener(e ->
                Toast.makeText(this, "Gagal memuat setelan", Toast.LENGTH_SHORT).show());
    }

    private void openUrl(String url) {
        if (url == null || url.isEmpty() || url.equals("-") || url.equals("Belum diset")) {
            Toast.makeText(this, "URL tidak tersedia", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!url.startsWith("http")) url = "https://" + url;
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            Toast.makeText(this, "Tidak bisa membuka URL", Toast.LENGTH_SHORT).show();
        }
    }
}
