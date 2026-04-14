package com.altomedia.altoindoapp.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.altomedia.altoindoapp.R;
import com.altomedia.altoindoapp.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class AdminUserManagementActivity extends AppCompatActivity {
    private LinearLayout userListContainer;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_management);

        if (!AdminGuard.isAdmin(this)) {
            finish();
            return;
        }

        userListContainer = findViewById(R.id.user_list_container);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        loadUsers();
    }

    private void loadUsers() {
        db.collection("users").get().addOnSuccessListener(querySnapshot -> {
            userListContainer.removeAllViews();
            for (QueryDocumentSnapshot doc : querySnapshot) {
                User user = doc.toObject(User.class);
                LinearLayout item = createUserItem(doc.getId(), user);
                userListContainer.addView(item);
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "Gagal memuat pengguna", Toast.LENGTH_SHORT).show());
    }

    private LinearLayout createUserItem(String docId, User user) {
        LinearLayout parent = new LinearLayout(this);
        parent.setOrientation(LinearLayout.VERTICAL);
        parent.setPadding(16, 16, 16, 16);
        parent.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);

        TextView title = new TextView(this);
        title.setText(user.full_name + " (" + user.member_id + ")");
        title.setTextSize(16f);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        parent.addView(title);

        TextView details = new TextView(this);
        details.setText("Email: " + user.email + "\nSaldo: Rp " + user.balance_wallet + "\nRole: " + user.role + "\nStatus: " + (user.is_active ? "Aktif" : "Nonaktif"));
        parent.addView(details);

        LinearLayout controls = new LinearLayout(this);
        controls.setOrientation(LinearLayout.HORIZONTAL);
        controls.setGravity(Gravity.END);

        Button btnEdit = new Button(this);
        btnEdit.setText("Edit");
        btnEdit.setOnClickListener(v -> showEditDialog(docId, user));
        controls.addView(btnEdit);

        Button btnDelete = new Button(this);
        btnDelete.setText("Hapus");
        btnDelete.setOnClickListener(v -> deleteUser(docId));
        controls.addView(btnDelete);

        parent.addView(controls);
        return parent;
    }

    private void showEditDialog(String docId, User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Pengguna");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(24, 24, 24, 24);

        EditText etName = new EditText(this);
        etName.setHint("Nama Lengkap");
        etName.setText(user.full_name);
        layout.addView(etName);

        EditText etEmail = new EditText(this);
        etEmail.setHint("Email");
        etEmail.setText(user.email);
        layout.addView(etEmail);

        EditText etBalance = new EditText(this);
        etBalance.setHint("Saldo Wallet");
        etBalance.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etBalance.setText(String.valueOf(user.balance_wallet));
        layout.addView(etBalance);

        EditText etActive = new EditText(this);
        etActive.setHint("Aktif=true/nonaktif=false");
        etActive.setText(String.valueOf(user.is_active));
        layout.addView(etActive);

        builder.setView(layout);
        builder.setPositiveButton("Simpan", (dialog, which) -> {
            String newName = etName.getText().toString().trim();
            String newEmail = etEmail.getText().toString().trim();
            long newBalance;
            try {
                newBalance = Long.parseLong(etBalance.getText().toString().trim());
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Saldo tidak valid", Toast.LENGTH_SHORT).show();
                return;
            }
            boolean active = Boolean.parseBoolean(etActive.getText().toString().trim());

            db.collection("users").document(docId).update(
                "full_name", newName,
                "email", newEmail,
                "balance_wallet", newBalance,
                "is_active", active
            ).addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Data pengguna diperbarui", Toast.LENGTH_SHORT).show();
                loadUsers();
            }).addOnFailureListener(e -> Toast.makeText(this, "Gagal memperbarui", Toast.LENGTH_SHORT).show());
        });
        builder.setNegativeButton("Batal", null);
        builder.show();
    }

    private void deleteUser(String docId) {
        db.collection("users").document(docId).delete().addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Pengguna dihapus", Toast.LENGTH_SHORT).show();
            loadUsers();
        }).addOnFailureListener(e -> Toast.makeText(this, "Gagal menghapus pengguna", Toast.LENGTH_SHORT).show());
    }
}
