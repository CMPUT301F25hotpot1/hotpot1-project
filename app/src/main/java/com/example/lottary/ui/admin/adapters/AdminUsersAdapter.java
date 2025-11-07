package com.example.lottary.ui.admin.adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottary.R;
import com.example.lottary.data.User;

public class AdminUsersAdapter extends ListAdapter<User, AdminUsersAdapter.ViewHolder> {

    public interface OnRemoveUser {
        void onRemove(User user);
    }

    private final OnRemoveUser callback;

    public AdminUsersAdapter(OnRemoveUser callback) {
        super(DIFF_CALLBACK);
        this.callback = callback;
    }

    private static final DiffUtil.ItemCallback<User> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<User>() {
                @Override
                public boolean areItemsTheSame(@NonNull User oldItem, @NonNull User newItem) {
                    // ✅ 用 deviceID 作为唯一标识符（你已有的字段）
                    return oldItem.getDeviceID().equals(newItem.getDeviceID());
                }

                @Override
                public boolean areContentsTheSame(@NonNull User oldItem, @NonNull User newItem) {
                    // ✅ 用 name + email 来比较内容
                    return oldItem.getName().equals(newItem.getName())
                            && oldItem.getEmail().equals(newItem.getEmail());
                }
            };

    @NonNull
    @Override
    public AdminUsersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_user_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminUsersAdapter.ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView role;   // 我们用 email 替代 role
        ImageButton removeBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.admin_user_name);
            role = itemView.findViewById(R.id.admin_user_role);
            removeBtn = itemView.findViewById(R.id.admin_user_remove);
        }

        void bind(User u) {
            name.setText(u.getName());

            // ✅ 因为没有 role 字段，我们显示 email，当作“用户信息”
            role.setText(u.getEmail());

            removeBtn.setOnClickListener(v -> callback.onRemove(u));
        }
    }
}
