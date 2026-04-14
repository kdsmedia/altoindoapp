package com.altomedia.altoindoapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.altomedia.altoindoapp.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() != null) {
            routeUser(mAuth.getCurrentUser().getEmail());
            return;
        }

        binding.btnLogin.setOnClickListener(v -> {
            String input = binding.etEmail.getText().toString().trim();
            String pass = binding.etPassword.getText().toString().trim();

            if (input.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Email/ID dan Password wajib diisi", Toast.LENGTH_SHORT).show();
                return;
            }

            binding.btnLogin.setEnabled(false);

            if (input.equalsIgnoreCase(AdminGuard.ADMIN_MEMBER_ID) || input.equalsIgnoreCase(AdminGuard.ADMIN_EMAIL)) {
                signIn(AdminGuard.ADMIN_EMAIL, pass);
            } else if (input.contains("@")) {
                signIn(input, pass);
            } else {
                resolveByMemberId(input, pass);
            }
        });

        binding.tvRegisterLink.setOnClickListener(v ->
            startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void resolveByMemberId(String memberId, String pass) {
        db.collection("users").whereEqualTo("member_id", memberId).limit(1).get()
            .addOnSuccessListener(querySnapshot -> {
                if (querySnapshot.isEmpty()) {
                    Toast.makeText(this, "ID Member tidak ditemukan", Toast.LENGTH_SHORT).show();
                    binding.btnLogin.setEnabled(true);
                    return;
                }
                String email = querySnapshot.getDocuments().get(0).getString("email");
                signIn(email, pass);
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Gagal mencari pengguna", Toast.LENGTH_SHORT).show();
                binding.btnLogin.setEnabled(true);
            });
    }

    private void signIn(String email, String pass) {
        mAuth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener(authResult -> {
                String userEmail = authResult.getUser().getEmail();
                routeUser(userEmail);
            })
            .addOnFailureListener(e -> {
                if (email.equalsIgnoreCase(AdminGuard.ADMIN_EMAIL) && pass.equals(AdminGuard.ADMIN_PASSWORD)) {
                    createAdminAndSignIn(pass);
                } else {
                    Toast.makeText(this, "Login gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    binding.btnLogin.setEnabled(true);
                }
            });
    }

    private void createAdminAndSignIn(String pass) {
        mAuth.createUserWithEmailAndPassword(AdminGuard.ADMIN_EMAIL, pass)
            .addOnSuccessListener(createResult -> {
                String uid = createResult.getUser().getUid();
                com.altomedia.altoindoapp.models.User adminUser = new com.altomedia.altoindoapp.models.User(
                    uid, AdminGuard.ADMIN_MEMBER_ID, AdminGuard.ADMIN_EMAIL, "Admin", "ROOT",
                    "", "bank", "BCA", "Admin", "0000000000");
                adminUser.role = "admin";
                adminUser.is_active = true;
                db.collection("users").document(uid).set(adminUser);
                startActivity(new Intent(this, AdminDashboardActivity.class));
                finish();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Gagal membuat akun admin", Toast.LENGTH_SHORT).show();
                binding.btnLogin.setEnabled(true);
            });
    }

    private void routeUser(String email) {
        if (AdminGuard.ADMIN_EMAIL.equalsIgnoreCase(email)) {
            startActivity(new Intent(this, AdminDashboardActivity.class));
        } else {
            startActivity(new Intent(this, MainActivity.class));
        }
        finish();
    }
}
