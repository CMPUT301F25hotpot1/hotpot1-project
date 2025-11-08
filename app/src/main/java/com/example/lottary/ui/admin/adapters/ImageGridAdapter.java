package com.example.lottary.ui.admin.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lottary.R;
import com.example.lottary.data.Image;

/**
 * RecyclerView adapter used for presenting images in a uniform grid layout.
 * Each grid item displays an image thumbnail and an optional title. The adapter
 * supports click and long-click callbacks and can optionally enable stable IDs
 * for improved RecyclerView animations.
 */
public class ImageGridAdapter extends ListAdapter<Image, ImageGridAdapter.VH> {

    /**
     * Callback invoked when an image item is clicked.
     */
    public interface OnItemClick {
        /**
         * @param image the clicked Image object
         */
        void onClick(@NonNull Image image);
    }

    /**
     * Optional callback invoked when an image item is long-pressed.
     */
    public interface OnItemLongClick {
        /**
         * @param image the long-pressed Image object
         */
        void onLongClick(@NonNull Image image);
    }

    private final OnItemClick click;
    @Nullable private OnItemLongClick longClick;

    /**
     * Creates a new ImageGridAdapter with the required click callback.
     *
     * @param click callback executed when an item is tapped
     */
    public ImageGridAdapter(OnItemClick click) {
        super(DIFF);
        this.click = click;
        setHasStableIds(false);
    }

    /**
     * Enables or disables optional long-press actions.
     *
     * @param l the long-press listener, or null to disable long-press behavior
     */
    public void setOnItemLongClick(@Nullable OnItemLongClick l) {
        this.longClick = l;
    }

    /**
     * Enables stable IDs for smoother animations. Disabled by default.
     *
     * @param on true to enable stable ID mode
     */
    public void enableStableIds(boolean on) {
        setHasStableIds(on);
    }

    private static final DiffUtil.ItemCallback<Image> DIFF = new DiffUtil.ItemCallback<Image>() {
        @Override
        public boolean areItemsTheSame(@NonNull Image o, @NonNull Image n) {
            String oid = o.getId() == null ? "" : o.getId();
            String nid = n.getId() == null ? "" : n.getId();
            return oid.equals(nid);
        }

        @Override
        public boolean areContentsTheSame(@NonNull Image o, @NonNull Image n) {
            String ou = o.getUrl() == null ? "" : o.getUrl();
            String nu = n.getUrl() == null ? "" : n.getUrl();
            String ot = o.getTitle() == null ? "" : o.getTitle();
            String nt = n.getTitle() == null ? "" : n.getTitle();
            long oa = o.getCreatedAt() == null ? 0 : o.getCreatedAt().getSeconds();
            long na = n.getCreatedAt() == null ? 0 : n.getCreatedAt().getSeconds();
            return ou.equals(nu) && ot.equals(nt) && oa == na;
        }
    };

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image_grid, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Image img = getItem(position);

        Glide.with(h.image.getContext())
                .load(img.getUrl() == null || img.getUrl().isEmpty() ? null : img.getUrl())
                .placeholder(R.drawable.placeholder_square)
                .centerCrop()
                .into(h.image);

        if (img.getTitle() != null && !img.getTitle().isEmpty()) {
            h.title.setVisibility(View.VISIBLE);
            h.title.setText(img.getTitle());
        } else {
            h.title.setVisibility(View.GONE);
        }

        h.itemView.setOnClickListener(v -> {
            if (click != null) click.onClick(img);
        });

        h.itemView.setOnLongClickListener(v -> {
            if (longClick != null) {
                longClick.onLongClick(img);
                return true;
            }
            return false;
        });
    }

    /**
     * Provides stable IDs when enabled via {@link #enableStableIds(boolean)}.
     * Uses image id, URL, or a fallback position key.
     *
     * @param position adapter position
     * @return stable ID derived from image metadata
     */
    @Override
    public long getItemId(int position) {
        Image img = getItem(position);
        String key = (img.getId() != null && !img.getId().isEmpty())
                ? img.getId()
                : (img.getUrl() != null ? img.getUrl() : ("pos_" + position));
        return key.hashCode();
    }

    /**
     * Convenience method to retrieve an image at a given position.
     *
     * @param position list index
     * @return the Image object, or null if out of bounds
     */
    @Nullable
    public Image getImageAt(int position) {
        if (position < 0 || position >= getItemCount()) return null;
        return getItem(position);
    }

    /**
     * ViewHolder representing a single image grid cell.
     * Holds a thumbnail image and an optional title.
     */
    public static class VH extends RecyclerView.ViewHolder {
        public final ImageView image;
        public final TextView title;

        /**
         * Creates a ViewHolder and initializes UI references.
         *
         * @param itemView the root view of the grid item
         */
        public VH(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.ivThumb);
            title = itemView.findViewById(R.id.tvTitle);
        }
    }
}
