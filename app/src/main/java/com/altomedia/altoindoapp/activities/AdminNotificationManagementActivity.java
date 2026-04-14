package com.altomedia.altoindoapp.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.altomedia.altoindoapp.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AdminNotificationManagementActivity extends AppCompatActivity {
    private LinearLayout notificationListContainer;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_notifications);

        if (!AdminGuard.isAdmin(this)) {
            finish();
            return;
        }

        notificationListContainer = findViewById(R.id.notification_list_container);
        Button btnAdd = findViewById(R.id.btn_add_notification);
        db = FirebaseFirestore.getInstance();

        btnAdd.setOnClickListener(v -> showAddNotificationDialog());
        loadNotifications();
    }

    private void loadNotifications() {
        db.collection("notifications").get().addOnSuccessListener(querySnapshot -> {
            notificationListContainer.removeAllViews();
            for (QueryDocumentSnapshot doc : querySnapshot) {
                LinearLayout item = createNotificationItem(doc.getId(), doc.getString("title"), doc.getString("body"));
                notificationListContainer.addView(item);
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "Gagal memuat notifikasi", Toast.LENGTH_SHORT).show());
    }

    private LinearLayout createNotificationItem(String id, String title, String body) {
        LinearLayout parent = new LinearLayout(this);
        parent.setOrientation(LinearLayout.VERTICAL);
        parent.setPadding(16, 16, 16, 16);
        parent.setBackgroundResource(android.R.drawable.editbox_background_normal);

        TextView tvTitle = new TextView(this);
        tvTitle.setText(title);
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        parent.addView(tvTitle);

        TextView tvBody = new TextView(this);
        tvBody.setText(body);
        parent.addView(tvBody);

        Button btnDelete = new Button(this);
        btnDelete.setText("Hapus");
        btnDelete.setOnClickListener(v -> db.collection("notifications").document(id).delete().addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Notifikasi dihapus", Toast.LENGTH_SHORT).show();
            loadNotifications();
        }).addOnFailureListener(e -> Toast.makeText(this, "Gagal menghapus notifikasi", Toast.LENGTH_SHORT).show()));
        parent.addView(btnDelete);
        return parent;
    }

    private void showAddNotificationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Buat Notifikasi Baru");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(24, 24, 24, 24);

        TextView titleHint = new TextView(this);
        titleHint.setText("Judul");
        layout.addView(titleHint);
        final EditText etTitle = new EditText(this);
        layout.addView(etTitle);

        TextView bodyHint = new TextView(this);
        bodyHint.setText("Isi Pesan");
        layout.addView(bodyHint);
        final EditText etBody = new EditText(this);
        layout.addView(etBody);

        builder.setView(layout);
        builder.setPositiveButton("Simpan", (dialog, which) -> {
            String title = etTitle.getText().toString().trim();
            String body = etBody.getText().toString().trim();
            if (title.isEmpty() || body.isEmpty()) {
                Toast.makeText(this, "Judul dan isi harus diisi", Toast.LENGTH_SHORT).show();
                return;
            }
            String id = String.valueOf(System.currentTimeMillis());
            String createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            db.collection("notifications").document(id)
                .set(new java.util.HashMap<String, Object>() {{
                    put("id", id);
                    put("title", title);
                    put("body", body);
                    put("createdAt", createdAt);
                }})
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Notifikasi tersimpan", Toast.LENGTH_SHORT).show();
                    loadNotifications();
                }).addOnFailureListener(e -> Toast.makeText(this, "Gagal menyimpan notifikasi", Toast.LENGTH_SHORT).show());
        });
        builder.setNegativeButton("Batal", null);
        builder.show();
    }
}
