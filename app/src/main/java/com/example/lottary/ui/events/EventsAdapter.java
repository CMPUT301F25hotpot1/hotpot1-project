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

import java.util.ArrayList;
import java.util.List;

/**
 * EventsAdapter
 *
 * Purpose:
 * RecyclerView adapter responsible for displaying events that the current user has created
 * or is managing. Each card shows the event title and time, along with “Manage” and “Edit”
 * action buttons that delegate user actions to a provided Listener.
 *
 * Design Role:
 * - Serves as the organizer’s event list adapter (the “Created Events” view).
 * - Decouples UI rendering from the logic that handles management and editing actions.
 * - Relies on the {@link Event} model for presentation data such as formatted start time.
 *
 * Outstanding Issues / Notes:
 * - Uses notifyDataSetChanged(); could be optimized via DiffUtil for large datasets.
 * - Assumes non-null event fields (title, startTime); upstream null safety recommended.
 * - Lacks empty/error-state UI; relies on fragment or parent activity to handle visibility.
 */
public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.ViewHolder> {

    public interface Listener {
        void onManage(@NonNull Event e);
        void onEdit(@NonNull Event e);
    }

    private final List<Event> items = new ArrayList<>();
    private final Listener listener;

    public EventsAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<Event> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Event e = items.get(position);
        h.txtTitle.setText(e.getTitle());
        h.txtTime.setText(e.getPrettyStartTime());

        h.btnManage.setOnClickListener(v -> {
            if (listener != null) listener.onManage(e);
        });
        h.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(e);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtTime;
        Button btnManage, btnEdit;

        ViewHolder(@NonNull View v) {
            super(v);
            txtTitle = v.findViewById(R.id.tv_title);
            txtTime = v.findViewById(R.id.tv_time);
            btnManage = v.findViewById(R.id.btn_manage);
            btnEdit = v.findViewById(R.id.btn_edit);
        }
    }
}
