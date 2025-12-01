package com.example.lottary.ui.browse;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottary.R;
import com.example.lottary.data.Event;
import com.example.lottary.data.GlideApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * RecyclerView adapter for the "Browse" list.
 *
 * Responsibilities:
 * - Efficiently render {@link Event} items using {@link ListAdapter} + {@link DiffUtil}.
 * - Bind textual fields (title/location/time/status) and enable/disable the "Join" button
 *   depending on whether the event is full.
 * - Forward click events (card click + join click) to the hosting component via {@link Listener}.
 *
 * Notes:
 * - This adapter does not perform any data mutation. It renders a snapshot provided via {@link #submit(java.util.List)}.
 * - No stable IDs are used; identity is determined by {@link Event#getId()} in the diff callback.
 */
class BrowseEventsAdapter extends ListAdapter<Event, BrowseEventsAdapter.VH> {

    /**
     * Minimal callback surface for the host (fragment/activity) to react to user actions.
     * Implementations should handle navigation to details and waitlist join logic.
     */
    interface Listener {
        void onEventClick(@NonNull Event e);
        void onJoinClick(@NonNull Event e);
    }

    /** Non-null listener used to propagate UI events. */
    private final Listener listener;

    /**
     * Constructor.
     * @param l callback target; must not be null and is expected to live as long as the adapter.
     */
    protected BrowseEventsAdapter(Listener l) {
        super(DIFF);
        this.listener = l;
    }

    /**
     * DiffUtil for efficient list updates.
     * - Identity: same event id.
     * - Content: fields relevant to current UI (full/title/city/venue/pretty time).
     *   If any of these change, the item will be rebound.
     */
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
        // Inflate a single event card. The root view is a CardView.
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_browse_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        // Bind the Event at the requested position.
        Event e = getItem(position);

        // Basic textual fields.
        h.title.setText(e.getTitle());
        h.location.setText(e.getVenue() + ", " + e.getCity());
        h.time.setText(e.getPrettyStartTime());

        // Status label + color (red for "Full", green for "Open").
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
        } else
            h.poster.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        // Disable the join button for full events to prevent invalid actions.
        h.btnJoin.setEnabled(!e.isFull());

        // Wire clicks: card -> open details; button -> join waitlist.
        h.root.setOnClickListener(v -> listener.onEventClick(e));
        h.btnJoin.setOnClickListener(v -> listener.onJoinClick(e));
    }

    /**
     * ViewHolder for a single event card.
     * Caches view references to avoid repeated findViewById calls during binds.
     */
    static class VH extends RecyclerView.ViewHolder {
        CardView root;
        TextView title, location, time, status;
        Button btnJoin;
        ImageView poster;
        VH(@NonNull View v) {
            super(v);
            root = (CardView) v;
            title = v.findViewById(R.id.tv_title);
            location = v.findViewById(R.id.tv_location);
            time = v.findViewById(R.id.tv_time);
            poster = v.findViewById(R.id.iv_poster);
            status = v.findViewById(R.id.tv_status);
            btnJoin = v.findViewById(R.id.btnJoin);
            poster = v.findViewById(R.id.img);
        }
    }

    /**
     * Convenience wrapper for {@link #submitList(java.util.List)} to keep call sites concise.
     * Must be called on the main thread (same contract as ListAdapter).
     */
    void submit(java.util.List<Event> items) { submitList(items); }
}
