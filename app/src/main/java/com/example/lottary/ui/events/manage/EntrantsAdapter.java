package com.example.lottary.ui.events.manage;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottary.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntrantsAdapter extends RecyclerView.Adapter<EntrantsAdapter.VH> {

    public static class Row {
        public final String deviceId;
        public final String status;

        public Row(@NonNull String deviceId, @NonNull String status) {
            this.deviceId = deviceId;
            this.status = status;
        }
    }

    private final List<Row> items = new ArrayList<>();

    private final Map<String, String> nameCache = new HashMap<>();


    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void submit(List<Row> rows) {
        items.clear();
        nameCache.clear();
        if (rows != null) items.addAll(rows);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_entrant, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Row row = items.get(position);


        holder.txtName.setText(row.deviceId);
        holder.txtStatus.setText(row.status);

        holder.btnViewLogs.setVisibility(View.GONE);
        holder.btnCancel.setVisibility(View.GONE);


        String cached = nameCache.get(row.deviceId);
        if (!TextUtils.isEmpty(cached)) {
            holder.txtName.setText(cached);
            return;
        }


        db.collection("users")
                .whereEqualTo("userDeviceId", row.deviceId)
                .limit(1)
                .get()
                .addOnSuccessListener((QuerySnapshot qs) -> {
                    if (qs == null || qs.isEmpty()) return;

                    DocumentSnapshot snap = qs.getDocuments().get(0);
                    String name = snap.getString("name");
                    if (TextUtils.isEmpty(name)) {
                        name = snap.getString("fullName");
                    }
                    if (TextUtils.isEmpty(name)) {
                        name = snap.getString("username");
                    }

                    if (TextUtils.isEmpty(name)) {
                        return;
                    }

                    nameCache.put(row.deviceId, name);


                    int adapterPos = holder.getBindingAdapterPosition();
                    if (adapterPos == RecyclerView.NO_POSITION) return;
                    Row current = items.get(adapterPos);
                    if (row.deviceId.equals(current.deviceId)) {
                        holder.txtName.setText(name);
                    }
                });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView txtName;
        final TextView txtStatus;
        final ImageView btnViewLogs;
        final ImageView btnCancel;

        VH(@NonNull View itemView) {
            super(itemView);
            txtName     = itemView.findViewById(R.id.txt_name);
            txtStatus   = itemView.findViewById(R.id.txt_status);
            btnViewLogs = itemView.findViewById(R.id.btn_view_logs);
            btnCancel   = itemView.findViewById(R.id.btn_cancel);
        }
    }
}
