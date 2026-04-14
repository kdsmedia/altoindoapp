package com.altomedia.altoindoapp.activities;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.altomedia.altoindoapp.databinding.ActivityTopupBinding;
import com.altomedia.altoindoapp.models.Transaction;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import android.graphics.Color;
import android.view.View;

public class TopUpActivity extends AppCompatActivity {
    private static final String QRIS_BASE = "00020101021126610014COM.GO-JEK.WWW01189360091439663050810210G9663050810303UMI51440014ID.CO.QRIS.WWW0215ID10254671365660303UMI5204549953033605802ID5917ALTOMEDIA, Grosir6008KARAWANG61054136162070703A016304D21A";

    private ActivityTopupBinding binding;
    private CountDownTimer countDownTimer;
    private Bitmap currentQrBitmap;
    private String currentTrxId;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTopupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        binding.btnNominal10k.setOnClickListener(v -> setNominal(10000));
        binding.btnNominal50k.setOnClickListener(v -> setNominal(50000));
        binding.btnNominal100k.setOnClickListener(v -> setNominal(100000));
        binding.btnGenerate.setOnClickListener(v -> generateQr());
        binding.btnSaveQr.setOnClickListener(v -> saveQrImage());
    }

    private void setNominal(int value) {
        binding.etAmount.setText(String.valueOf(value));
    }

    private void generateQr() {
        String amountText = binding.etAmount.getText().toString().trim();
        String name = binding.etSenderName.getText().toString().trim();
        String message = binding.etSenderMessage.getText().toString().trim();

        if (amountText.isEmpty()) {
            Toast.makeText(this, "Masukkan nominal topup", Toast.LENGTH_SHORT).show();
            return;
        }

        long amount;
        try {
            amount = Long.parseLong(amountText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Nominal tidak valid", Toast.LENGTH_SHORT).show();
            return;
        }

        if (amount <= 0) {
            Toast.makeText(this, "Nominal harus lebih dari 0", Toast.LENGTH_SHORT).show();
            return;
        }

        if (countDownTimer != null) countDownTimer.cancel();

        currentTrxId = "TRX-" + System.currentTimeMillis();

        String qrContent = QRIS_BASE + amount;
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(qrContent, BarcodeFormat.QR_CODE, 512, 512);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            currentQrBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    currentQrBitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            binding.ivQr.setImageBitmap(currentQrBitmap);
            binding.tvTotalDisplay.setText("Rp " + amount);
            binding.tvTrxId.setText("TRX-ID: " + currentTrxId);
            binding.qrContainer.setVisibility(View.VISIBLE);

            startTimer();
            saveTopupRequest(amount, name, message);

        } catch (WriterException e) {
            Toast.makeText(this, "Gagal membuat QR", Toast.LENGTH_SHORT).show();
        }
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(3 * 60 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = millisUntilFinished / 60000;
                long seconds = (millisUntilFinished % 60000) / 1000;
                binding.tvPayTimer.setText(String.format(Locale.getDefault(),
                    "Selesaikan dalam %02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                binding.tvPayTimer.setText("Waktu habis. Buat QR baru.");
                binding.qrContainer.setVisibility(View.GONE);
            }
        }.start();
    }

    private void saveTopupRequest(long amount, String name, String message) {
        if (mAuth.getCurrentUser() == null) return;

        Map<String, Object> topup = new HashMap<>();
        topup.put("uid", mAuth.getCurrentUser().getUid());
        topup.put("amount", amount);
        topup.put("status", "pending");
        topup.put("created_at", com.google.firebase.firestore.FieldValue.serverTimestamp());
        topup.put("trx_id", currentTrxId);
        topup.put("sender_name", name.isEmpty() ? "Supporter" : name);
        topup.put("sender_message", message);

        db.collection("topups").document(currentTrxId).set(topup)
            .addOnSuccessListener(aVoid -> {
                String uid = mAuth.getCurrentUser().getUid();
                String createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                Transaction trx = new Transaction(currentTrxId, uid, "topup", amount,
                    "", "", "pending", createdAt, "Top Up via QRIS");
                db.collection("transactions").document(currentTrxId).set(trx);
            });
    }

    private void saveQrImage() {
        if (currentQrBitmap == null) {
            Toast.makeText(this, "QR belum dibuat", Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "QRIS-ALTOMEDIA-" + System.currentTimeMillis() + ".png");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ALTOINDO");
        }

        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (uri == null) {
            Toast.makeText(this, "Gagal menyimpan QR", Toast.LENGTH_SHORT).show();
            return;
        }

        try (OutputStream out = getContentResolver().openOutputStream(uri)) {
            if (out == null) throw new IOException("OutputStream not available");
            currentQrBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            Toast.makeText(this, "QR code disimpan ke galeri.", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Gagal menyimpan QR", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}
