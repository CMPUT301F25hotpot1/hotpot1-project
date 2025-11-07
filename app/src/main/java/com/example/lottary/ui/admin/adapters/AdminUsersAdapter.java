package com.example.lottary.ui.admin.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottary.R;
import com.example.lottary.data.User;

public class AdminUsersAdapter extends ListAdapter<User, AdminUsersAdapter.ViewHolder> {

    public interface UserClickListener {
        void onViewLogs(User u);
        void onRemoveUser(User u);
    }

    private final UserClickListener listener;

    public AdminUsersAdapter(UserClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<User> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<User>() {
                @Override
                public boolean areItemsTheSame(@NonNull User oldItem, @NonNull User newItem) {
                    return oldItem.getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull User oldItem, @NonNull User newItem) {
                    return oldItem.getName().equals(newItem.getName());
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_user_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView avatar, btnLogs, btnRemove;
        TextView name;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            avatar = itemView.findViewById(R.id.admin_user_avatar);
            name   = itemView.findViewById(R.id.admin_user_name);
            btnLogs = itemView.findViewById(R.id.btn_view_logs);
            btnRemove = itemView.findViewById(R.id.btn_remove_user);
        }

        void bind(User u) {
            name.setText(u.getName());   // ✅ 终于显示名字了

            // ✅ 查看 Logs
            btnLogs.setOnClickListener(v -> listener.onViewLogs(u));

            // ✅ 删除用户
            btnRemove.setOnClickListener(v -> listener.onRemoveUser(u));
        }
    }
}
