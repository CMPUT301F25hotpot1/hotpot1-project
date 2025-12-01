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

/**
 * RecyclerView adapter used by {@code UserNotificationLogsActivity}
 * to render a simple, read-only list of notification logs for a user.
 *
 * <p>Each row shows:
 * <ul>
 *     <li>Notification title (event title)</li>
 *     <li>Recipient line ({@code "to &lt;name&gt;"} if present)</li>
 *     <li>Message body preview</li>
 *     <li>A status icon (currently a generic info icon)</li>
 * </ul>
 */
public class UserNotificationLogsAdapter
        extends RecyclerView.Adapter<UserNotificationLogsAdapter.ViewHolder> {

    /** Backing list; never {@code null}. */
    private final List<NotificationLog> items = new ArrayList<>();

    public UserNotificationLogsAdapter() {
        // No special init required.
    }

    /**
     * Replaces the adapter's items with a new list and refreshes the UI.
     *
     * @param newItems list of {@link NotificationLog} to display
     */
    public void setItems(List<NotificationLog> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    /**
     * ViewHolder that holds all the views of a single notification row.
     */
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
    public UserNotificationLogsAdapter.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_notification_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull UserNotificationLogsAdapter.ViewHolder holder,
            int position
    ) {
        NotificationLog log = items.get(position);

        // Title: usually the event title ("Coding Workshop").
        String title = log.getTitle();
        if (title == null) title = "";
        holder.titleText.setText(title);

        // Message body preview.
        String message = log.getMessage();
        if (message == null) message = "";
        holder.messageText.setText(message);

        // Recipient line: "to <name>" if present, otherwise leave blank.
        String recipientName = log.getRecipientName();
        if (recipientName != null && !recipientName.isEmpty()) {
            holder.recipientText.setText("to " + recipientName);
        } else {
            holder.recipientText.setText("");
        }

        // Simple status icon; could later be customized by log type.
        holder.statusIcon.setImageResource(R.drawable.ic_info_black_24dp);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
