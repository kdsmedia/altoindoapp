package com.altomedia.altoindoapp.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.altomedia.altoindoapp.R;
import com.altomedia.altoindoapp.models.TopupRequest;
import com.altomedia.altoindoapp.models.WithdrawRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class AdminTransactionManagementActivity extends AppCompatActivity {
    private LinearLayout topupListContainer;
    private LinearLayout withdrawListContainer;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_transactions);

        if (!AdminGuard.isAdmin(this)) {
            finish();
            return;
        }

        topupListContainer = findViewById(R.id.topup_list_container);
        withdrawListContainer = findViewById(R.id.withdraw_list_container);
        db = FirebaseFirestore.getInstance();

        loadTopups();
        loadWithdrawals();
    }

    private void loadTopups() {
        db.collection("topups").whereEqualTo("status", "pending").get().addOnSuccessListener(querySnapshot -> {
            topupListContainer.removeAllViews();
            for (QueryDocumentSnapshot doc : querySnapshot) {
                TopupRequest topup = doc.toObject(TopupRequest.class);
                LinearLayout item = createTopupItem(doc.getId(), topup);
                topupListContainer.addView(item);
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "Gagal memuat topup", Toast.LENGTH_SHORT).show());
    }

    private void loadWithdrawals() {
        db.collection("withdraw_requests").whereEqualTo("status", "pending").get().addOnSuccessListener(querySnapshot -> {
            withdrawListContainer.removeAllViews();
            for (QueryDocumentSnapshot doc : querySnapshot) {
                WithdrawRequest withdraw = doc.toObject(WithdrawRequest.class);
                LinearLayout item = createWithdrawItem(doc.getId(), withdraw);
                withdrawListContainer.addView(item);
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "Gagal memuat withdrawal", Toast.LENGTH_SHORT).show());
    }

    private LinearLayout createTopupItem(String docId, TopupRequest topup) {
        LinearLayout parent = new LinearLayout(this);
        parent.setOrientation(LinearLayout.VERTICAL);
        parent.setPadding(16, 16, 16, 16);
        parent.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);

        TextView title = new TextView(this);
        title.setText("Topup " + topup.trx_id + " - Rp " + topup.amount);
        title.setTextSize(16f);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        parent.addView(title);

        TextView body = new TextView(this);
        body.setText("UID: " + topup.uid + "\nNama: " + topup.sender_name + "\nPesan: " + topup.sender_message + "\nStatus: " + topup.status);
        parent.addView(body);

        LinearLayout controls = new LinearLayout(this);
        controls.setOrientation(LinearLayout.HORIZONTAL);

        Button btnAccept = new Button(this);
        btnAccept.setText("Terima");
        btnAccept.setOnClickListener(v -> updateTopupStatus(docId, topup.uid, topup.amount, "approved"));
        controls.addView(btnAccept);

        Button btnReject = new Button(this);
        btnReject.setText("Tolak");
        btnReject.setOnClickListener(v -> updateTopupStatus(docId, topup.uid, topup.amount, "rejected"));
        controls.addView(btnReject);

        parent.addView(controls);
        return parent;
    }

    private LinearLayout createWithdrawItem(String docId, WithdrawRequest withdraw) {
        LinearLayout parent = new LinearLayout(this);
        parent.setOrientation(LinearLayout.VERTICAL);
        parent.setPadding(16, 16, 16, 16);
        parent.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);

        TextView title = new TextView(this);
        title.setText("Withdraw " + withdraw.id + " - Rp " + withdraw.amount);
        title.setTextSize(16f);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        parent.addView(title);

        TextView body = new TextView(this);
        body.setText("UID: " + withdraw.uid + "\nBank: " + withdraw.bankName + "\nRek: " + withdraw.accountNumber + "\nNama: " + withdraw.accountHolder + "\nStatus: " + withdraw.status);
        parent.addView(body);

        LinearLayout controls = new LinearLayout(this);
        controls.setOrientation(LinearLayout.HORIZONTAL);

        Button btnAccept = new Button(this);
        btnAccept.setText("Terima");
        btnAccept.setOnClickListener(v -> updateWithdrawStatus(docId, withdraw.uid, withdraw.amount, "approved"));
        controls.addView(btnAccept);

        Button btnReject = new Button(this);
        btnReject.setText("Tolak");
        btnReject.setOnClickListener(v -> updateWithdrawStatus(docId, withdraw.uid, withdraw.amount, "rejected"));
        controls.addView(btnReject);

        parent.addView(controls);
        return parent;
    }

    private void updateTopupStatus(String docId, String uid, long amount, String status) {
        db.collection("topups").document(docId).update("status", status).addOnSuccessListener(aVoid -> {
            if ("approved".equals(status)) {
                db.collection("users").document(uid)
                    .update("balance_wallet", com.google.firebase.firestore.FieldValue.increment(amount));
                db.collection("transactions").whereEqualTo("id", docId).limit(1).get()
                    .addOnSuccessListener(snap -> {
                        if (!snap.isEmpty()) {
                            snap.getDocuments().get(0).getReference().update("status", "success");
                        }
                    });
            } else if ("rejected".equals(status)) {
                db.collection("transactions").whereEqualTo("id", docId).limit(1).get()
                    .addOnSuccessListener(snap -> {
                        if (!snap.isEmpty()) {
                            snap.getDocuments().get(0).getReference().update("status", "rejected");
                        }
                    });
            }
            Toast.makeText(this, "Topup " + status, Toast.LENGTH_SHORT).show();
            loadTopups();
        }).addOnFailureListener(e -> Toast.makeText(this, "Gagal memperbarui status topup", Toast.LENGTH_SHORT).show());
    }

    private void updateWithdrawStatus(String docId, String uid, long amount, String status) {
        db.collection("withdraw_requests").document(docId).update("status", status).addOnSuccessListener(aVoid -> {
            if ("rejected".equals(status)) {
                db.collection("users").document(uid)
                    .update("balance_wallet", com.google.firebase.firestore.FieldValue.increment(amount));
            }
            Toast.makeText(this, "Withdraw " + status, Toast.LENGTH_SHORT).show();
            loadWithdrawals();
        }).addOnFailureListener(e -> Toast.makeText(this, "Gagal memperbarui status withdraw", Toast.LENGTH_SHORT).show());
    }
}
