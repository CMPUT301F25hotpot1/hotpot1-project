package com.example.lottary.ui.admin.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

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

        TextView title, city, time, status;
        Button removeBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.admin_event_title);
            city = itemView.findViewById(R.id.admin_event_city);
            time = itemView.findViewById(R.id.admin_event_time);
            status = itemView.findViewById(R.id.admin_event_status);
            removeBtn = itemView.findViewById(R.id.admin_event_remove);
        }

        void bind(Event e) {
            title.setText(e.getTitle());
            city.setText(e.getCity());
            time.setText(e.getPrettyTime());

            // ✅ boolean → Full / Open
            if (e.isFull()) {
                status.setText("Full");
                status.setTextColor(Color.parseColor("#CC0000"));
            } else {
                status.setText("Open");
                status.setTextColor(Color.parseColor("#008A00"));
            }

            removeBtn.setOnClickListener(v -> removeClick.onRemove(e));
        }
    }
}