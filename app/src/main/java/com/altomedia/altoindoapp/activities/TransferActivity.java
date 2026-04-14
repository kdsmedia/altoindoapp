package com.altomedia.altoindoapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.altomedia.altoindoapp.databinding.ActivityTransferBinding;
import com.altomedia.altoindoapp.models.Transaction;
import com.altomedia.altoindoapp.utils.FirebaseHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TransferActivity extends AppCompatActivity {
    private ActivityTransferBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private long currentBalance = 0;
    private String currentMemberId = "";

    private final ActivityResultLauncher<Intent> scanLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                String scannedId = result.getData().getStringExtra("SCANNED_ID");
                if (scannedId != null) {
                    binding.etTargetId.setText(scannedId);
                }
            }
        }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTransferBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        loadSenderInfo();

        binding.btnScanQr.setOnClickListener(v ->
            scanLauncher.launch(new Intent(this, ScannerActivity.class)));

        binding.btnSendNow.setOnClickListener(v -> validateAndTransfer());
    }

    private void loadSenderInfo() {
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    currentBalance = doc.getLong("balance_wallet") != null
                        ? doc.getLong("balance_wallet") : 0;
                    currentMemberId = doc.getString("member_id") != null
                        ? doc.getString("member_id") : "";
                    NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                    binding.tvSenderBalance.setText("Saldo Anda: " + nf.format(currentBalance));
                }
            });
    }

    private void validateAndTransfer() {
        String targetId = binding.etTargetId.getText().toString().trim();
        String amountStr = binding.etAmount.getText().toString().trim();
        String pin = binding.etPin.getText().toString().trim();

        if (targetId.isEmpty() || amountStr.isEmpty() || pin.isEmpty()) {
            Toast.makeText(this, "Mohon lengkapi semua data", Toast.LENGTH_SHORT).show();
            return;
        }

        if (targetId.equals(currentMemberId)) {
            Toast.makeText(this, "Tidak bisa transfer ke diri sendiri", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "Nominal harus lebih dari 0", Toast.LENGTH_SHORT).show();
            return;
        }

        if (amount > currentBalance) {
            Toast.makeText(this, "Saldo tidak mencukupi", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).get()
            .addOnSuccessListener(senderDoc -> {
                String savedPin = senderDoc.getString("security_pin");
                if (savedPin == null) savedPin = "123456";
                if (!pin.equals(savedPin)) {
                    Toast.makeText(this, "PIN salah", Toast.LENGTH_SHORT).show();
                    return;
                }
                findReceiverAndTransfer(uid, targetId, amount);
            });
    }

    private void findReceiverAndTransfer(String senderUid, String targetId, long amount) {
        binding.btnSendNow.setEnabled(false);
        db.collection("users").whereEqualTo("member_id", targetId).get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String receiverUid = document.getId();
                        String receiverMemberId = document.getString("member_id");

                        FirebaseHelper.transferBalance(senderUid, receiverUid, amount, new FirebaseHelper.TransactionCallback() {
                            @Override
                            public void onSuccess() {
                                saveTransactionHistory(senderUid, amount, receiverMemberId);
                                Toast.makeText(TransferActivity.this, "Transfer Berhasil!", Toast.LENGTH_SHORT).show();
                                finish();
                            }

                            @Override
                            public void onFailure(String error) {
                                Toast.makeText(TransferActivity.this, "Gagal: " + error, Toast.LENGTH_SHORT).show();
                                binding.btnSendNow.setEnabled(true);
                            }
                        });
                    }
                } else {
                    Toast.makeText(this, "ID Tujuan tidak ditemukan", Toast.LENGTH_SHORT).show();
                    binding.btnSendNow.setEnabled(true);
                }
            });
    }

    private void saveTransactionHistory(String uid, long amount, String toMemberId) {
        String trxId = db.collection("transactions").document().getId();
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        Transaction trx = new Transaction(trxId, uid, "transfer", amount,
            currentMemberId, toMemberId, "success", now, "Transfer ke " + toMemberId);
        db.collection("transactions").document(trxId).set(trx);
    }
}
