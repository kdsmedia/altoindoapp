package com.altomedia.altoindoapp.utils;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Transaction;

public class FirebaseHelper {

    public interface TransactionCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public static void transferBalance(String senderUid, String receiverUid, long amount, TransactionCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference senderRef = db.collection("users").document(senderUid);
        DocumentReference receiverRef = db.collection("users").document(receiverUid);

        db.runTransaction((Transaction.Function<Void>) transaction -> {
            DocumentSnapshot senderSnap = transaction.get(senderRef);
            DocumentSnapshot receiverSnap = transaction.get(receiverRef);

            Long senderBalanceRaw = senderSnap.getLong("balance_wallet");
            Long receiverBalanceRaw = receiverSnap.getLong("balance_wallet");

            long senderBalance = senderBalanceRaw != null ? senderBalanceRaw : 0L;
            long receiverBalance = receiverBalanceRaw != null ? receiverBalanceRaw : 0L;

            if (senderBalance < amount) {
                throw new RuntimeException("Saldo tidak mencukupi");
            }

            transaction.update(senderRef, "balance_wallet", senderBalance - amount);
            transaction.update(receiverRef, "balance_wallet", receiverBalance + amount);
            return null;
        }).addOnSuccessListener(aVoid -> callback.onSuccess())
          .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}
