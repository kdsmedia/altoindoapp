package com.altomedia.altoindoapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.altomedia.altoindoapp.adapters.TransactionAdapter;
import com.altomedia.altoindoapp.databinding.ActivityTransactionHistoryBinding;
import com.altomedia.altoindoapp.models.Transaction;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class TransactionHistoryActivity extends AppCompatActivity {
    private ActivityTransactionHistoryBinding binding;
    private FirebaseFirestore db;
    private TransactionAdapter adapter;
    private List<Transaction> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTransactionHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TransactionAdapter(list);
        binding.rvTransactions.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        loadTransactions();
    }

    private void loadTransactions() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("transactions")
            .whereEqualTo("uid", uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                list.clear();
                for (QueryDocumentSnapshot doc : querySnapshot) {
                    Transaction trx = doc.toObject(Transaction.class);
                    list.add(trx);
                }
                adapter.notifyDataSetChanged();
                if (list.isEmpty()) {
                    binding.tvEmpty.setVisibility(View.VISIBLE);
                    binding.rvTransactions.setVisibility(View.GONE);
                } else {
                    binding.tvEmpty.setVisibility(View.GONE);
                    binding.rvTransactions.setVisibility(View.VISIBLE);
                }
            })
            .addOnFailureListener(e ->
                Toast.makeText(this, "Gagal memuat riwayat", Toast.LENGTH_SHORT).show());
    }
}
