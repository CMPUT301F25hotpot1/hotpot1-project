package com.example.lottary.ui.events;

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
import com.example.lottary.ui.events.JoinedEventsFragment.EntrantRow;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

/**
 * EntrantEventsAdapter
 *
 * Purpose:
 * RecyclerView adapter that renders the “Joined Events” list for an entrant/user.
 * Each card displays event title, time, place, a status badge (Open / Selected / Not Selected),
 * and shows action buttons depending on the precomputed visibility flags in {@link EntrantRow}.
 *
 * Design Role:
 * - Pure view-binding adapter: relies on {@link JoinedEventsFragment} to supply a list of
 *   precomputed {@link EntrantRow} items (including status text and which buttons to show).
 * - Delegates button actions via the {@link Listener} callback so the fragment handles business logic.
 *
 * Outstanding Issues / Notes:
 * - No diffing optimization (uses notifyDataSetChanged()); consider ListAdapter + DiffUtil if lists grow.
 * - Assumes {@link Event} provides non-null formatted fields (e.g., getPrettyStartTime()).
 * - Minimal error/empty handling; relies on upstream filtering and data preparation.
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

        String placeText;
        if (!e.getCity().isEmpty() && !e.getVenue().isEmpty()) {
            placeText = e.getVenue() + " · " + e.getCity();
        } else if (!e.getCity().isEmpty()) {
            placeText = e.getCity();
        } else {
            placeText = e.getVenue();
        }
        h.place.setText(placeText);

        h.status.setVisibility(View.VISIBLE);
        h.status.setText(row.status);

        // TODO: Add selected and not selected color option
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

        h.btnSignUp.setVisibility(row.showSignUp ? View.VISIBLE : View.GONE);
        h.btnDecline.setVisibility(row.showDecline ? View.VISIBLE : View.GONE);
        h.btnLeave.setVisibility(row.showLeave ? View.VISIBLE : View.GONE);

        h.btnSignUp.setOnClickListener(v -> { if (cb != null) cb.onSignUp(e); });
        h.btnDecline.setOnClickListener(v -> { if (cb != null) cb.onDecline(e); });
        h.btnLeave.setOnClickListener(v -> { if (cb != null) cb.onLeaveWaitlist(e); });
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        CardView root;
        TextView title, time, place, status;
        Button btnSignUp, btnDecline, btnLeave;
        ImageView poster;
        VH(@NonNull View v) {
            super(v);
            root = (CardView) v;
            title = v.findViewById(R.id.tv_title);
            time  = v.findViewById(R.id.tv_time);
            place = v.findViewById(R.id.tv_location);
            status = v.findViewById(R.id.tv_status);
            poster = v.findViewById(R.id.img);
            btnSignUp = v.findViewById(R.id.btn_sign_up);
            btnDecline = v.findViewById(R.id.btn_decline);
            btnLeave = v.findViewById(R.id.btn_leave_waiting);
        }
    }
}
