package com.example.lottary.ui.browse;

import android.os.Bundle;
import android.provider.Settings;
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

public class BrowseListFragment extends Fragment implements BrowseEventsAdapter.Listener {

    private RecyclerView recyclerView;
    private BrowseEventsAdapter adapter;
    private ListenerRegistration reg;

    private final List<Event> all = new ArrayList<>();
    private String query = "";
    private FilterOptions options;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_browse_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        recyclerView = v.findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new BrowseEventsAdapter(this);
        recyclerView.setAdapter(adapter);

        reg = FirestoreEventRepository.get().listenRecentCreated(items -> {
            all.clear();
            all.addAll(items);
            applyCurrentFilters();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (reg != null) { reg.remove(); reg = null; }
        recyclerView = null;
        adapter = null;
    }


    public void applyFilter(@NonNull String q) {
        query = q.trim();
        applyCurrentFilters();
    }

    public void applyOptions(@NonNull FilterOptions opts) {
        options = opts;
        applyCurrentFilters();
    }

    private void applyCurrentFilters() {
        if (adapter == null) return;

        final String q = query.toLowerCase(Locale.ROOT);
        List<Event> out = new ArrayList<>();
        for (Event e : all) {
            if (!q.isEmpty()) {
                String blob = (e.getTitle() + " " + e.getCity() + " " + e.getVenue()).toLowerCase(Locale.ROOT);
                if (!blob.contains(q)) continue;
            }

            out.add(e);
        }
        adapter.submit(out);
    }


    @Override
    public void onEventClick(@NonNull Event e) {
        EventDetailsActivity.open(requireContext(), e.getId());
    }

    @Override
    public void onJoinClick(@NonNull Event e) {
        String did = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        if (did == null || did.isEmpty()) did = "device_demo";
        FirestoreEventRepository.get().joinWaitingList(e.getId(), did);
    }
}
