package com.example.lottary.ui.events;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottary.R;
import com.example.lottary.data.Event;
import com.example.lottary.data.GlideApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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
        // Bind the Event at the requested position.
        Event e = items.get(position);

        // Basic textual fields.
        h.title.setText(e.getTitle());
        h.location.setText(e.getVenue() + ", " + e.getCity());
        h.time.setText(e.getPrettyStartTime());

        // Status label + color (red for "Full", green for "Open").
        // TODO: Add ended color option
        h.status.setText(e.isFull() ? "Full" : "Open");
        int color = ContextCompat.getColor(
                h.status.getContext(),
                e.isFull() ? R.color.full_red : R.color.open_green
        );
        h.status.setTextColor(color);

        // Set poster if one is available
        String posterUrl = e.getImageUrl();
        if (!posterUrl.isEmpty()) {
            // Reference to event poster in Cloud Storage
            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(posterUrl);
            // Download directly from StorageReference using Glide
            // (See MyAppGlideModule for Loader registration)
            GlideApp.with(h.root.getContext())
                    .load(storageReference)
                    .into(h.poster);
            h.poster.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else h.poster.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        h.btnManage.setOnClickListener(v -> {
            if (listener != null) listener.onManage(e);
        });
        h.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(e);
        });
        // h.root.setOnClickListener(v -> listener.onEventClick(e));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView root;
        TextView title, time, location, status;
        Button btnManage, btnEdit;
        ImageView poster;

        ViewHolder(@NonNull View v) {
            super(v);
            root = (CardView) v;
            title = v.findViewById(R.id.tv_title);
            location = v.findViewById(R.id.tv_location);
            time = v.findViewById(R.id.tv_time);
            status = v.findViewById(R.id.tv_status);
            poster = v.findViewById(R.id.img);
            btnManage = v.findViewById(R.id.btn_manage);
            btnEdit = v.findViewById(R.id.btn_edit);
        }
    }
}
