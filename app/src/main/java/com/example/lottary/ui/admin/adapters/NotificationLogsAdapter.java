package com.example.lottary.ui.admin.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottary.R;
import com.example.lottary.data.NotificationLog;

public class NotificationLogsAdapter extends ListAdapter<NotificationLog, NotificationLogsAdapter.ViewHolder> {

    public NotificationLogsAdapter() {
        super(DIFF);
    }

    private static final DiffUtil.ItemCallback<NotificationLog> DIFF =
            new DiffUtil.ItemCallback<NotificationLog>() {
                @Override
                public boolean areItemsTheSame(@NonNull NotificationLog oldItem, @NonNull NotificationLog newItem) {
                    return oldItem.getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull NotificationLog oldItem, @NonNull NotificationLog newItem) {
                    return oldItem.equals(newItem);
                }
            };

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView title, receiver, time;
        ImageView icon;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.log_title);
            receiver = itemView.findViewById(R.id.log_receiver);
            time = itemView.findViewById(R.id.log_time);
            icon = itemView.findViewById(R.id.log_icon);
        }

        void bind(NotificationLog log) {
            title.setText(log.getTitle());
            receiver.setText("Recipient: " + log.getRecipientName());
            time.setText("Sent at: " + log.getPrettyTime());
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int v) {
        View view = LayoutInflater.from(p.getContext())
                .inflate(R.layout.notification_log_row, p, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        h.bind(getItem(pos));
    }
}
