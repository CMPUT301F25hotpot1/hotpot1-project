package com.example.lottary.ui.admin.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottary.R;
import com.example.lottary.data.NotificationLog;

import java.util.ArrayList;
import java.util.List;

public class UserNotificationLogsAdapter
        extends RecyclerView.Adapter<UserNotificationLogsAdapter.ViewHolder> {

    private final List<NotificationLog> items = new ArrayList<>();

    public UserNotificationLogsAdapter() {}

    public void setItems(List<NotificationLog> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView statusIcon;
        TextView titleText;
        TextView recipientText;
        TextView messageText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            statusIcon = itemView.findViewById(R.id.imageStatusIcon);
            titleText = itemView.findViewById(R.id.textNotificationTitle);
            recipientText = itemView.findViewById(R.id.textNotificationRecipient);
            messageText = itemView.findViewById(R.id.textNotificationMessage);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_notification_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationLog log = items.get(position);

        String title = log.getTitle();
        if (title == null) title = "";
        holder.titleText.setText(title);

        String message = log.getMessage();
        if (message == null) message = "";
        holder.messageText.setText(message);

        String recipientName = log.getRecipientName();
        if (recipientName != null && !recipientName.isEmpty()) {
            holder.recipientText.setText("to " + recipientName);
        } else {
            holder.recipientText.setText("");
        }

        holder.statusIcon.setImageResource(android.R.drawable.ic_dialog_info);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
