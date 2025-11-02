package com.example.lottary.ui.browse;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottary.R;
import com.example.lottary.data.Event;

class BrowseEventsAdapter extends ListAdapter<Event, BrowseEventsAdapter.VH> {

    interface Listener {
        void onEventClick(@NonNull Event e);
        void onJoinClick(@NonNull Event e);
    }

    private final Listener listener;

    protected BrowseEventsAdapter(Listener l) {
        super(DIFF);
        this.listener = l;
    }

    private static final DiffUtil.ItemCallback<Event> DIFF = new DiffUtil.ItemCallback<Event>() {
        @Override public boolean areItemsTheSame(@NonNull Event a, @NonNull Event b) {
            return a.getId().equals(b.getId());
        }
        @Override public boolean areContentsTheSame(@NonNull Event a, @NonNull Event b) {
            return a.isFull() == b.isFull()
                    && a.getTitle().equals(b.getTitle())
                    && a.getCity().equals(b.getCity())
                    && a.getVenue().equals(b.getVenue())
                    && a.getPrettyStartTime().equals(b.getPrettyStartTime());
        }
    };

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_browse_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Event e = getItem(position);
        h.title.setText(e.getTitle());
        h.location.setText(e.getCity() + ", " + e.getVenue());
        h.time.setText(e.getPrettyStartTime());
        h.status.setText(e.isFull() ? "Full" : "Open");
        int color = ContextCompat.getColor(
                h.status.getContext(),
                e.isFull() ? android.R.color.holo_red_dark : android.R.color.holo_green_dark
        );
        h.status.setTextColor(color);
        h.btnJoin.setEnabled(!e.isFull());

        h.root.setOnClickListener(v -> listener.onEventClick(e));
        h.btnJoin.setOnClickListener(v -> listener.onJoinClick(e));
    }

    static class VH extends RecyclerView.ViewHolder {
        CardView root;
        TextView title, location, time, status;
        Button btnJoin;
        VH(@NonNull View v) {
            super(v);
            root = (CardView) v;
            title = v.findViewById(R.id.tvTitle);
            location = v.findViewById(R.id.tvLocation);
            time = v.findViewById(R.id.tvTime);
            status = v.findViewById(R.id.tvStatus);
            btnJoin = v.findViewById(R.id.btnJoin);
        }
    }

    void submit(java.util.List<Event> items) { submitList(items); }
}
