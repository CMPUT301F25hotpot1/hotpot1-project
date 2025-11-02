package com.example.lottary.ui.notifications;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.lottary.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.VH> {

    public interface Handler {
        void onSignUp(NotificationItem n);
        void onDecline(NotificationItem n);
        void onOptOutOrganizer(NotificationItem n);
    }

    private final Handler handler;
    private final List<NotificationItem> data = new ArrayList<>();

    public NotificationsAdapter(Handler h) { this.handler = h; }

    public void submit(List<NotificationItem> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int v) {
        View view = LayoutInflater.from(p.getContext()).inflate(R.layout.item_notification, p, false);
        return new VH(view);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int i) {
        NotificationItem n = data.get(i);
        h.title.setText(n.title);
        h.msg.setText(n.message);
        h.time.setText(n.createdAt <= 0 ? "" : android.text.format.DateFormat.format("MMM d, h:mm a", n.createdAt));
        h.status.setText(n.status == null ? "" : n.status);

        boolean actionable = "selected".equalsIgnoreCase(n.type);
        h.btnSign.setVisibility(actionable ? View.VISIBLE : View.GONE);
        h.btnDecline.setVisibility(actionable ? View.VISIBLE : View.GONE);

        h.btnSign.setOnClickListener(v -> handler.onSignUp(n));
        h.btnDecline.setOnClickListener(v -> handler.onDecline(n));
        h.btnOptOut.setOnClickListener(v -> handler.onOptOutOrganizer(n));
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, msg, time, status;
        MaterialButton btnSign, btnDecline, btnOptOut;
        VH(@NonNull View v) {
            super(v);
            title = v.findViewById(R.id.tvTitle);
            msg = v.findViewById(R.id.tvMessage);
            time = v.findViewById(R.id.tvTime);
            status = v.findViewById(R.id.tvStatus);
            btnSign = v.findViewById(R.id.btnSignUp);
            btnDecline = v.findViewById(R.id.btnDecline);
            btnOptOut = v.findViewById(R.id.btnOptOutThisOrganizer);
        }
    }
}

