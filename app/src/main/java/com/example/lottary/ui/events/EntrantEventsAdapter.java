package com.example.lottary.ui.events;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottary.R;
import com.example.lottary.data.Event;
import com.example.lottary.ui.events.JoinedEventsFragment.EntrantRow;

import java.util.ArrayList;
import java.util.List;

/**
 * Entrant（加入者）视角卡片适配器：
 * - 右上角显示状态：Open / Selected / Not Selected
 * - 底部动态显示：Sign Up / Decline / Leave Waiting List
 *
 * 数据模型：JoinedEventsFragment.EntrantRow（包含 Event、status、按钮可见性）
 */
public class EntrantEventsAdapter extends RecyclerView.Adapter<EntrantEventsAdapter.VH> {

    public interface Listener {
        void onSignUp(@NonNull Event e);
        void onDecline(@NonNull Event e);
        void onLeaveWaitlist(@NonNull Event e);
    }

    private final List<EntrantRow> items = new ArrayList<>();
    private final Listener cb;

    public EntrantEventsAdapter(Listener cb) { this.cb = cb; }

    /** 接收 EntrantRow 列表（与 JoinedEventsFragment.render() 对齐） */
    public void submit(List<EntrantRow> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int vt) {
        View v = LayoutInflater.from(p.getContext())
                .inflate(R.layout.item_event_joined_card, p, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        EntrantRow row = items.get(position);
        Event e = row.event;

        h.title.setText(e.getTitle());
        h.time.setText(e.getPrettyStartTime());

        // 地点展示：city · venue（有哪个用哪个）
        String placeText;
        if (!e.getCity().isEmpty() && !e.getVenue().isEmpty()) {
            placeText = e.getCity() + " · " + e.getVenue();
        } else if (!e.getCity().isEmpty()) {
            placeText = e.getCity();
        } else {
            placeText = e.getVenue();
        }
        h.place.setText(placeText);

        // —— 状态角标 —— //
        h.status.setVisibility(View.VISIBLE);
        h.status.setText(row.status);

        // —— 按钮可见性（由 JoinedEventsFragment 计算完成，这里直接使用） —— //
        h.btnSignUp.setVisibility(row.showSignUp ? View.VISIBLE : View.GONE);
        h.btnDecline.setVisibility(row.showDecline ? View.VISIBLE : View.GONE);
        h.btnLeave.setVisibility(row.showLeave ? View.VISIBLE : View.GONE);

        // —— 点击回调 —— //
        h.btnSignUp.setOnClickListener(v -> { if (cb != null) cb.onSignUp(e); });
        h.btnDecline.setOnClickListener(v -> { if (cb != null) cb.onDecline(e); });
        h.btnLeave.setOnClickListener(v -> { if (cb != null) cb.onLeaveWaitlist(e); });
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, time, place, status;
        Button btnSignUp, btnDecline, btnLeave;
        VH(@NonNull View v) {
            super(v);
            title = v.findViewById(R.id.tv_title);
            time  = v.findViewById(R.id.tv_time);
            place = v.findViewById(R.id.tv_place);
            status = v.findViewById(R.id.tv_status);
            btnSignUp = v.findViewById(R.id.btn_sign_up);
            btnDecline = v.findViewById(R.id.btn_decline);
            btnLeave = v.findViewById(R.id.btn_leave_waiting);
        }
    }
}
