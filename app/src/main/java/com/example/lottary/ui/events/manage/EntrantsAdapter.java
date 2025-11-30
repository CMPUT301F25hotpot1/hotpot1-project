package com.example.lottary.ui.events.manage;

import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottary.R;

import java.util.ArrayList;
import java.util.List;

/** Tiny adapter for entrants; avatar is placeholder circle, bell icon is not interactive for now. */
public class EntrantsAdapter extends RecyclerView.Adapter<EntrantsAdapter.VH> {
    private final List<EntrantsListFragment.Row> items = new ArrayList<>();
    public void submit(List<EntrantsListFragment.Row> list){ items.clear(); items.addAll(list); notifyDataSetChanged(); }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int v){ return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_entrant, p,false)); }
    @Override public void onBindViewHolder(@NonNull VH h, int pos){
        EntrantsListFragment.Row r = items.get(pos);
        h.name.setText(r.id); h.status.setText(r.status);
    }
    @Override public int getItemCount(){ return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final TextView name, status; final ImageView bell;
        VH(@NonNull View v){ super(v); name=v.findViewById(R.id.txt_name); status=v.findViewById(R.id.txt_status); bell=v.findViewById(R.id.img_bell); }
    }
}
