package com.altomedia.altoindoapp.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.altomedia.altoindoapp.R;
import com.altomedia.altoindoapp.models.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RankingActivity extends AppCompatActivity {
    private RecyclerView rvRanking;
    private Button btnTabSaldo, btnTabPv;
    private FirebaseFirestore db;
    private List<User> userList = new ArrayList<>();
    private RankAdapter adapter;
    private boolean showSaldo = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

        db = FirebaseFirestore.getInstance();
        rvRanking = findViewById(R.id.rvRanking);
        btnTabSaldo = findViewById(R.id.btnTabSaldo);
        btnTabPv = findViewById(R.id.btnTabPv);

        rvRanking.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RankAdapter(userList, true);
        rvRanking.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnTabSaldo.setOnClickListener(v -> {
            showSaldo = true;
            btnTabSaldo.setBackgroundResource(R.drawable.btn_action_card);
            btnTabSaldo.setTextColor(0xFFFFFFFF);
            btnTabPv.setBackgroundResource(R.drawable.btn_action_card2);
            btnTabPv.setTextColor(0xFFAAAAAA);
            loadRanking();
        });

        btnTabPv.setOnClickListener(v -> {
            showSaldo = false;
            btnTabPv.setBackgroundResource(R.drawable.btn_action_card);
            btnTabPv.setTextColor(0xFFFFFFFF);
            btnTabSaldo.setBackgroundResource(R.drawable.btn_action_card2);
            btnTabSaldo.setTextColor(0xFFAAAAAA);
            loadRanking();
        });

        loadRanking();
    }

    private void loadRanking() {
        String orderField = showSaldo ? "balance_wallet" : "points_personal";
        db.collection("users")
            .whereEqualTo("role", "member")
            .orderBy(orderField, Query.Direction.DESCENDING)
            .limit(50)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                userList.clear();
                for (QueryDocumentSnapshot doc : querySnapshot) {
                    User u = doc.toObject(User.class);
                    userList.add(u);
                }
                adapter.setShowSaldo(showSaldo);
                adapter.notifyDataSetChanged();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Gagal memuat peringkat", Toast.LENGTH_SHORT).show();
            });
    }

    static class RankAdapter extends RecyclerView.Adapter<RankAdapter.VH> {
        private List<User> list;
        private boolean showSaldo;

        RankAdapter(List<User> list, boolean showSaldo) {
            this.list = list;
            this.showSaldo = showSaldo;
        }

        void setShowSaldo(boolean showSaldo) { this.showSaldo = showSaldo; }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ranking, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(VH h, int pos) {
            User u = list.get(pos);
            int rank = pos + 1;
            h.tvRank.setText(String.valueOf(rank));

            if (rank == 1) {
                h.tvRank.setTextColor(0xFFFFD700);
            } else if (rank == 2) {
                h.tvRank.setTextColor(0xFFC0C0C0);
            } else if (rank == 3) {
                h.tvRank.setTextColor(0xFFCD7F32);
            } else {
                h.tvRank.setTextColor(0xFFAAAAAA);
            }

            h.tvName.setText(u.full_name != null ? u.full_name : "-");
            h.tvId.setText("ID: " + (u.member_id != null ? u.member_id : "-"));
            h.tvTier.setText(u.tier != null ? u.tier.toUpperCase() : "BRONZE");

            NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            if (showSaldo) {
                h.tvValue.setText(nf.format(u.balance_wallet));
                h.tvLabel.setText("Saldo");
            } else {
                h.tvValue.setText(u.points_personal + " PV");
                h.tvLabel.setText("PV Personal");
            }
        }

        @Override
        public int getItemCount() { return list.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvRank, tvName, tvId, tvTier, tvValue, tvLabel;
            VH(View v) {
                super(v);
                tvRank = v.findViewById(R.id.tvRankNumber);
                tvName = v.findViewById(R.id.tvRankName);
                tvId = v.findViewById(R.id.tvRankMemberId);
                tvTier = v.findViewById(R.id.tvRankTier);
                tvValue = v.findViewById(R.id.tvRankValue);
                tvLabel = v.findViewById(R.id.tvRankLabel);
            }
        }
    }
}
