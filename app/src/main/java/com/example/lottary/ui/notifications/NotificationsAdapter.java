/*
 * NotificationsAdapter.java
 *
 * RecyclerView adapter for rendering NotificationItem rows in the
 * notifications inbox.
 *
 * Responsibilities:
 * - Bind NotificationItem data (message, time, type-specific icon).
 * - Show contextual actions for "selected" notifications
 *   (Sign Up / Decline buttons).
 * - Forward user interactions to a Listener implemented by the host Activity.
 *
 * Outstanding notes:
 * - Type values such as "selected" and "cancelled" are string-based and
 *   must match what is written to Firestore.
 * - Diffing is not optimized (notifyDataSetChanged is used) which is
 *   acceptable for the small lists in this project.
 */

package com.example.lottary.ui.notifications;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottary.R;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Adapter that displays a list of {@link NotificationItem} instances.
 * <p>
 * The hosting {@link android.app.Activity} or {@link androidx.fragment.app.Fragment}
 * must implement {@link NotificationsAdapter.Listener} to handle user actions.
 */
public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.Holder> {

    /**
     * Callback interface for user interactions on notification rows.
     */
    public interface Listener {

        /**
         * Called when the user taps the "Sign Up" button on a notification.
         *
         * @param item the notification associated with the action
         */
        void onSignUp(@NonNull NotificationItem item);

        /**
         * Called when the user taps the "Decline" button on a notification.
         *
         * @param item the notification associated with the action
         */
        void onDecline(@NonNull NotificationItem item);

        /**
         * Called when the user taps the overflow button on a notification row.
         * The host is responsible for showing a popup menu and handling actions.
         *
         * @param anchor view used as the popup anchor
         * @param item   the notification associated with the overflow menu
         */
        void onOverflow(@NonNull View anchor, @NonNull NotificationItem item);
    }

    /** Backing list of notifications currently displayed. */
    private final List<NotificationItem> items = new ArrayList<>();

    /** Listener for row-level actions, typically implemented by the Activity. */
    private final Listener listener;

    /** Formatter for displaying the sent time to the user. */
    private final DateFormat fmt =
            DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);

    /**
     * Creates a new adapter instance.
     *
     * @param l listener that will handle user actions; may be {@code null}
     */
    public NotificationsAdapter(Listener l) {
        this.listener = l;
    }

    /**
     * Replaces the current list of notifications and refreshes the UI.
     *
     * @param list new list of notifications; if {@code null}, an empty list is used
     */
    public void submit(List<NotificationItem> list) {
        items.clear();
        if (list != null) {
            items.addAll(list);
        }
        // For this project size, a full refresh is sufficient.
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate a single notification row layout.
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        final NotificationItem n = items.get(position);

        // Set title based on notification type.
        // "selected" -> Selected, "cancelled" -> Cancelled, otherwise generic.
        h.txtTitle.setText(
                n.type.equals("selected") ? "Selected" :
                        n.type.equals("cancelled") ? "Cancelled" : "Notification"
        );

        // Bind message body.
        h.txtMsg.setText(n.message);

        // Bind formatted timestamp.
        h.txtTime.setText(fmt.format(new Date(n.sentAtMs)));

        // Choose icon according to type.
        h.icon.setImageResource(
                n.type.equals("selected")  ? R.drawable.ic_check :
                        n.type.equals("cancelled") ? R.drawable.ic_close :
                                R.drawable.ic_info
        );

        // Only "selected" notifications show Sign Up / Decline actions.
        boolean showActions = n.type.equals("selected");
        h.btnSign.setVisibility(showActions ? View.VISIBLE : View.GONE);
        h.btnDecline.setVisibility(showActions ? View.VISIBLE : View.GONE);

        // Wire button callbacks to the Listener if provided.
        h.btnSign.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSignUp(n);
            }
        });

        h.btnDecline.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDecline(n);
            }
        });

        h.btnOverflow.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOverflow(h.btnOverflow, n);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * ViewHolder representing a single notification row.
     * Holds references to all views that are updated during binding.
     */
    static class Holder extends RecyclerView.ViewHolder {

        final ImageView icon;
        final TextView txtTitle;
        final TextView txtMsg;
        final TextView txtTime;
        final Button btnSign;
        final Button btnDecline;
        final ImageButton btnOverflow;

        Holder(@NonNull View v) {
            super(v);
            icon = v.findViewById(R.id.icon);
            txtTitle = v.findViewById(R.id.txt_title);
            txtMsg = v.findViewById(R.id.txt_msg);
            txtTime = v.findViewById(R.id.txt_time);
            btnSign = v.findViewById(R.id.btn_sign);
            btnDecline = v.findViewById(R.id.btn_decline);
            btnOverflow = v.findViewById(R.id.btn_overflow);
        }
    }
}

