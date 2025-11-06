package com.example.lottary.ui.notifications;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottary.R;
import com.example.lottary.data.FirestoreEventRepository;
import com.example.lottary.ui.browse.BrowseActivity;
import com.example.lottary.ui.events.MyEventsActivity;
import com.example.lottary.ui.profile.ProfileActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationsActivity extends AppCompatActivity implements NotificationsAdapter.Listener {

    private RecyclerView recycler;
    private ProgressBar loading;
    private NotificationsAdapter adapter;
    private ListenerRegistration reg;
    private String deviceId;

    private final List<NotificationItem> latest = new ArrayList<>();

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        // 避免误触全局静音后一直空白（demo 环境可保留）
        NotifyPrefs.setAllOptedOut(this, false);

        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        if (deviceId == null || deviceId.isEmpty()) deviceId = "device_demo";

        MaterialToolbar top = findViewById(R.id.top_app_bar);
        top.setTitle(getString(R.string.notifications));
        top.setNavigationOnClickListener(v -> finish());

        loading = findViewById(R.id.loading);
        recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationsAdapter(this);
        recycler.setAdapter(adapter);

        wireBottomNav();
    }

    @Override protected void onStart() {
        super.onStart();
        startListening();
    }

    @Override protected void onStop() {
        super.onStop();
        if (reg != null) { reg.remove(); reg = null; }
    }

    /** 不再使用 orderBy("sentAt")，仅用 whereEqualTo 过滤，然后客户端排序，避免复合索引需求 */
    private void startListening() {
        loading.setVisibility(View.VISIBLE);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (reg != null) { reg.remove(); reg = null; }

        reg = db.collection("notifications")
                .whereEqualTo("recipientId", deviceId)
                .limit(200)
                .addSnapshotListener((snap, err) -> {
                    loading.setVisibility(View.GONE);
                    if (err != null) {
                        adapter.submit(new ArrayList<>());
                        Toast.makeText(this, "Load notifications failed: " + err.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (snap == null) {
                        adapter.submit(new ArrayList<>());
                        return;
                    }
                    latest.clear();
                    latest.addAll(mapList(snap));

                    // 客户端按 sentAtMs 降序
                    Collections.sort(latest, new Comparator<NotificationItem>() {
                        @Override public int compare(NotificationItem a, NotificationItem b) {
                            return Long.compare(b.sentAtMs, a.sentAtMs);
                        }
                    });

                    submitWithOptOutFilter();
                });
    }

    private List<NotificationItem> mapList(@NonNull QuerySnapshot snap) {
        List<NotificationItem> out = new ArrayList<>();
        for (DocumentSnapshot d : snap.getDocuments()) {
            String id   = d.getId();
            String evId = s(d.get("eventId"));
            String evTi = s(d.get("eventTitle"));
            String grp  = s(d.get("targetGroup"));
            String type = s(d.get("type"));
            String msg  = s(d.get("message"));
            String org  = s(d.get("organizerId"));
            Timestamp ts = d.getTimestamp("sentAt");
            long when = ts == null ? 0L : ts.toDate().getTime();
            out.add(new NotificationItem(id, evId, grp, type, msg, when, evTi, org));
        }
        return out;
    }

    private static String s(Object o){ return o == null ? "" : o.toString(); }

    private void submitWithOptOutFilter() {
        if (NotifyPrefs.isAllOptedOut(this)) {
            adapter.submit(new ArrayList<>());
            return;
        }
        List<NotificationItem> filtered = new ArrayList<>();
        for (NotificationItem n : latest) {
            if (n.organizerId != null && !n.organizerId.isEmpty()
                    && NotifyPrefs.isOrganizerOptedOut(this, n.organizerId)) {
                continue;
            }
            filtered.add(n);
        }
        adapter.submit(filtered);
    }

    // === Adapter callbacks ===
    @Override public void onSignUp(@NonNull NotificationItem item) {
        if (item.eventId == null || item.eventId.isEmpty()) {
            Toast.makeText(this, "Missing eventId", Toast.LENGTH_SHORT).show();
            return;
        }
        FirestoreEventRepository.get()
                .signUp(item.eventId, deviceId)
                .addOnSuccessListener(v -> {
                    markNotificationRead(item.id);
                    Toast.makeText(this, "Signed up successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, MyEventsActivity.class)
                            .putExtra("focus_event_id", item.eventId));
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Sign up failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    @Override public void onDecline(@NonNull NotificationItem item) {
        if (item.eventId == null || item.eventId.isEmpty()) {
            Toast.makeText(this, "Missing eventId", Toast.LENGTH_SHORT).show();
            return;
        }
        FirestoreEventRepository.get()
                .decline(item.eventId, deviceId)
                .addOnSuccessListener(v -> {
                    markNotificationRead(item.id);
                    Toast.makeText(this, "Declined", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Decline failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    @Override public void onOverflow(@NonNull View anchor, @NonNull NotificationItem item) {
        PopupMenu pm = new PopupMenu(this, anchor);
        pm.getMenuInflater().inflate(R.menu.menu_notifications_overflow, pm.getMenu());

        boolean allMuted = NotifyPrefs.isAllOptedOut(this);
        pm.getMenu().findItem(R.id.action_optout_all).setVisible(!allMuted);
        pm.getMenu().findItem(R.id.action_optin_all).setVisible(allMuted);

        MenuItem outOrg = pm.getMenu().findItem(R.id.action_optout_org);
        MenuItem inOrg  = pm.getMenu().findItem(R.id.action_optin_org);
        if (item.organizerId == null || item.organizerId.isEmpty()) {
            outOrg.setVisible(false);
            inOrg.setVisible(false);
        } else {
            boolean orgMuted = NotifyPrefs.isOrganizerOptedOut(this, item.organizerId);
            outOrg.setVisible(!orgMuted);
            inOrg.setVisible(orgMuted);
        }

        pm.setOnMenuItemClickListener(mi -> {
            int id = mi.getItemId();
            if (id == R.id.action_optout_all) {
                NotifyPrefs.setAllOptedOut(this, true);
                Toast.makeText(this, "Opted out of all notifications", Toast.LENGTH_SHORT).show();
                submitWithOptOutFilter();
                return true;
            } else if (id == R.id.action_optin_all) {
                NotifyPrefs.setAllOptedOut(this, false);
                Toast.makeText(this, "Opted in to all notifications", Toast.LENGTH_SHORT).show();
                submitWithOptOutFilter();
                return true;
            } else if (id == R.id.action_optout_org && item.organizerId != null && !item.organizerId.isEmpty()) {
                NotifyPrefs.setOrganizerOptedOut(this, item.organizerId, true);
                Toast.makeText(this, "Opted out from this organizer", Toast.LENGTH_SHORT).show();
                submitWithOptOutFilter();
                return true;
            } else if (id == R.id.action_optin_org && item.organizerId != null && !item.organizerId.isEmpty()) {
                NotifyPrefs.setOrganizerOptedOut(this, item.organizerId, false);
                Toast.makeText(this, "Opted in to this organizer", Toast.LENGTH_SHORT).show();
                submitWithOptOutFilter();
                return true;
            } else if (id == R.id.action_reset_mute) {
                NotifyPrefs.resetAll(this);
                Toast.makeText(this, "Mute settings reset", Toast.LENGTH_SHORT).show();
                submitWithOptOutFilter();
                return true;
            }
            return false;
        });

        pm.show();
    }

    private void markNotificationRead(@NonNull String notifId) {
        if (notifId.isEmpty()) return;
        Map<String, Object> up = new HashMap<>();
        up.put("read", true);
        up.put("actedAt", Timestamp.now());
        FirebaseFirestore.getInstance()
                .collection("notifications")
                .document(notifId)
                .set(up, SetOptions.merge());
    }

    private void wireBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottomNav);
        if (nav == null) return;
        nav.setSelectedItemId(R.id.nav_notifications);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_notifications) return true;
            if (id == R.id.nav_my_events) {
                startActivity(new Intent(this, MyEventsActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_browse) {
                startActivity(new Intent(this, BrowseActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
    }
}

