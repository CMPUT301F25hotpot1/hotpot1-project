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

public class ImageGridAdapter extends ListAdapter<Image, ImageGridAdapter.VH> {

    /** 单击回调（保持原有 API 不变） */
    public interface OnItemClick { void onClick(@NonNull Image image); }

    /** 🆕 可选：长按回调 */
    public interface OnItemLongClick { void onLongClick(@NonNull Image image); }

    private final OnItemClick click;
    @Nullable private OnItemLongClick longClick; // 可选

    public ImageGridAdapter(OnItemClick click) {
        super(DIFF);
        this.click = click;
        setHasStableIds(false);
    }

    /** 🆕 供外部设置长按回调；不设置则无长按行为 */
    public void setOnItemLongClick(@Nullable OnItemLongClick l) {
        this.longClick = l;
    }

    private static final DiffUtil.ItemCallback<Image> DIFF = new DiffUtil.ItemCallback<Image>() {
        @Override public boolean areItemsTheSame(@NonNull Image o, @NonNull Image n) {
            String oid = o.getId()==null? "": o.getId();
            String nid = n.getId()==null? "": n.getId();
            return oid.equals(nid);
        }
        @Override public boolean areContentsTheSame(@NonNull Image o, @NonNull Image n) {
            String ou = o.getUrl()==null?"":o.getUrl();
            String nu = n.getUrl()==null?"":n.getUrl();
            String ot = o.getTitle()==null?"":o.getTitle();
            String nt = n.getTitle()==null?"":n.getTitle();
            long oa = o.getCreatedAt()==null?0:o.getCreatedAt().getSeconds();
            long na = n.getCreatedAt()==null?0:n.getCreatedAt().getSeconds();
            return ou.equals(nu) && ot.equals(nt) && oa==na;
        }
    };

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image_grid, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Image img = getItem(position);

        // 无论有无 url 都显示方形占位框，url 存在则加载
        Glide.with(h.image.getContext())
                .load(img.getUrl()==null || img.getUrl().isEmpty() ? null : img.getUrl())
                .placeholder(R.drawable.placeholder_square)
                .centerCrop()
                .into(h.image);

        // 标题可选
        if (img.getTitle()!=null && !img.getTitle().isEmpty()) {
            h.title.setVisibility(View.VISIBLE);
            h.title.setText(img.getTitle());
        } else {
            h.title.setVisibility(View.GONE);
        }

        // 单击
        h.itemView.setOnClickListener(v -> {
            if (click != null) click.onClick(img);
        });

        // 🆕 长按（仅当外部设置了回调）
        h.itemView.setOnLongClickListener(v -> {
            if (longClick != null) {
                longClick.onLongClick(img);
                return true;
            }
            return false;
        });
    }

    public static class VH extends RecyclerView.ViewHolder {
        public final ImageView image;
        public final TextView title;
        public VH(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.ivThumb);
            title = itemView.findViewById(R.id.tvTitle);
        }
    }
}
