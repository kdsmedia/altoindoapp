package com.altomedia.altoindoapp.activities;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
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
import com.altomedia.altoindoapp.models.IdCardRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import java.io.OutputStream;

public class AdminIdCardManagementActivity extends AppCompatActivity {
    private RecyclerView rvRequests;
    private TextView tvCount;
    private FirebaseFirestore db;
    private List<IdCardRequest> requestList = new ArrayList<>();
    private RequestAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_id_card);

        if (!AdminGuard.isAdmin(this)) { finish(); return; }

        db = FirebaseFirestore.getInstance();
        rvRequests = findViewById(R.id.rvIdCardRequests);
        tvCount = findViewById(R.id.tvRequestCount);

        rvRequests.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RequestAdapter(requestList);
        rvRequests.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        loadRequests();
    }

    private void loadRequests() {
        db.collection("id_card_requests")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener((snap, e) -> {
                if (snap == null) return;
                requestList.clear();
                for (QueryDocumentSnapshot doc : snap) {
                    IdCardRequest r = doc.toObject(IdCardRequest.class);
                    requestList.add(r);
                }
                tvCount.setText(requestList.size() + " permintaan");
                adapter.notifyDataSetChanged();
            });
    }

    private Bitmap generateIdCardBitmap(IdCardRequest req) {
        int w = 1012, h = 638;
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        paint.setColor(Color.parseColor("#001A50"));
        c.drawRect(0, 0, w, h, paint);

        paint.setColor(Color.parseColor("#003087"));
        c.drawRect(0, 0, w, 120, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(40);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        c.drawText("ALTOMEDIA INDONESIA", 20, 55, paint);

        paint.setColor(Color.parseColor("#AACCFF"));
        paint.setTextSize(22);
        paint.setTypeface(Typeface.DEFAULT);
        c.drawText("KARTU ANGGOTA RESMI", 20, 90, paint);

        paint.setColor(Color.parseColor("#FFD700"));
        paint.setTextSize(24);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        c.drawText("ALTOINDO", w - 180, 65, paint);

        paint.setColor(Color.parseColor("#AACCFF"));
        paint.setTextSize(20);
        c.drawText("ID ANGGOTA", 20, 160, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(32);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        c.drawText(req.memberId != null ? req.memberId : "-", 20, 200, paint);

        paint.setColor(Color.parseColor("#AACCFF"));
        paint.setTextSize(20);
        paint.setTypeface(Typeface.DEFAULT);
        c.drawText("NAMA LENGKAP", 20, 250, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(32);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        c.drawText(req.fullName != null ? req.fullName.toUpperCase() : "-", 20, 290, paint);

        paint.setColor(Color.parseColor("#AACCFF"));
        paint.setTextSize(20);
        paint.setTypeface(Typeface.DEFAULT);
        c.drawText("ALAMAT PENGIRIMAN", 20, 340, paint);

        paint.setColor(Color.parseColor("#CCCCCC"));
        paint.setTextSize(22);
        String addr = req.address != null ? req.address : "-";
        if (addr.length() > 60) addr = addr.substring(0, 57) + "...";
        c.drawText(addr, 20, 370, paint);

        paint.setColor(Color.parseColor("#001A50"));
        c.drawRect(0, h - 60, w, h, paint);

        paint.setColor(Color.parseColor("#7EC8E3"));
        paint.setTextSize(20);
        paint.setTypeface(Typeface.DEFAULT);
        c.drawText("www.altomedia.id", 20, h - 25, paint);

        paint.setColor(Color.parseColor("#00E676"));
        paint.setTextSize(20);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        c.drawText("● MEMBER RESMI", w - 250, h - 25, paint);

        return bmp;
    }

    private void downloadCard(IdCardRequest req) {
        try {
            Bitmap bmp = generateIdCardBitmap(req);
            String filename = "IDCARD_" + req.memberId + "_" + req.fullName.replace(" ", "_") + ".png";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/ALTOINDO_IDCARD");
                Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (uri != null) {
                    OutputStream out = getContentResolver().openOutputStream(uri);
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
                    if (out != null) out.close();
                    Toast.makeText(this, "ID Card diunduh: " + filename, Toast.LENGTH_LONG).show();
                }
            } else {
                java.io.File dir = new java.io.File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS), "ALTOINDO_IDCARD");
                dir.mkdirs();
                java.io.File file = new java.io.File(dir, filename);
                java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
                bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();
                Toast.makeText(this, "ID Card diunduh ke: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
            }
        } catch (Exception ex) {
            Toast.makeText(this, "Gagal mengunduh: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void markAsShipped(IdCardRequest req) {
        db.collection("id_card_requests").document(req.id)
            .update("status", "shipped")
            .addOnSuccessListener(v -> Toast.makeText(this, "Status diperbarui: Dikirim", Toast.LENGTH_SHORT).show())
            .addOnFailureListener(e -> Toast.makeText(this, "Gagal memperbarui", Toast.LENGTH_SHORT).show());
    }

    class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.VH> {
        List<IdCardRequest> list;
        RequestAdapter(List<IdCardRequest> list) { this.list = list; }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_id_card_request, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(VH h, int pos) {
            IdCardRequest r = list.get(pos);
            h.tvName.setText(r.fullName != null ? r.fullName : "-");
            h.tvId.setText("ID: " + (r.memberId != null ? r.memberId : "-"));
            h.tvAddress.setText("Alamat: " + (r.address != null ? r.address : "-"));
            h.tvDate.setText("Tanggal: " + (r.createdAt != null ? r.createdAt : "-"));

            switch (r.status != null ? r.status : "pending") {
                case "pending":
                    h.tvStatus.setText("Pending");
                    h.tvStatus.setTextColor(0xFFFFB74D);
                    break;
                case "processing":
                    h.tvStatus.setText("Diproses");
                    h.tvStatus.setTextColor(0xFF7EC8E3);
                    break;
                case "shipped":
                    h.tvStatus.setText("Dikirim");
                    h.tvStatus.setTextColor(0xFF00E676);
                    break;
            }

            h.btnDownload.setOnClickListener(v -> downloadCard(r));
            h.btnShip.setOnClickListener(v -> markAsShipped(r));

            if ("shipped".equals(r.status)) {
                h.btnShip.setEnabled(false);
                h.btnShip.setAlpha(0.5f);
            }
        }

        @Override
        public int getItemCount() { return list.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView tvName, tvId, tvAddress, tvDate, tvStatus;
            Button btnDownload, btnShip;
            VH(View v) {
                super(v);
                tvName = v.findViewById(R.id.tvReqName);
                tvId = v.findViewById(R.id.tvReqMemberId);
                tvAddress = v.findViewById(R.id.tvReqAddress);
                tvDate = v.findViewById(R.id.tvReqDate);
                tvStatus = v.findViewById(R.id.tvReqStatus);
                btnDownload = v.findViewById(R.id.btnDownloadCard);
                btnShip = v.findViewById(R.id.btnMarkShipped);
            }
        }
    }
}
