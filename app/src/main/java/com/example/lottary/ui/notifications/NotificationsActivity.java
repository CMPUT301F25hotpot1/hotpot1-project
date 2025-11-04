package com.example.lottary.ui.notifications;

import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottary.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity implements NotificationsAdapter.Listener {

    private RecyclerView recycler;
    private ProgressBar loading;
    private NotificationsAdapter adapter;
    private ListenerRegistration reg;
    private String deviceId;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        if (deviceId == null || deviceId.isEmpty()) deviceId = "device_demo";

        MaterialToolbar top = findViewById(R.id.top_app_bar);
        top.setTitle("Notifications");
        top.setNavigationOnClickListener(v -> finish());

        loading = findViewById(R.id.loading);
        recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationsAdapter(this);
        recycler.setAdapter(adapter);
    }

    @Override protected void onStart() {
        super.onStart();
        startListening();
    }

    @Override protected void onStop() {
        super.onStop();
        if (reg != null) { reg.remove(); reg = null; }
    }

    private void startListening() {
        loading.setVisibility(View.VISIBLE);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        reg = db.collection("notifications")
                .whereEqualTo("recipientId", deviceId)
                .orderBy("sentAt", Query.Direction.DESCENDING)
                .limit(200)
                .addSnapshotListener((snap, err) -> {
                    loading.setVisibility(View.GONE);
                    if (err != null || snap == null) {
                        adapter.submit(new ArrayList<>());
                        return;
                    }
                    adapter.submit(mapList(snap));
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
            Timestamp ts = d.getTimestamp("sentAt");
            long when = ts == null ? System.currentTimeMillis() : ts.toDate().getTime();
            out.add(new NotificationItem(id, evId, grp, type, msg, when, evTi));
        }
        return out;
    }

    private static String s(Object o){ return o == null ? "" : o.toString(); }

    @Override public void onSignUp(@NonNull NotificationItem item) {
        Toast.makeText(this, "Sign Up clicked (stub)", Toast.LENGTH_SHORT).show();
    }
    @Override public void onDecline(@NonNull NotificationItem item) {
        Toast.makeText(this, "Decline clicked (stub)", Toast.LENGTH_SHORT).show();
    }
    @Override public void onOverflow(@NonNull NotificationItem item) {
        Toast.makeText(this, "Overflow menu (stub)", Toast.LENGTH_SHORT).show();
    }
}
