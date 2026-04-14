package com.altomedia.altoindoapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.altomedia.altoindoapp.R;
import com.altomedia.altoindoapp.adapters.NotificationAdapter;
import com.altomedia.altoindoapp.models.NotificationMessage;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private View tvEmpty;
    private FirebaseFirestore db;
    private NotificationAdapter adapter;
    private List<NotificationMessage> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        recyclerView = findViewById(R.id.rvNotifications);
        tvEmpty = findViewById(R.id.tvEmpty);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(list);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        loadNotifications();
    }

    private void loadNotifications() {
        db.collection("notifications")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                list.clear();
                for (QueryDocumentSnapshot doc : querySnapshot) {
                    NotificationMessage notification = doc.toObject(NotificationMessage.class);
                    list.add(notification);
                }
                adapter.notifyDataSetChanged();
                if (list.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            })
            .addOnFailureListener(e ->
                Toast.makeText(this, "Gagal memuat notifikasi", Toast.LENGTH_SHORT).show());
    }
}
