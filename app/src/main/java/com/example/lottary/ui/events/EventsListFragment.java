package com.example.lottary.ui.events;

import android.os.Bundle;
import android.text.TextUtils;
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
import com.example.lottary.data.FirestoreEventRepository;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EventsListFragment extends Fragment {
    public static final int MODE_JOINED  = 0;
    public static final int MODE_CREATED = 1;
    private static final String ARG_MODE = "mode";

    private int mode = MODE_CREATED;

    private RecyclerView rv;
    private EventsAdapter adapter;
    private final List<Event> fullData = new ArrayList<>();
    private String currentQuery = "";
    private ListenerRegistration reg;

    public static EventsListFragment newInstance(int mode) {
        Bundle b = new Bundle(); b.putInt(ARG_MODE, mode);
        EventsListFragment f = new EventsListFragment();
        f.setArguments(b);
        return f;
    }

    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) mode = getArguments().getInt(ARG_MODE, MODE_CREATED);
        setRetainInstance(true);
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_events_list, container, false);
        rv = v.findViewById(R.id.recycler);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new EventsAdapter();
        rv.setAdapter(adapter);
        return v;
    }

    @Override public void onStart() {
        super.onStart();
        startListening();
    }

    @Override public void onStop() {
        super.onStop();
        if (reg != null) { reg.remove(); reg = null; }
    }

    private void startListening() {
        if (reg != null) { reg.remove(); reg = null; }
        FirestoreEventRepository repo = FirestoreEventRepository.get();

        if (mode == MODE_CREATED) {
            String deviceId = android.provider.Settings.Secure.getString(
                    requireContext().getContentResolver(),
                    android.provider.Settings.Secure.ANDROID_ID
            );
            if (TextUtils.isEmpty(deviceId)) {
                reg = repo.listenRecentCreated(items -> {
                    fullData.clear(); fullData.addAll(items);
                    applyFilter(currentQuery);
                });
            } else {
                reg = repo.listenCreatedByDevice(deviceId, items -> {
                    fullData.clear(); fullData.addAll(items);
                    applyFilter(currentQuery);
                });
            }
        } else {
            String deviceId = android.provider.Settings.Secure.getString(
                    requireContext().getContentResolver(),
                    android.provider.Settings.Secure.ANDROID_ID
            );
            if (TextUtils.isEmpty(deviceId)) deviceId = "device_demo";

            reg = repo.listenJoined(deviceId, items -> {
                fullData.clear(); fullData.addAll(items);
                applyFilter(currentQuery);
            });
        }
    }

    public void applyFilter(String q) {
        currentQuery = q == null ? "" : q.trim();
        if (TextUtils.isEmpty(currentQuery)) {
            adapter.setData(new ArrayList<>(fullData));
            return;
        }
        String needle = currentQuery.toLowerCase(Locale.ROOT);

        List<Event> filtered = new ArrayList<>();
        for (Event e : fullData) {
            if (contains(e.getTitle(), needle) ||
                    contains(e.getCity(), needle)  ||
                    contains(e.getVenue(), needle)) {
                filtered.add(e);
            }
        }
        adapter.setData(filtered);
    }

    private boolean contains(String src, String needle) {
        return src != null && src.toLowerCase(Locale.ROOT).contains(needle);
    }
}
