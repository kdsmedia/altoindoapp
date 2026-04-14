package com.altomedia.altoindoapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.altomedia.altoindoapp.databinding.ActivityRegisterBinding;
import com.altomedia.altoindoapp.models.User;
import com.altomedia.altoindoapp.utils.IDGenerator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"DANA", "OVO", "GOPAY"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spEwallet.setAdapter(adapter);

        binding.rbEwallet.setChecked(true);
        binding.ewalletSection.setVisibility(View.VISIBLE);
        binding.bankSection.setVisibility(View.GONE);

        binding.rgAccountType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == binding.rbEwallet.getId()) {
                binding.ewalletSection.setVisibility(View.VISIBLE);
                binding.bankSection.setVisibility(View.GONE);
            } else {
                binding.ewalletSection.setVisibility(View.GONE);
                binding.bankSection.setVisibility(View.VISIBLE);
            }
        });

        binding.btnRegister.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            String pass = binding.etPassword.getText().toString().trim();
            String name = binding.etFullName.getText().toString().trim();
            String phone = binding.etPhone.getText().toString().trim();
            String upline = binding.etUplineId.getText().toString().trim();

            if (email.isEmpty() || pass.isEmpty() || name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Lengkapi data!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (email.equalsIgnoreCase(AdminGuard.ADMIN_EMAIL)) {
                Toast.makeText(this, "Email admin tidak bisa didaftarkan di sini", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean isEwallet = binding.rgAccountType.getCheckedRadioButtonId() == binding.rbEwallet.getId();
            if (isEwallet) {
                if (binding.etEwalletOwner.getText().toString().trim().isEmpty() || binding.etEwalletNumber.getText().toString().trim().isEmpty()) {
                    Toast.makeText(this, "Lengkapi data ewallet", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                if (binding.etBankName.getText().toString().trim().isEmpty() || binding.etBankOwner.getText().toString().trim().isEmpty() || binding.etBankAccount.getText().toString().trim().isEmpty()) {
                    Toast.makeText(this, "Lengkapi data bank", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            mAuth.createUserWithEmailAndPassword(email, pass).addOnSuccessListener(authResult -> {
                String uid = authResult.getUser().getUid();
                String memberId = IDGenerator.generateMemberID();

                String accountType = isEwallet ? "ewallet" : "bank";
                String accountName = isEwallet ? binding.spEwallet.getSelectedItem().toString() : binding.etBankName.getText().toString().trim();
                String accountOwner = isEwallet ? binding.etEwalletOwner.getText().toString().trim() : binding.etBankOwner.getText().toString().trim();
                String accountNumber = isEwallet ? binding.etEwalletNumber.getText().toString().trim() : binding.etBankAccount.getText().toString().trim();

                User newUser = new User(uid, memberId, email, name, upline.isEmpty() ? "ROOT" : upline, phone, accountType, accountName, accountOwner, accountNumber);
                db.collection("users").document(uid).set(newUser)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Registrasi Berhasil! ID Anda: " + memberId, Toast.LENGTH_LONG).show();
                        finish();
                    }).addOnFailureListener(e -> Toast.makeText(this, "Gagal menyimpan data pengguna", Toast.LENGTH_SHORT).show());
            }).addOnFailureListener(e -> Toast.makeText(this, "Gagal registrasi", Toast.LENGTH_SHORT).show());
        });

        binding.tvGoToLogin.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
    }
}
