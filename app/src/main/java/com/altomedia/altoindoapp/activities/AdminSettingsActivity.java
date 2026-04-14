package com.altomedia.altoindoapp.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.altomedia.altoindoapp.R;
import com.altomedia.altoindoapp.models.AppSetting;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminSettingsActivity extends AppCompatActivity {
    private EditText etContactEmail, etContactPhone, etSocialUrl, etOtherUrl;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_settings);

        if (!AdminGuard.isAdmin(this)) {
            finish();
            return;
        }

        etContactEmail = findViewById(R.id.et_contact_email);
        etContactPhone = findViewById(R.id.et_contact_phone);
        etSocialUrl = findViewById(R.id.et_social_url);
        etOtherUrl = findViewById(R.id.et_other_url);
        Button btnSave = findViewById(R.id.btn_save_settings);
        db = FirebaseFirestore.getInstance();

        btnSave.setOnClickListener(v -> saveSettings());
        loadSettings();
    }

    private void loadSettings() {
        db.collection("app_settings").document("default").get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                AppSetting setting = documentSnapshot.toObject(AppSetting.class);
                if (setting != null) {
                    etContactEmail.setText(setting.contactEmail);
                    etContactPhone.setText(setting.contactPhone);
                    etSocialUrl.setText(setting.socialUrl);
                    etOtherUrl.setText(setting.otherUrl);
                }
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "Gagal memuat setelan", Toast.LENGTH_SHORT).show());
    }

    private void saveSettings() {
        AppSetting setting = new AppSetting(
            "default",
            etContactEmail.getText().toString().trim(),
            etContactPhone.getText().toString().trim(),
            etSocialUrl.getText().toString().trim(),
            etOtherUrl.getText().toString().trim()
        );

        db.collection("app_settings").document("default").set(setting).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Setelan tersimpan", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> Toast.makeText(this, "Gagal menyimpan setelan", Toast.LENGTH_SHORT).show());
    }
}
