package com.example.lottary.ui.events;

import android.content.Intent;
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
import com.example.lottary.ui.events.edit.EditEventActivity;
import com.example.lottary.ui.events.manage.ManageEventActivity;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * EventsListFragment
 *
 * Purpose:
 * Fragment that displays a list of events either created by the current device (organizer view)
 * or joined by the user (entrant view). It listens to Firestore for live updates, maintains an
 * in-memory list, and exposes a text filtering API used by the parent activity.
 */
public class EventsListFragment extends Fragment implements EventsAdapter.Listener {

    private static final String ARG_SHOW_CREATED = "ARG_SHOW_CREATED";

    public static EventsListFragment newInstance(boolean showCreated) {
        Bundle b = new Bundle();
        b.putBoolean(ARG_SHOW_CREATED, showCreated);
        EventsListFragment f = new EventsListFragment();
        f.setArguments(b);
        return f;
    }

    private boolean showCreated = false;
    private RecyclerView recyclerView;
    private EventsAdapter adapter;
    private ListenerRegistration reg;

    private final List<Event> all = new ArrayList<>();
    private String query = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) showCreated = args.getBoolean(ARG_SHOW_CREATED, false);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_events_list, container, false);
        recyclerView = v.findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new EventsAdapter(this);
        recyclerView.setAdapter(adapter);
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (reg != null) {
            reg.remove();
            reg = null;
        }
    }

    private void startListening() {
        if (reg != null) { reg.remove(); reg = null; }

        String did = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
        if (did == null || did.isEmpty()) did = "device_demo";

        if (showCreated) {
            reg = FirestoreEventRepository.get().listenCreatedByDevice(did, items -> {
                all.clear();
                all.addAll(items);
                applyCurrentFilters();
            });
        } else {
            reg = FirestoreEventRepository.get().listenJoined(did, items -> {
                all.clear();
                all.addAll(items);
                applyCurrentFilters();
            });
        }
    }

    public void applyFilter(@NonNull String q) {
        query = q.trim();
        applyCurrentFilters();
    }

    private void applyCurrentFilters() {
        if (adapter == null) return;
        final String q = query.toLowerCase(Locale.ROOT);

        List<Event> out = new ArrayList<>();
        for (Event e : all) {
            if (!q.isEmpty()) {
                String blob = (e.getTitle() + " " + e.getCity() + " " + e.getVenue())
                        .toLowerCase(Locale.ROOT);
                if (!blob.contains(q)) continue;
            }
            out.add(e);
        }
        adapter.submit(out);
    }

    @Override
    public void onManage(@NonNull Event e) {
        Intent i = new Intent(requireContext(), ManageEventActivity.class);
        // ✅ 用 ManageEventActivity 自己定义的 EXTRA_EVENT_ID 传过去
        i.putExtra(ManageEventActivity.EXTRA_EVENT_ID, e.getId());
        startActivity(i);
    }

    @Override
    public void onEdit(@NonNull Event e) {
        Intent i = new Intent(requireContext(), EditEventActivity.class);
        i.putExtra(EditEventActivity.EXTRA_EVENT_ID, e.getId());
        startActivity(i);
    }
}
