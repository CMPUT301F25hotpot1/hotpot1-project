package com.example.lottary.ui.admin.adapters;

import android.app.Dialog;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lottary.R;
import com.example.lottary.data.Event;

/**
 * Adapter used by the Admin Events screen to render a list of Event objects.
 * Each row contains title, city, time, venue, status color, thumbnail image,
 * and actions such as "remove" or viewing the event image.
 */
public class AdminEventsAdapter extends ListAdapter<Event, AdminEventsAdapter.ViewHolder> {

    /**
     * Callback interface invoked when an event's "Remove" button is clicked.
     */
    public interface OnRemoveClick {
        void onRemove(Event e);
    }

    private final OnRemoveClick removeClick;

    /**
     * Creates a new AdminEventsAdapter.
     *
     * @param removeClick callback executed when the user requests to remove an event
     */
    public AdminEventsAdapter(OnRemoveClick removeClick) {
        super(DIFF_CALLBACK);
        this.removeClick = removeClick;
    }

    private static final DiffUtil.ItemCallback<Event> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Event>() {
                @Override
                public boolean areItemsTheSame(@NonNull Event oldItem, @NonNull Event newItem) {
                    return oldItem.getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Event oldItem, @NonNull Event newItem) {
                    return oldItem.getTitle().equals(newItem.getTitle());
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_event, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event e = getItem(position);
        holder.bind(e);
    }

    /**
     * ViewHolder responsible for binding and managing a single event row.
     */
    class ViewHolder extends RecyclerView.ViewHolder {

        TextView title, location, time, venue, status;
        Button removeBtn, viewImageBtn;
        ImageView eventImage;

        /**
         * Constructs a ViewHolder and initializes all view references.
         *
         * @param itemView the root view of the row layout
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.tv_title);
            location = itemView.findViewById(R.id.tv_location);
            time = itemView.findViewById(R.id.tv_time);
            venue = itemView.findViewById(R.id.tv_venue);
            status = itemView.findViewById(R.id.tv_status);

            removeBtn = itemView.findViewById(R.id.btn_event_remove);
            viewImageBtn = itemView.findViewById(R.id.btn_view_image);
            eventImage = itemView.findViewById(R.id.img);
        }

        /**
         * Binds data from an Event model into the row's views.
         *
         * @param e the Event to display
         */
        void bind(Event e) {

            title.setText(e.getTitle());
            time.setText(e.getPrettyTime());

            String placeText;
            if (!e.getCity().isEmpty() && !e.getVenue().isEmpty()) {
                placeText = e.getCity() + " · " + e.getVenue();
            } else if (!e.getCity().isEmpty()) {
                placeText = e.getCity();
            } else {
                placeText = e.getVenue();
            }
            location.setText(placeText);

            if (e.isFull()) {
                status.setText("Full");
                status.setTextColor(Color.parseColor("#CC0000"));
            } else {
                status.setText("Open");
                status.setTextColor(Color.parseColor("#008A00"));
            }

            removeBtn.setOnClickListener(v -> removeClick.onRemove(e));

            Glide.with(itemView.getContext())
                    .load(e.getImageUrl())
                    .into(eventImage);

            eventImage.setOnClickListener(v -> showImagePreview(e.getImageUrl()));
            viewImageBtn.setOnClickListener(v -> showImagePreview(e.getImageUrl()));
        }

        /**
         * Displays a small dialog showing a larger preview of the event’s image.
         * Clicking the preview dismisses the dialog.
         *
         * @param url image URL to display
         */
        private void showImagePreview(String url) {

            Dialog dialog = new Dialog(itemView.getContext());
            dialog.setContentView(R.layout.dialog_image_preview);

            ImageView preview = dialog.findViewById(R.id.preview_image);

            Glide.with(itemView.getContext())
                    .load(url)
                    .into(preview);

            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            preview.setOnClickListener(v -> dialog.dismiss());

            dialog.show();
        }
    }
}
