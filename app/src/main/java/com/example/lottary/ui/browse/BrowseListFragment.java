package com.example.lottary.ui.browse;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottary.R;
import com.example.lottary.data.Event;
import com.example.lottary.data.FirestoreEventRepository;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class BrowseListFragment extends Fragment implements BrowseEventsAdapter.Listener {

    private RecyclerView recyclerView;
    private BrowseEventsAdapter adapter;
    private ListenerRegistration reg;

    private final List<Event> all = new ArrayList<>();
    private String query = "";
    private FilterOptions options = new FilterOptions();

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
        recyclerView = null; adapter = null;
    }

    public void applyFilter(@NonNull String q) { query = q.trim(); applyCurrentFilters(); }
    public void applyOptions(@NonNull FilterOptions opts) { options = opts; applyCurrentFilters(); }

    private void applyCurrentFilters() {
        if (adapter == null) return;

        final String q = query.toLowerCase(Locale.ROOT);
        final FilterOptions fo = options == null ? new FilterOptions() : options;

        List<Event> out = new ArrayList<>();
        for (Event e : all) {
            // 搜索
            if (!q.isEmpty()) {
                String blob = (e.getTitle() + " " + e.getCity() + " " + e.getVenue()).toLowerCase(Locale.ROOT);
                if (!blob.contains(q)) continue;
            }
            // Open only
            if (fo.isOpenOnly() && e.isFull()) continue;

            // Geolocation（开启地理开关的活动）
            if (fo.isGeoOnly() && !e.isGeolocationEnabled()) continue;

            // 日期范围（按 startTimeMs）
            long startMs = e.getStartTimeMs();
            if (fo.getFromDateMs() > 0 && (startMs == 0 || startMs < fo.getFromDateMs())) continue;
            if (fo.getToDateMs()   > 0 && (startMs == 0 || startMs > fo.getToDateMs())) continue;

            // 类型匹配（优先用 type 字段；无字段则根据标题关键词兜底）
            if (!matchesTypes(e, fo.getTypes())) continue;

            out.add(e);
        }
        adapter.submit(out);
    }

    private boolean matchesTypes(@NonNull Event e, @NonNull Set<String> selected) {
        if (selected.isEmpty()) return true;
        String t = e.getType();
        for (String s : selected) if (s.equalsIgnoreCase(t)) return true;

        String title = e.getTitle().toLowerCase(Locale.ROOT);
        Set<String> hit = new HashSet<>();
        if (title.contains("swim") || title.contains("soccer") || title.contains("run") || title.contains("basketball"))
            hit.add("Sports");
        if (title.contains("music") || title.contains("concert") || title.contains("piano") || title.contains("guitar"))
            hit.add("Music");
        if (title.contains("art") || title.contains("craft"))
            hit.add("Arts & Crafts");
        if (title.contains("market") || title.contains("fair") || title.contains("bazaar"))
            hit.add("Market");

        for (String s : selected) if (hit.contains(s)) return true;
        return false;
    }

    @Override public void onEventClick(@NonNull Event e) {
        startActivity(new Intent(requireContext(), EventDetailsActivity.class)
                .putExtra(EventDetailsActivity.EXTRA_EVENT_ID, e.getId()));
    }

    @Override public void onJoinClick(@NonNull Event e) {
        // 设备唯一 id 作为“用户 id”
        String uid = Settings.Secure.getString(requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
        if (uid == null || uid.isEmpty()) uid = "device_demo";

        // 直接写 Firestore，并在回调里弹 Toast
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference ref = db.collection("events").document(e.getId());

        ref.update(
                "waitingList",     FieldValue.arrayUnion(uid),
                "allParticipants", FieldValue.arrayUnion(uid)
        ).addOnSuccessListener(v -> {
            toast("Success! You have joined the waitlist.");
            // 可选：这里你也可以刷新或禁用按钮
            // applyCurrentFilters(); // 若列表状态需要立刻变化
        }).addOnFailureListener(err -> {
            toast("Join failed: " + err.getMessage());
        });
    }

    private void toast(String msg) {
        if (getContext() != null) {
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        }
    }
}