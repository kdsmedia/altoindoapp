package com.altomedia.altoindoapp.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.altomedia.altoindoapp.R;
import com.altomedia.altoindoapp.models.Transaction;
import java.util.List;
import java.text.NumberFormat;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {
    private final List<Transaction> list;

    public TransactionAdapter(List<Transaction> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction trx = list.get(position);
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

        switch (trx.type != null ? trx.type : "") {
            case "transfer":
                holder.tvTypeIcon.setText("→");
                holder.tvTrxType.setText("Transfer");
                holder.tvTrxDesc.setText("Ke: " + (trx.toMemberId != null ? trx.toMemberId : "-"));
                holder.tvTrxAmount.setTextColor(Color.parseColor("#C62828"));
                holder.tvTrxAmount.setText("- " + nf.format(trx.amount));
                break;
            case "topup":
                holder.tvTypeIcon.setText("+");
                holder.tvTrxType.setText("Top Up");
                holder.tvTrxDesc.setText("Via QRIS");
                holder.tvTrxAmount.setTextColor(Color.parseColor("#2E7D32"));
                holder.tvTrxAmount.setText("+ " + nf.format(trx.amount));
                break;
            case "withdraw":
                holder.tvTypeIcon.setText("↓");
                holder.tvTrxType.setText("Penarikan");
                holder.tvTrxDesc.setText(trx.description != null ? trx.description : "");
                holder.tvTrxAmount.setTextColor(Color.parseColor("#C62828"));
                holder.tvTrxAmount.setText("- " + nf.format(trx.amount));
                break;
            default:
                holder.tvTypeIcon.setText("◆");
                holder.tvTrxType.setText(trx.type != null ? trx.type : "-");
                holder.tvTrxDesc.setText(trx.description != null ? trx.description : "");
                holder.tvTrxAmount.setText(nf.format(trx.amount));
        }

        holder.tvTrxDate.setText(trx.createdAt != null ? trx.createdAt : "");
        holder.tvTrxStatus.setText(trx.status != null ? trx.status : "");

        int statusColor = "success".equals(trx.status) || "approved".equals(trx.status)
            ? Color.parseColor("#2E7D32") : Color.parseColor("#E65100");
        holder.tvTrxStatus.setTextColor(statusColor);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTypeIcon, tvTrxType, tvTrxDesc, tvTrxDate, tvTrxAmount, tvTrxStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTypeIcon = itemView.findViewById(R.id.tvTypeIcon);
            tvTrxType = itemView.findViewById(R.id.tvTrxType);
            tvTrxDesc = itemView.findViewById(R.id.tvTrxDesc);
            tvTrxDate = itemView.findViewById(R.id.tvTrxDate);
            tvTrxAmount = itemView.findViewById(R.id.tvTrxAmount);
            tvTrxStatus = itemView.findViewById(R.id.tvTrxStatus);
        }
    }
}
