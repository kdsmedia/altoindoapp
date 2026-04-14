package com.altomedia.altoindoapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.altomedia.altoindoapp.R;
import com.altomedia.altoindoapp.models.NotificationMessage;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private final List<NotificationMessage> list;

    public NotificationAdapter(List<NotificationMessage> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationMessage notif = list.get(position);
        holder.tvTitle.setText(notif.title != null ? notif.title : "");
        holder.tvBody.setText(notif.body != null ? notif.body : "");
        holder.tvDate.setText(notif.createdAt != null ? notif.createdAt : "");
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvBody, tvDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvNotifTitle);
            tvBody = itemView.findViewById(R.id.tvNotifBody);
            tvDate = itemView.findViewById(R.id.tvNotifDate);
        }
    }
}
