package com.example.lottary.ui.events.manage;

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
import com.example.lottary.data.FirestoreEventRepository;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class EntrantsListFragment extends Fragment {

    private static final String ARG_EVENT_ID  = "event_id";
    private static final String ARG_TAB_INDEX = "tab_index";

    public static EntrantsListFragment newInstance(@NonNull String eventId, int tabIndex) {
        Bundle b = new Bundle();
        b.putString(ARG_EVENT_ID, eventId);
        b.putInt(ARG_TAB_INDEX, tabIndex);
        EntrantsListFragment f = new EntrantsListFragment();
        f.setArguments(b);
        return f;
    }

    private String eventId = "";
    private int tabIndex = 0;

    private RecyclerView recycler;
    private EntrantsAdapter adapter;
    private ListenerRegistration reg;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            eventId  = args.getString(ARG_EVENT_ID, "");
            tabIndex = args.getInt(ARG_TAB_INDEX, 0);
        }
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View v = inflater.inflate(R.layout.fragment_manage_entrants_list, container, false);
        recycler = v.findViewById(R.id.recycler_entrants);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new EntrantsAdapter();
        recycler.setAdapter(adapter);
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        attach();
    }

    @Override
    public void onStop() {
        super.onStop();
        detach();
    }

    private void attach() {
        detach();
        if (eventId == null || eventId.isEmpty()) return;

        reg = FirestoreEventRepository.get()
                .listenEvent(eventId, this::onEventChanged);
    }

    private void detach() {
        if (reg != null) {
            reg.remove();
            reg = null;
        }
    }

    private void onEventChanged(@Nullable DocumentSnapshot d) {
        if (!isAdded()) return;

        if (d == null || !d.exists()) {
            adapter.submit(new ArrayList<>());
            return;
        }

        List<String> waiting   = objToStringList(d.get("waitingList"));
        List<String> chosen    = objToStringList(d.get("chosen"));
        List<String> signed    = objToStringList(d.get("signedUp"));
        List<String> cancelled = objToStringList(d.get("cancelled"));

        List<EntrantsAdapter.Row> rows = new ArrayList<>();

        switch (tabIndex) {
            case 1: // Chosen
                for (String id : chosen) {
                    rows.add(new EntrantsAdapter.Row(id, "Chosen"));
                }
                break;

            case 2: // Signed-Up
                for (String id : signed) {
                    rows.add(new EntrantsAdapter.Row(id, "Signed-Up"));
                }
                break;

            case 3: // Cancelled
                for (String id : cancelled) {
                    rows.add(new EntrantsAdapter.Row(id, "Cancelled"));
                }
                break;

            default: // All, grouped by status
                for (String id : waiting) {
                    rows.add(new EntrantsAdapter.Row(id, "Waiting"));
                }
                for (String id : chosen) {
                    rows.add(new EntrantsAdapter.Row(id, "Chosen"));
                }
                for (String id : signed) {
                    rows.add(new EntrantsAdapter.Row(id, "Signed-Up"));
                }
                for (String id : cancelled) {
                    rows.add(new EntrantsAdapter.Row(id, "Cancelled"));
                }
                break;
        }

        adapter.submit(rows);
    }

    private static List<String> objToStringList(Object obj) {
        List<String> out = new ArrayList<>();
        if (obj instanceof List<?>) {
            for (Object e : (List<?>) obj) {
                if (e != null) out.add(e.toString());
            }
        }
        return out;
    }
}
