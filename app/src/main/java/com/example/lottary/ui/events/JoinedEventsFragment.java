package com.example.lottary.ui.events;

import android.app.AlertDialog;
import android.os.Bundle;
import android.provider.Settings;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Entrant 视角的“Joined Events”
 * - 四路监听：waitingList/chosen/signedUp/cancelled
 * - 合并去重并渲染状态与按钮
 * - 保障：仅在 Fragment 已 attach 时访问 Context / 调用 reload
 */
public class JoinedEventsFragment extends Fragment {

    private RecyclerView recycler;
    private EntrantEventsAdapter adapter;

    // 事件池：eventId -> Event
    private final Map<String, Event> pool = new HashMap<>();
    // 用户所在集合标记
    private final Map<String, Boolean> inWaiting   = new HashMap<>();
    private final Map<String, Boolean> inChosen    = new HashMap<>();
    private final Map<String, Boolean> inSigned    = new HashMap<>();
    private final Map<String, Boolean> inCancelled = new HashMap<>();

    private String currentQuery = "";

    private ListenerRegistration regWaiting, regChosen, regSigned, regCancelled;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_events_list, container, false);
        recycler = v.findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new EntrantEventsAdapter(new EntrantEventsAdapter.Listener() {
            @Override public void onSignUp(@NonNull Event e) {
                String did = safeDeviceId();
                if (did == null) return;
                FirestoreEventRepository.get().signUp(e.getId(), did)
                        .addOnCompleteListener(t -> reload());
            }
            @Override public void onDecline(@NonNull Event e) {
                String did = safeDeviceId();
                if (did == null) return;
                FirestoreEventRepository.get().decline(e.getId(), did)
                        .addOnCompleteListener(t -> reload());
            }
            @Override public void onLeaveWaitlist(@NonNull Event e) {
                if (!isAdded()) return;
                new AlertDialog.Builder(requireContext())
                        .setTitle("Leave Waiting List")
                        .setMessage("Are you sure that you want to leave the waiting list of this event?")
                        .setPositiveButton("Yes", (d, w) -> {
                            String did = safeDeviceId();
                            if (did == null) return;
                            FirestoreEventRepository.get().leaveWaitingList(e.getId(), did)
                                    .addOnCompleteListener(t -> reload());
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });
        recycler.setAdapter(adapter);

        // 初次进入不强刷，由 onResume 触发（确保已 attach）
        return v;
    }

    @Override public void onResume() {
        super.onResume();
        reload();
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        detach();
    }

    /** Activity 的搜索栏会调用这个方法 */
    public void applyFilter(String q) {
        currentQuery = q == null ? "" : q.trim().toLowerCase();
        render();
    }

    /** 仅在已 attach 时才进行监听与渲染 */
    public void reload() {
        if (!isAdded() || getContext() == null) return;
        detach();
        attach();
    }

    // ============ 监听与合并 ============

    private void attach() {
        String did = safeDeviceId();
        if (did == null) return;

        regWaiting = FirestoreEventRepository.get().listenJoined(did, events -> {
            mark(inWaiting, events);
            addToPool(events);
            render();
        });

        regChosen = FirestoreEventRepository.get().listenChosen(did, events -> {
            mark(inChosen, events);
            addToPool(events);
            render();
        });

        regSigned = FirestoreEventRepository.get().listenSigned(did, events -> {
            mark(inSigned, events);
            addToPool(events);
            render();
        });

        regCancelled = FirestoreEventRepository.get().listenCancelled(did, events -> {
            mark(inCancelled, events);
            addToPool(events);
            render();
        });
    }

    private void detach() {
        if (regWaiting   != null) { regWaiting.remove();   regWaiting = null; }
        if (regChosen    != null) { regChosen.remove();    regChosen = null; }
        if (regSigned    != null) { regSigned.remove();    regSigned = null; }
        if (regCancelled != null) { regCancelled.remove(); regCancelled = null; }
    }

    private void mark(Map<String, Boolean> bucket, List<Event> list) {
        bucket.clear();
        for (Event e : list) bucket.put(e.getId(), true);
    }

    private void addToPool(List<Event> list) {
        for (Event e : list) pool.put(e.getId(), e);
    }

    private void render() {
        if (!isAdded()) return;
        List<EntrantRow> rows = new ArrayList<>();

        for (Map.Entry<String, Event> kv : pool.entrySet()) {
            String id = kv.getKey();
            Event e = kv.getValue();

            if (!TextUtils.isEmpty(currentQuery)) {
                String hay = (e.getTitle() + " " + e.getCity() + " " + e.getVenue()).toLowerCase();
                if (!hay.contains(currentQuery)) continue;
            }

            String status;
            if (inCancelled.containsKey(id)) {
                status = "Not Selected";
            } else if (inChosen.containsKey(id) || inSigned.containsKey(id)) {
                status = "Selected";
            } else if (inWaiting.containsKey(id)) {
                status = "Open";
            } else {
                continue; // 不在四类里就不显示
            }

            boolean showSignUp   = "Selected".equals(status) && withinRegisterWindow(e) && !e.isFull();
            boolean showDecline  = "Selected".equals(status);
            boolean showLeave    = "Open".equals(status);

            rows.add(new EntrantRow(e, status, showSignUp, showDecline, showLeave));
        }

        adapter.submit(rows);
    }

    private boolean withinRegisterWindow(@NonNull Event e) {
        long now = System.currentTimeMillis();
        long start = e.getRegisterStartMs();
        long end   = e.getRegisterEndMs();
        if (start <= 0 && end <= 0) return true;
        if (start > 0 && now < start) return false;
        if (end > 0 && now > end) return false;
        return true;
    }

    /** 安全获取 deviceId；未 attach 则返回 null */
    @Nullable
    private String safeDeviceId() {
        if (!isAdded() || getContext() == null) return null;
        return Settings.Secure.getString(
                getContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
    }

    /** UI 行模型（状态与按钮可见性已经计算好） */
    static class EntrantRow {
        final Event event;
        final String status;
        final boolean showSignUp, showDecline, showLeave;
        EntrantRow(Event e, String s, boolean su, boolean de, boolean le) {
            this.event = e; this.status = s;
            this.showSignUp = su; this.showDecline = de; this.showLeave = le;
        }
    }
}
