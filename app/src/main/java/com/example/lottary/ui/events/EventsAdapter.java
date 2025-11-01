package com.example.lottary.ui.events;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottary.R;
import com.example.lottary.data.Event;
import com.example.lottary.ui.events.edit.EditEventActivity;

import java.util.ArrayList;
import java.util.List;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.ViewHolder> {

    private final List<Event> data = new ArrayList<>();

    public void setData(@NonNull List<Event> newData) {
        data.clear();
        data.addAll(newData);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        Event e = data.get(pos);
        h.txtTitle.setText(e.getTitle());
        try {
            h.txtTime.setText(e.getPrettyStartTime() != null ? e.getPrettyStartTime() : "");
        } catch (Throwable t) {
            h.txtTime.setText("");
        }

        h.btnManage.setOnClickListener(v -> {
        });

        h.btnEdit.setOnClickListener(v -> {
            Context ctx = v.getContext();
            Intent i = new Intent(ctx, EditEventActivity.class);
            i.putExtra(EditEventActivity.EXTRA_EVENT_ID, e.getId()); // ✅ 统一 key
            ctx.startActivity(i);
        });
    }

    @Override public int getItemCount() { return data.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtTime;
        Button btnManage, btnEdit;
        ViewHolder(@NonNull View v) {
            super(v);
            txtTitle = v.findViewById(R.id.txt_title);
            txtTime  = v.findViewById(R.id.txt_time);
            btnManage = v.findViewById(R.id.btn_manage);
            btnEdit   = v.findViewById(R.id.btn_edit);
        }
    }
}
