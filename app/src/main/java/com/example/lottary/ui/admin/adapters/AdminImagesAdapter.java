package com.example.lottary.ui.admin.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottary.R;
import com.squareup.picasso.Picasso;

public class AdminImagesAdapter extends ListAdapter<String, AdminImagesAdapter.ViewHolder> {

    public interface OnImageClick {
        void onClick(String url);
    }

    public interface OnRemoveClick {
        void onRemove(String url);
    }

    private final OnImageClick imageClick;
    private final OnRemoveClick removeClick;

    public AdminImagesAdapter(OnImageClick imageClick, OnRemoveClick removeClick) {
        super(DIFF_CALLBACK);
        this.imageClick = imageClick;
        this.removeClick = removeClick;
    }

    private static final DiffUtil.ItemCallback<String> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<String>() {
                @Override
                public boolean areItemsTheSame(@NonNull String oldItem, @NonNull String newItem) {
                    return oldItem.equals(newItem);
                }

                @Override
                public boolean areContentsTheSame(@NonNull String oldItem, @NonNull String newItem) {
                    return oldItem.equals(newItem);
                }
            };

    @NonNull
    @Override
    public AdminImagesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                            int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_image_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminImagesAdapter.ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView img;
        ImageButton removeBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.admin_image_thumb);
            removeBtn = itemView.findViewById(R.id.admin_image_remove);
        }

        void bind(String url) {
            Picasso.get().load(url).fit().centerCrop().into(img);

            img.setOnClickListener(v -> imageClick.onClick(url));

            removeBtn.setOnClickListener(v -> removeClick.onRemove(url));
        }
    }
}
