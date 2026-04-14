package com.altomedia.altoindoapp.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.altomedia.altoindoapp.R;
import com.altomedia.altoindoapp.models.Product;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminProductManagementActivity extends AppCompatActivity {
    private LinearLayout productListContainer;
    private FirebaseFirestore db;
    private StorageReference storageRef;
    private List<Uri> selectedImages = new ArrayList<>();
    private ActivityResultLauncher<String[]> imagePicker;
    private String pendingProductId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_products);

        if (!AdminGuard.isAdmin(this)) {
            finish();
            return;
        }

        productListContainer = findViewById(R.id.product_list_container);
        Button btnAdd = findViewById(R.id.btn_add_product);
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        imagePicker = registerForActivityResult(new ActivityResultContracts.OpenMultipleDocuments(), uris -> {
            selectedImages.clear();
            if (uris != null) {
                selectedImages.addAll(uris);
                Toast.makeText(this, selectedImages.size() + " gambar dipilih", Toast.LENGTH_SHORT).show();
            }
        });

        btnAdd.setOnClickListener(v -> showAddProductDialog());
        loadProducts();
    }

    private void loadProducts() {
        db.collection("products").get().addOnSuccessListener(querySnapshot -> {
            productListContainer.removeAllViews();
            for (com.google.firebase.firestore.QueryDocumentSnapshot doc : querySnapshot) {
                Product product = doc.toObject(Product.class);
                LinearLayout item = createProductItem(doc.getId(), product);
                productListContainer.addView(item);
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "Gagal memuat produk", Toast.LENGTH_SHORT).show());
    }

    private LinearLayout createProductItem(String id, Product product) {
        LinearLayout parent = new LinearLayout(this);
        parent.setOrientation(LinearLayout.VERTICAL);
        parent.setPadding(16, 16, 16, 16);
        parent.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);

        TextView title = new TextView(this);
        title.setText(product.name + " - Rp " + product.price);
        title.setTextSize(16f);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        parent.addView(title);

        TextView body = new TextView(this);
        body.setText(product.description + "\nDiskon: Rp " + product.discountPrice + "\nKomisi: " + product.commissionPercent + "%");
        parent.addView(body);

        Button btnDelete = new Button(this);
        btnDelete.setText("Hapus");
        btnDelete.setOnClickListener(v -> db.collection("products").document(id).delete().addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Produk dihapus", Toast.LENGTH_SHORT).show();
            loadProducts();
        }).addOnFailureListener(e -> Toast.makeText(this, "Gagal menghapus produk", Toast.LENGTH_SHORT).show()));
        parent.addView(btnDelete);
        return parent;
    }

    private void showAddProductDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Tambah Produk");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(24, 24, 24, 24);

        final EditText etName = new EditText(this);
        etName.setHint("Nama Produk");
        layout.addView(etName);

        final EditText etDesc = new EditText(this);
        etDesc.setHint("Deskripsi");
        layout.addView(etDesc);

        final EditText etPrice = new EditText(this);
        etPrice.setHint("Harga");
        etPrice.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(etPrice);

        final EditText etDiscount = new EditText(this);
        etDiscount.setHint("Harga Diskon (opsional)");
        etDiscount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(etDiscount);

        final EditText etCommission = new EditText(this);
        etCommission.setHint("Komisi (%)");
        etCommission.setInputType(android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL | android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(etCommission);

        final EditText etVariants = new EditText(this);
        etVariants.setHint("Varian (pisahkan dengan koma)");
        layout.addView(etVariants);

        Button btnChooseImages = new Button(this);
        btnChooseImages.setText("Pilih gambar (max 5)");
        btnChooseImages.setOnClickListener(v -> imagePicker.launch(new String[]{"image/*"}));
        layout.addView(btnChooseImages);

        builder.setView(layout);
        builder.setPositiveButton("Simpan", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String description = etDesc.getText().toString().trim();
            if (name.isEmpty() || description.isEmpty() || etPrice.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "Nama, deskripsi, dan harga wajib diisi", Toast.LENGTH_SHORT).show();
                return;
            }
            long price;
            try {
                price = Long.parseLong(etPrice.getText().toString().trim());
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Harga tidak valid", Toast.LENGTH_SHORT).show();
                return;
            }
            long discount;
            try {
                discount = etDiscount.getText().toString().trim().isEmpty() ? 0 : Long.parseLong(etDiscount.getText().toString().trim());
            } catch (NumberFormatException e) {
                discount = 0;
            }
            double commission;
            try {
                commission = etCommission.getText().toString().trim().isEmpty() ? 0 : Double.parseDouble(etCommission.getText().toString().trim());
            } catch (NumberFormatException e) {
                commission = 0;
            }
            List<String> variants = new ArrayList<>();
            if (!etVariants.getText().toString().trim().isEmpty()) {
                for (String variant : etVariants.getText().toString().trim().split(",")) {
                    variants.add(variant.trim());
                }
            }
            String id = String.valueOf(System.currentTimeMillis());
            pendingProductId = id;
            if (!selectedImages.isEmpty()) {
                uploadImagesAndSaveProduct(id, name, description, price, discount, commission, variants);
            } else {
                saveProduct(id, name, description, price, discount, commission, variants, new ArrayList<>());
            }
        });
        builder.setNegativeButton("Batal", null);
        builder.show();
    }

    private void uploadImagesAndSaveProduct(String id, String name, String description, long price, long discount, double commission, List<String> variants) {
        List<String> uploadedUrls = new ArrayList<>();
        int total = Math.min(selectedImages.size(), 5);
        for (int i = 0; i < total; i++) {
            Uri uri = selectedImages.get(i);
            StorageReference imageRef = storageRef.child("product_images/" + id + "/image_" + i + ".jpg");
            int finalI = i;
            imageRef.putFile(uri).addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                uploadedUrls.add(downloadUri.toString());
                if (uploadedUrls.size() == total) {
                    saveProduct(id, name, description, price, discount, commission, variants, uploadedUrls);
                }
            })).addOnFailureListener(e -> Toast.makeText(this, "Gagal upload gambar", Toast.LENGTH_SHORT).show());
        }
    }

    private void saveProduct(String id, String name, String description, long price, long discount, double commission, List<String> variants, List<String> imageUrls) {
        Product product = new Product(id, name, description, price, discount, commission, variants, imageUrls);
        db.collection("products").document(id).set(product).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Produk ditambahkan", Toast.LENGTH_SHORT).show();
            loadProducts();
            selectedImages.clear();
        }).addOnFailureListener(e -> Toast.makeText(this, "Gagal menambah produk", Toast.LENGTH_SHORT).show());
    }
}
