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

/**
 * RecyclerView adapter for displaying a list of NotificationLog entries.
 * Each row shows the title, recipient, timestamp, and an associated icon.
 */
public class NotificationLogsAdapter extends ListAdapter<NotificationLog, NotificationLogsAdapter.ViewHolder> {

    /**
     * Creates a new NotificationLogsAdapter using the shared DiffUtil rules.
     */
    public NotificationLogsAdapter() {
        super(DIFF);
    }

    /**
     * DiffUtil callback for efficiently updating NotificationLog items.
     * Items are considered identical if they share the same ID.
     */
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

    /**
     * ViewHolder representing a single notification log row.
     * Holds references to UI components for title, recipient, time, and icon.
     */
    class ViewHolder extends RecyclerView.ViewHolder {

        TextView title, receiver, time;
        ImageView icon;

        /**
         * Creates a ViewHolder and binds view references.
         *
         * @param itemView the root view for the row layout
         */
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.log_title);
            receiver = itemView.findViewById(R.id.log_receiver);
            time = itemView.findViewById(R.id.log_time);
            icon = itemView.findViewById(R.id.log_icon);
        }

        /**
         * Binds a NotificationLog to the UI elements within the row.
         *
         * @param log the notification log to display
         */
        void bind(NotificationLog log) {
            title.setText(log.getTitle());
            receiver.setText("Recipient: " + log.getRecipientName());
            time.setText("Sent at: " + log.getPrettyTime());
        }
    }

    /**
     * Inflates a new row layout and creates a corresponding ViewHolder.
     *
     * @param parent the RecyclerView container
     * @param viewType currently unused, always a single view type
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification_log_row, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds a NotificationLog to an existing ViewHolder.
     *
     * @param holder the ViewHolder to bind data into
     * @param position list index of the item
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }
}
