package com.example.lottary.ui.events.manage;

import android.os.Bundle;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import com.example.lottary.R;
import com.example.lottary.data.FirestoreEventRepository;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.*;

/**
 * Shows entrants filtered by bucket. Buckets: 0=All,1=Chosen,2=SignedUp,3=Cancelled.
 */
public class EntrantsListFragment extends Fragment {

    private static final String ARG_EVENT_ID = "event_id";
    private static final String ARG_BUCKET = "bucket";

    public static EntrantsListFragment newInstance(String eventId, int bucket){
        EntrantsListFragment f = new EntrantsListFragment();
        Bundle b = new Bundle(); b.putString(ARG_EVENT_ID, eventId); b.putInt(ARG_BUCKET, bucket);
        f.setArguments(b); return f;
    }

    private String eventId; private int bucket;
    private ListenerRegistration reg; private EntrantsAdapter adapter;

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_entrants_list, container, false);
        RecyclerView rv = root.findViewById(R.id.recycler_entrants);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new EntrantsAdapter(); rv.setAdapter(adapter);

        if (getArguments()!=null){ eventId = getArguments().getString(ARG_EVENT_ID); bucket = getArguments().getInt(ARG_BUCKET,0); }
        reg = FirestoreEventRepository.get().listenEvent(eventId, this::bind);
        return root;
    }

    private void bind(DocumentSnapshot d){
        if (d==null || !d.exists()) { adapter.submit(Collections.emptyList()); return; }
        List<String> chosen = toList(d.get("chosen")), signed = toList(d.get("signedUp")), cancelled = toList(d.get("cancelled")), waiting = toList(d.get("waitingList"));
        List<Row> all = new ArrayList<>();
        for (String id : waiting)  all.add(new Row(id, "Waiting"));
        for (String id : chosen)   all.add(new Row(id, "Chosen"));
        for (String id : signed)   all.add(new Row(id, "Signed Up"));
        for (String id : cancelled)all.add(new Row(id, "Cancelled"));

        List<Row> target;
        switch (bucket){
            case 1: target = filter(all,"Chosen"); break;
            case 2: target = filter(all,"Signed Up"); break;
            case 3: target = filter(all,"Cancelled"); break;
            default: target = all;
        }
        adapter.submit(target);
    }

    private List<Row> filter(List<Row> src, String status){
        List<Row> out = new ArrayList<>(); for (Row r:src) if (r.status.equals(status)) out.add(r); return out;
    }
    private List<String> toList(Object o){ return (o instanceof List)? new ArrayList<>((List<String>)o) : new ArrayList<>(); }

    @Override public void onDestroyView() { if (reg!=null) reg.remove(); super.onDestroyView(); }

    static class Row { final String id; final String status; Row(String id,String status){this.id=id; this.status=status;} }
}
