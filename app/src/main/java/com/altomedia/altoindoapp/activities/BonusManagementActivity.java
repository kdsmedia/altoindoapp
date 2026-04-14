package com.altomedia.altoindoapp.activities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.altomedia.altoindoapp.databinding.ActivityBonusManagementBinding;
import com.altomedia.altoindoapp.models.BonusConfig;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class BonusManagementActivity extends AppCompatActivity {
    private ActivityBonusManagementBinding binding;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBonusManagementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (!AdminGuard.isAdmin(this)) {
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();

        binding.btnSaveBonus.setOnClickListener(v -> saveBonus());
        binding.btnDistributeBonus.setOnClickListener(v -> distributeAffiliateBonus());
        loadBonus();
    }

    private void loadBonus() {
        db.collection("bonus_config").document("default").get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                BonusConfig config = documentSnapshot.toObject(BonusConfig.class);
                if (config != null) {
                    binding.etInviteBonus.setText(String.valueOf(config.affiliateBonusPercent));
                    binding.etCheckinBonus.setText(String.valueOf(config.checkinBonusPercent));
                    binding.etSponsorBonus.setText(String.valueOf(config.sponsorBonusPercent));
                    binding.etVideoBonus.setText(String.valueOf(config.videoBonusPercent));
                }
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "Gagal memuat bonus", Toast.LENGTH_SHORT).show());
    }

    private void saveBonus() {
        BonusConfig config = new BonusConfig(
            "default",
            parseDouble(binding.etInviteBonus.getText().toString()),
            parseDouble(binding.etCheckinBonus.getText().toString()),
            parseDouble(binding.etSponsorBonus.getText().toString()),
            parseDouble(binding.etVideoBonus.getText().toString())
        );

        db.collection("bonus_config").document("default").set(config)
            .addOnSuccessListener(aVoid ->
                Toast.makeText(this, "Konfigurasi bonus tersimpan", Toast.LENGTH_SHORT).show())
            .addOnFailureListener(e ->
                Toast.makeText(this, "Gagal menyimpan bonus", Toast.LENGTH_SHORT).show());
    }

    private void distributeAffiliateBonus() {
        db.collection("bonus_config").document("default").get()
            .addOnSuccessListener(configDoc -> {
                if (!configDoc.exists()) {
                    Toast.makeText(this, "Konfigurasi bonus belum diset", Toast.LENGTH_SHORT).show();
                    return;
                }
                BonusConfig config = configDoc.toObject(BonusConfig.class);
                if (config == null) return;

                double affiliatePercent = config.affiliateBonusPercent / 100.0;

                db.collection("users").whereNotEqualTo("upline_id", "ROOT").get()
                    .addOnSuccessListener(usersSnapshot -> {
                        int[] distributed = {0};
                        for (QueryDocumentSnapshot userDoc : usersSnapshot) {
                            String uplineId = userDoc.getString("upline_id");
                            Long wallet = userDoc.getLong("balance_wallet");
                            if (uplineId == null || uplineId.isEmpty() || wallet == null || wallet == 0) continue;

                            long bonusAmount = (long)(wallet * affiliatePercent);
                            if (bonusAmount <= 0) continue;

                            db.collection("users").whereEqualTo("member_id", uplineId).limit(1).get()
                                .addOnSuccessListener(uplineSnapshot -> {
                                    if (!uplineSnapshot.isEmpty()) {
                                        String uplineDocId = uplineSnapshot.getDocuments().get(0).getId();
                                        db.collection("users").document(uplineDocId)
                                            .update("balance_wallet",
                                                com.google.firebase.firestore.FieldValue.increment(bonusAmount));
                                    }
                                });
                            distributed[0]++;
                        }
                        Toast.makeText(this, "Bonus afiliasi didistribusikan ke " + distributed[0] + " upline", Toast.LENGTH_LONG).show();
                    })
                    .addOnFailureListener(e ->
                        Toast.makeText(this, "Gagal mendistribusikan bonus", Toast.LENGTH_SHORT).show());
            });
    }

    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value.trim());
        } catch (Exception e) {
            return 0;
        }
    }
}
