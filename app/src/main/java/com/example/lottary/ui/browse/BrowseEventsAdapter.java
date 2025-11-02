package com.example.lottary.ui.browse;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.lottary.R;
import com.example.lottary.data.Event;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class BrowseEventsAdapter extends RecyclerView.Adapter<BrowseEventsAdapter.VH> {
    public interface ClickHandler { void onOpen(Event e); }

    private final ClickHandler handler;
    private final List<Event> data = new ArrayList<>();

    public BrowseEventsAdapter(ClickHandler h) { this.handler = h; }

    public void submit(List<Event> items) {
        data.clear();
        if (items != null) data.addAll(items);
        notifyDataSetChanged();
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int v) {
        View view = LayoutInflater.from(p.getContext()).inflate(R.layout.item_event_browse_card, p, false);
        return new VH(view);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int i) {
        Event e = data.get(i);
        h.title.setText(e.getTitle());
        h.location.setText(e.getCity());
        h.time.setText(e.getPrettyStartTime());
        h.venue.setText(e.getVenue());
        h.status.setText(e.isFull() ? "Not Selected" : "Open");
        h.btnJoin.setOnClickListener(v -> handler.onOpen(e));
        h.itemView.setOnClickListener(v -> handler.onOpen(e));
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, location, time, venue, status;
        MaterialButton btnJoin;
        VH(@NonNull View v) {
            super(v);
            title = v.findViewById(R.id.tvTitle);
            location = v.findViewById(R.id.tvLocation);
            time = v.findViewById(R.id.tvTime);
            venue = v.findViewById(R.id.tvVenue);
            status = v.findViewById(R.id.tvStatus);
            btnJoin = v.findViewById(R.id.btnJoin);
        }
    }
}

