package com.altomedia.altoindoapp.activities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.altomedia.altoindoapp.databinding.ActivityWithdrawBinding;
import com.altomedia.altoindoapp.models.Transaction;
import com.altomedia.altoindoapp.models.User;
import com.altomedia.altoindoapp.models.WithdrawRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WithdrawActivity extends AppCompatActivity {
    private ActivityWithdrawBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private long currentBalance = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWithdrawBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loadUserData();

        binding.btnSubmit.setOnClickListener(v -> submitWithdrawRequest());
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void loadUserData() {
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                User user = documentSnapshot.toObject(User.class);
                if (user == null) return;
                if (!user.isProfileComplete()) {
                    Toast.makeText(this, "Harap lengkapi profil dan rekening bank sebelum melakukan penarikan", Toast.LENGTH_LONG).show();
                }
                currentBalance = user.balance_wallet;
                NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                binding.tvBalance.setText("Saldo Tersedia: " + nf.format(currentBalance));

                if (user.account_name != null && !user.account_name.isEmpty()) {
                    binding.etBankName.setText(user.account_name);
                }
                if (user.account_number != null && !user.account_number.isEmpty()) {
                    binding.etAccountNumber.setText(user.account_number);
                }
                if (user.account_owner != null && !user.account_owner.isEmpty()) {
                    binding.etAccountHolder.setText(user.account_owner);
                }
            }
        }).addOnFailureListener(e ->
            Toast.makeText(this, "Gagal memuat data", Toast.LENGTH_SHORT).show());
    }

    private void submitWithdrawRequest() {
        String amountStr = binding.etAmount.getText().toString().trim();
        String bankName = binding.etBankName.getText().toString().trim();
        String accountNumber = binding.etAccountNumber.getText().toString().trim();
        String accountHolder = binding.etAccountHolder.getText().toString().trim();

        if (amountStr.isEmpty() || bankName.isEmpty() || accountNumber.isEmpty() || accountHolder.isEmpty()) {
            Toast.makeText(this, "Semua field wajib diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        long amount;
        try {
            amount = Long.parseLong(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Nominal tidak valid", Toast.LENGTH_SHORT).show();
            return;
        }

        if (amount <= 0) {
            Toast.makeText(this, "Jumlah penarikan harus lebih dari 0", Toast.LENGTH_SHORT).show();
            return;
        }

        if (amount < 100000) {
            Toast.makeText(this, "Minimum penarikan adalah Rp.100.000", Toast.LENGTH_SHORT).show();
            return;
        }

        if (amount > currentBalance) {
            Toast.makeText(this, "Saldo tidak mencukupi", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnSubmit.setEnabled(false);
        String uid = mAuth.getCurrentUser().getUid();
        String id = db.collection("withdraw_requests").document().getId();
        String createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        WithdrawRequest request = new WithdrawRequest(id, uid, amount, bankName, accountNumber, accountHolder, "pending", createdAt);

        db.collection("withdraw_requests").document(id).set(request)
            .addOnSuccessListener(aVoid -> {
                db.collection("users").document(uid)
                    .update("balance_wallet", FieldValue.increment(-amount))
                    .addOnSuccessListener(v -> {
                        String trxId = db.collection("transactions").document().getId();
                        Transaction trx = new Transaction(trxId, uid, "withdraw", amount,
                            "", "", "pending", createdAt, "Penarikan ke " + bankName);
                        db.collection("transactions").document(trxId).set(trx);
                        Toast.makeText(this, "Permintaan penarikan berhasil diajukan", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Gagal memotong saldo", Toast.LENGTH_SHORT).show();
                        binding.btnSubmit.setEnabled(true);
                    });
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Gagal mengajukan permintaan", Toast.LENGTH_SHORT).show();
                binding.btnSubmit.setEnabled(true);
            });
    }
}
