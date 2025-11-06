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

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.Holder> {

    public interface Listener {
        void onSignUp(@NonNull NotificationItem item);
        void onDecline(@NonNull NotificationItem item);
        void onOverflow(@NonNull View anchor, @NonNull NotificationItem item);
    }

    private final List<NotificationItem> items = new ArrayList<>();
    private final Listener listener;
    private final DateFormat fmt = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);

    public NotificationsAdapter(Listener l) { this.listener = l; }

    public void submit(List<NotificationItem> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        NotificationItem n = items.get(position);
        h.txtTitle.setText(n.type.equals("selected") ? "Selected" :
                n.type.equals("cancelled") ? "Cancelled" : "Notification");
        h.txtMsg.setText(n.message);
        h.txtTime.setText(fmt.format(new Date(n.sentAtMs)));

        h.icon.setImageResource(
                n.type.equals("selected") ? R.drawable.ic_check :
                        n.type.equals("cancelled") ? R.drawable.ic_close : R.drawable.ic_info);

        boolean showActions = n.type.equals("selected");
        h.btnSign.setVisibility(showActions ? View.VISIBLE : View.GONE);
        h.btnDecline.setVisibility(showActions ? View.VISIBLE : View.GONE);

        h.btnSign.setOnClickListener(v -> { if (listener != null) listener.onSignUp(n); });
        h.btnDecline.setOnClickListener(v -> { if (listener != null) listener.onDecline(n); });
        h.btnOverflow.setOnClickListener(v -> { if (listener != null) listener.onOverflow(h.btnOverflow, n); });
    }

    @Override public int getItemCount() { return items.size(); }

    static class Holder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView txtTitle, txtMsg, txtTime;
        Button btnSign, btnDecline;
        ImageButton btnOverflow;
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

