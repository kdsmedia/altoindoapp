package com.altomedia.altoindoapp.activities;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import com.altomedia.altoindoapp.R;
import com.altomedia.altoindoapp.models.IdCardRequest;
import com.altomedia.altoindoapp.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import net.glxn.qrgen.android.QRCode;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class IdCardActivity extends AppCompatActivity {
    private static final long PRINT_COST = 50000;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private User currentUser;
    private CardView cardIdCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_id_card);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        cardIdCard = findViewById(R.id.cardIdCard);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnRequestPrint).setOnClickListener(v -> confirmRequestPrint());
        findViewById(R.id.btnShareCard).setOnClickListener(v -> saveCardAsImage());

        loadUserData();
    }

    private void loadUserData() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                currentUser = doc.toObject(User.class);
                if (currentUser != null) {
                    populateCard(currentUser);
                    checkExistingRequest(uid);
                }
            }
        });
    }

    private void populateCard(User user) {
        ((TextView) findViewById(R.id.tvCardMemberId)).setText(
            user.member_id != null ? user.member_id : "-");
        ((TextView) findViewById(R.id.tvCardName)).setText(
            user.full_name != null ? user.full_name.toUpperCase() : "-");
        ((TextView) findViewById(R.id.tvCardTier)).setText(
            (user.tier != null ? user.tier : "BRONZE").toUpperCase());

        if (user.member_id != null && !user.member_id.isEmpty()) {
            Bitmap qr = QRCode.from(user.member_id).withSize(256, 256).bitmap();
            ((ImageView) findViewById(R.id.ivCardQr)).setImageBitmap(qr);
        }

        if (!user.is_active) {
            ((TextView) findViewById(R.id.tvCardStatus)).setText("● TIDAK AKTIF");
            ((TextView) findViewById(R.id.tvCardStatus)).setTextColor(0xFFFF5252);
        }
    }

    private void checkExistingRequest(String uid) {
        db.collection("id_card_requests")
            .whereEqualTo("uid", uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                if (!querySnapshot.isEmpty()) {
                    QueryDocumentSnapshot doc = (QueryDocumentSnapshot) querySnapshot.getDocuments().get(0);
                    IdCardRequest request = doc.toObject(IdCardRequest.class);
                    if (request != null) {
                        CardView cardStatus = findViewById(R.id.cardPrintStatus);
                        TextView tvStatus = findViewById(R.id.tvPrintStatus);
                        cardStatus.setVisibility(View.VISIBLE);
                        switch (request.status) {
                            case "pending":
                                tvStatus.setText("Menunggu verifikasi admin");
                                tvStatus.setTextColor(0xFFFFB74D);
                                break;
                            case "processing":
                                tvStatus.setText("Sedang diproses");
                                tvStatus.setTextColor(0xFF7EC8E3);
                                break;
                            case "shipped":
                                tvStatus.setText("Dikirim ke alamat Anda");
                                tvStatus.setTextColor(0xFF00E676);
                                break;
                        }
                        if ("pending".equals(request.status) || "processing".equals(request.status)) {
                            findViewById(R.id.btnRequestPrint).setEnabled(false);
                            ((Button) findViewById(R.id.btnRequestPrint)).setText("Permintaan sudah dikirim");
                        }
                    }
                }
            });
    }

    private void confirmRequestPrint() {
        if (currentUser == null) return;
        if (!currentUser.isProfileComplete()) {
            Toast.makeText(this, "Lengkapi profil dan alamat terlebih dahulu", Toast.LENGTH_LONG).show();
            return;
        }
        if (currentUser.balance_wallet < PRINT_COST) {
            Toast.makeText(this, "Saldo tidak cukup. Diperlukan Rp 50.000", Toast.LENGTH_LONG).show();
            return;
        }
        if (currentUser.address == null || currentUser.address.isEmpty()) {
            Toast.makeText(this, "Alamat pengiriman belum diisi di profil", Toast.LENGTH_LONG).show();
            return;
        }

        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        new AlertDialog.Builder(this)
            .setTitle("Konfirmasi Cetak ID Card")
            .setMessage("Biaya cetak: " + nf.format(PRINT_COST) + "\n\nID Card fisik akan dikirim ke:\n" + currentUser.address + "\n\nLanjutkan?")
            .setPositiveButton("Ya, Cetak", (dialog, which) -> submitPrintRequest())
            .setNegativeButton("Batal", null)
            .show();
    }

    private void submitPrintRequest() {
        if (mAuth.getCurrentUser() == null || currentUser == null) return;
        String uid = mAuth.getCurrentUser().getUid();
        String id = db.collection("id_card_requests").document().getId();
        String createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        IdCardRequest request = new IdCardRequest(id, uid, currentUser.member_id,
            currentUser.full_name, currentUser.address, "pending", createdAt);

        db.collection("id_card_requests").document(id).set(request)
            .addOnSuccessListener(aVoid -> {
                db.collection("users").document(uid)
                    .update("balance_wallet", FieldValue.increment(-PRINT_COST))
                    .addOnSuccessListener(v -> {
                        Toast.makeText(this, "Permintaan cetak berhasil! ID Card akan dikirim ke alamat Anda.", Toast.LENGTH_LONG).show();
                        loadUserData();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Gagal memotong saldo", Toast.LENGTH_SHORT).show());
            })
            .addOnFailureListener(e -> Toast.makeText(this, "Gagal mengirim permintaan", Toast.LENGTH_SHORT).show());
    }

    private void saveCardAsImage() {
        try {
            cardIdCard.setDrawingCacheEnabled(true);
            cardIdCard.buildDrawingCache();
            Bitmap bmp = Bitmap.createBitmap(cardIdCard.getWidth(), cardIdCard.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bmp);
            cardIdCard.draw(canvas);
            cardIdCard.setDrawingCacheEnabled(false);

            String filename = "ALTOINDO_IDCARD_" + (currentUser != null ? currentUser.member_id : "member") + ".png";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ALTOINDO");
                Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (uri != null) {
                    OutputStream out = getContentResolver().openOutputStream(uri);
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
                    if (out != null) out.close();
                    Toast.makeText(this, "ID Card disimpan ke galeri: " + filename, Toast.LENGTH_LONG).show();
                }
            } else {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
                    return;
                }
                java.io.File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                java.io.File file = new java.io.File(dir, filename);
                java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
                bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();
                Toast.makeText(this, "ID Card disimpan: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Gagal menyimpan ID Card: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
