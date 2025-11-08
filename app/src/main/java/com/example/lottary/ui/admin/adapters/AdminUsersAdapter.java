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

/**
 * Adapter used in the Admin Users screen for displaying a list of User objects.
 * Each row shows a user avatar, username, and actions to view logs or remove the user.
 */
public class AdminUsersAdapter extends ListAdapter<User, AdminUsersAdapter.ViewHolder> {

    /**
     * Listener that exposes callbacks for user-related row actions.
     */
    public interface UserClickListener {
        /**
         * Triggered when the "View Logs" button is pressed.
         *
         * @param u the selected User
         */
        void onViewLogs(User u);

        /**
         * Triggered when the "Remove User" button is pressed.
         *
         * @param u the selected User
         */
        void onRemoveUser(User u);
    }

    private final UserClickListener listener;

    /**
     * Creates a new AdminUsersAdapter.
     *
     * @param listener callback invoked for user actions within each row
     */
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

    /**
     * ViewHolder responsible for displaying a single user row.
     * Contains user avatar, name, and quick action buttons.
     */
    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView avatar, btnLogs, btnRemove;
        TextView name;

        /**
         * Constructs the ViewHolder and initializes all row views.
         *
         * @param itemView the row layout root
         */
        ViewHolder(@NonNull View itemView) {
            super(itemView);

            avatar = itemView.findViewById(R.id.admin_user_avatar);
            name = itemView.findViewById(R.id.admin_user_name);
            btnLogs = itemView.findViewById(R.id.btn_view_logs);
            btnRemove = itemView.findViewById(R.id.btn_remove_user);
        }

        /**
         * Binds the given User object to the row's UI components.
         *
         * @param u the user to display
         */
        void bind(User u) {
            name.setText(u.getName());
            btnLogs.setOnClickListener(v -> listener.onViewLogs(u));
            btnRemove.setOnClickListener(v -> listener.onRemoveUser(u));
        }
    }
}
