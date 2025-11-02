package com.example.lottary.ui.notifications;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottary.R;
import com.example.lottary.data.FirestoreEventRepository;
import com.example.lottary.ui.browse.BrowseActivity;
import com.example.lottary.ui.events.MyEventsActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity implements NotificationsAdapter.Handler {

    private NotificationsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        RecyclerView rv = findViewById(R.id.recycler);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationsAdapter(this);
        rv.setAdapter(adapter);

        MaterialToolbar tb = findViewById(R.id.toolbar);
        tb.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_optout_all) {
                NotifyPrefs.setAllOptedOut(this, true);
                loadData();
                return true;
            } else if (item.getItemId() == R.id.action_optin_all) {
                NotifyPrefs.setAllOptedOut(this, false);
                loadData();
                return true;
            }
            return false;
        });

        BottomNavigationView nav = findViewById(R.id.bottomNav);
        nav.setSelectedItemId(R.id.nav_notifications);
        nav.setOnItemSelectedListener(i -> {
            int id = i.getItemId();
            if (id == R.id.nav_my_events) {
                startActivity(new Intent(this, MyEventsActivity.class));
                return true;
            } else if (id == R.id.nav_browse) {
                startActivity(new Intent(this, BrowseActivity.class));
                return true;
            } else if (id == R.id.nav_notifications) {
                return true;
            } else if (id == R.id.nav_profile) {
                return true;
            }
            return false;
        });

        loadData();
    }

    private void loadData() {
        if (NotifyPrefs.isAllOptedOut(this)) {
            adapter.submit(new ArrayList<>());
            return;
        }
        String did = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        if (did == null || did.isEmpty()) did = "device_demo";

        FirebaseFirestore.getInstance().collection("notifications")
                .whereEqualTo("deviceId", did)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(100)
                .get()
                .addOnSuccessListener(snap -> {
                    List<NotificationItem> list = new ArrayList<>();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        String orgId = d.getString("organizerId");
                        if (orgId != null && NotifyPrefs.isOrganizerOptedOut(this, orgId)) continue;

                        NotificationItem n = new NotificationItem();
                        n.id = d.getId();
                        n.eventId = d.getString("eventId");
                        n.organizerId = orgId == null ? "" : orgId;
                        n.title = d.getString("title");
                        n.message = d.getString("message");
                        n.type = d.getString("type");
                        Number ts = (Number) d.get("createdAtMs");
                        n.createdAt = ts == null ? 0 : ts.longValue();
                        n.status = d.getString("status");
                        list.add(n);
                    }
                    adapter.submit(list);
                });
    }

    @Override
    public void onSignUp(NotificationItem n) {
        String did = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        if (did == null || did.isEmpty()) did = "device_demo";
        if (n.eventId != null) FirestoreEventRepository.get().signUp(n.eventId, did);
    }

    @Override
    public void onDecline(NotificationItem n) {
        String did = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        if (did == null || did.isEmpty()) did = "device_demo";
        if (n.eventId != null) FirestoreEventRepository.get().decline(n.eventId, did);
    }

    @Override
    public void onOptOutOrganizer(NotificationItem n) {
        if (n.organizerId != null && !n.organizerId.isEmpty()) {
            NotifyPrefs.setOrganizerOptedOut(this, n.organizerId, true);
            loadData();
        }
    }
}
