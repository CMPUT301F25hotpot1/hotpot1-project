package com.example.lottary.ui.browse;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottary.R;
import com.example.lottary.data.Event;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BrowseListFragment extends Fragment {
    private RecyclerView rv;
    private BrowseEventsAdapter adapter;
    private final List<Event> all = new ArrayList<>();
    private String query = "";
    private FilterOptions options = new FilterOptions();
    private ListenerRegistration reg;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_browse_list, container, false);
        rv = v.findViewById(R.id.recycler);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new BrowseEventsAdapter(e -> {
            Intent i = new Intent(requireContext(), EventDetailsActivity.class);
            i.putExtra(EventDetailsActivity.EXTRA_EVENT_ID, e.getId());
            startActivity(i);
        });
        rv.setAdapter(adapter);
        return v;
    }

    @Override public void onStart() {
        super.onStart();
        startListen();
    }

    @Override public void onStop() {
        super.onStop();
        if (reg != null) { reg.remove(); reg = null; }
    }

    public void applyFilter(String q) {
        query = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
        render();
    }

    public void applyOptions(FilterOptions o) {
        options = o == null ? new FilterOptions() : o;
        render();
    }

    private void render() {
        List<Event> flt = new ArrayList<>();
        for (Event e : all) {
            if (options.openOnly && e.isFull()) continue;
            if (!query.isEmpty()) {
                String bundle = (e.getTitle() == null ? "" : e.getTitle()) + " " +
                        (e.getCity() == null ? "" : e.getCity()) + " " +
                        (e.getVenue() == null ? "" : e.getVenue());
                if (!bundle.toLowerCase(Locale.ROOT).contains(query)) continue;
            }
            flt.add(e);
        }
        adapter.submit(flt);
    }

    private void startListen() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        reg = db.collection("events")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50)
                .addSnapshotListener((snap, err) -> {
                    all.clear();
                    if (snap != null) {
                        for (DocumentSnapshot d : snap.getDocuments()) {
                            String id = d.getId();
                            String title = d.getString("title");
                            String city = d.getString("city");
                            String venue = d.getString("venue");
                            boolean full = Boolean.TRUE.equals(d.getBoolean("full"));
                            Timestamp ts = d.getTimestamp("startTime");
                            String pretty = ts == null ? "" : DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(ts.toDate());
                            all.add(new Event(id, title, city, venue, pretty, full));
                        }
                    }
                    render();
                });
    }
}



