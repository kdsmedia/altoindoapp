package com.altomedia.altoindoapp.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.altomedia.altoindoapp.R;
import com.altomedia.altoindoapp.databinding.ActivityUserProfileBinding;
import com.altomedia.altoindoapp.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class UserProfileActivity extends AppCompatActivity {
    private ActivityUserProfileBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
            R.array.account_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerAccountType.setAdapter(adapter);

        loadUserData();

        binding.btnSave.setOnClickListener(v -> saveProfile());
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void loadUserData() {
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                User user = documentSnapshot.toObject(User.class);
                if (user == null) return;
                binding.tvMemberId.setText(user.member_id != null ? user.member_id : "");
                binding.tvEmail.setText(user.email != null ? user.email : "");
                binding.etFullName.setText(user.full_name != null ? user.full_name : "");
                binding.etPhone.setText(user.phone != null ? user.phone : "");
                binding.etAddress.setText(user.address != null ? user.address : "");
                binding.etDropshipName.setText(user.dropship_name != null ? user.dropship_name : "");
                binding.etAccountName.setText(user.account_name != null ? user.account_name : "");
                binding.etAccountOwner.setText(user.account_owner != null ? user.account_owner : "");
                binding.etAccountNumber.setText(user.account_number != null ? user.account_number : "");

                String[] accountTypes = getResources().getStringArray(R.array.account_types);
                for (int i = 0; i < accountTypes.length; i++) {
                    if (accountTypes[i].equalsIgnoreCase(user.account_type)) {
                        binding.spinnerAccountType.setSelection(i);
                        break;
                    }
                }
            }
        }).addOnFailureListener(e ->
            Toast.makeText(this, "Gagal memuat data profil", Toast.LENGTH_SHORT).show());
    }

    private void saveProfile() {
        String fullName = binding.etFullName.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        String address = binding.etAddress.getText().toString().trim();
        String dropshipName = binding.etDropshipName.getText().toString().trim();
        String accountType = binding.spinnerAccountType.getSelectedItem().toString();
        String accountName = binding.etAccountName.getText().toString().trim();
        String accountOwner = binding.etAccountOwner.getText().toString().trim();
        String accountNumber = binding.etAccountNumber.getText().toString().trim();

        if (fullName.isEmpty()) {
            Toast.makeText(this, "Nama lengkap wajib diisi", Toast.LENGTH_SHORT).show();
            return;
        }
        if (phone.isEmpty()) {
            Toast.makeText(this, "No. telepon wajib diisi", Toast.LENGTH_SHORT).show();
            return;
        }
        if (address.isEmpty()) {
            Toast.makeText(this, "Alamat lengkap wajib diisi untuk pengiriman", Toast.LENGTH_SHORT).show();
            return;
        }
        if (accountName.isEmpty() || accountOwner.isEmpty() || accountNumber.isEmpty()) {
            Toast.makeText(this, "Data rekening bank wajib dilengkapi", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnSave.setEnabled(false);
        String uid = mAuth.getCurrentUser().getUid();
        Map<String, Object> updates = new HashMap<>();
        updates.put("full_name", fullName);
        updates.put("phone", phone);
        updates.put("address", address);
        updates.put("dropship_name", dropshipName);
        updates.put("account_type", accountType);
        updates.put("account_name", accountName);
        updates.put("account_owner", accountOwner);
        updates.put("account_number", accountNumber);

        db.collection("users").document(uid).update(updates)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Gagal menyimpan profil", Toast.LENGTH_SHORT).show();
                binding.btnSave.setEnabled(true);
            });
    }
}
