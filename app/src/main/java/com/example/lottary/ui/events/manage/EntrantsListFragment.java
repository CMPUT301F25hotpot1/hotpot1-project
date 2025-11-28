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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EntrantsListFragment extends Fragment {

    private static final String ARG_EVENT_ID   = "event_id";
    private static final String ARG_TAB_INDEX  = "tab_index";

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

        Set<String> allIds = new HashSet<>();
        allIds.addAll(waiting);
        allIds.addAll(chosen);
        allIds.addAll(signed);
        allIds.addAll(cancelled);

        List<EntrantsAdapter.Row> rows = new ArrayList<>();

        for (String id : allIds) {
            boolean inWaiting   = waiting.contains(id);
            boolean inChosen    = chosen.contains(id);
            boolean inSigned    = signed.contains(id);
            boolean inCancelled = cancelled.contains(id);

            String status;
            if (inCancelled) {
                status = "Cancelled";
            } else if (inSigned) {
                status = "Signed-Up";
            } else if (inChosen) {
                status = "Chosen";
            } else if (inWaiting) {
                status = "Waiting";
            } else {
                continue;
            }

            switch (tabIndex) {
                case 1: // Chosen Entrants
                    if (!inChosen) continue;
                    break;
                case 2: // Signed-Up Entrants
                    if (!inSigned) continue;
                    break;
                case 3: // Cancelled Entrants
                    if (!inCancelled) continue;
                    break;
                default:
                    break;
            }

            rows.add(new EntrantsAdapter.Row(id, status));
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
