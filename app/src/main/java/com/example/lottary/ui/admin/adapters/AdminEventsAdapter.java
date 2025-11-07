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

public class AdminEventsAdapter extends ListAdapter<Event, AdminEventsAdapter.ViewHolder> {

    public interface OnRemoveClick {
        void onRemove(Event e);
    }

    private final OnRemoveClick removeClick;

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
                .inflate(R.layout.admin_event_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event e = getItem(position);
        holder.bind(e);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView title, city, time, venue, status;
        Button removeBtn, viewImageBtn;
        ImageView eventImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.admin_event_title);
            city = itemView.findViewById(R.id.admin_event_city);
            time = itemView.findViewById(R.id.admin_event_time);
            venue = itemView.findViewById(R.id.admin_event_venue);
            status = itemView.findViewById(R.id.admin_event_status);

            removeBtn = itemView.findViewById(R.id.admin_event_remove);
            viewImageBtn = itemView.findViewById(R.id.btn_view_image);
            eventImage = itemView.findViewById(R.id.event_image);
        }

        void bind(Event e) {

            title.setText(e.getTitle());
            city.setText(e.getCity() + ", " + e.getProvince());
            time.setText(e.getPrettyTime());
            venue.setText(e.getVenue());

            // ✅ status color logic
            if (e.isFull()) {
                status.setText("Full");
                status.setTextColor(Color.parseColor("#CC0000"));
            } else {
                status.setText("Open");
                status.setTextColor(Color.parseColor("#008A00"));
            }

            removeBtn.setOnClickListener(v -> removeClick.onRemove(e));

            // ✅ Load thumbnail image
            Glide.with(itemView.getContext())
                    .load(e.getImageUrl())
                    .into(eventImage);

            // ✅ Clicking thumbnail → open preview dialog
            eventImage.setOnClickListener(v -> showImagePreview(e.getImageUrl()));

            // ✅ Clicking View Image button → open preview dialog
            viewImageBtn.setOnClickListener(v -> showImagePreview(e.getImageUrl()));
        }

        // ✅ Beautiful small rounded image preview dialog
        private void showImagePreview(String url) {

            Dialog dialog = new Dialog(itemView.getContext());
            dialog.setContentView(R.layout.dialog_image_preview);

            ImageView preview = dialog.findViewById(R.id.preview_image);

            Glide.with(itemView.getContext())
                    .load(url)
                    .into(preview);

            // ✅ make background transparent for rounded card
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            // ✅ tap image to dismiss
            preview.setOnClickListener(v -> dialog.dismiss());

            dialog.show();
        }
    }
}
