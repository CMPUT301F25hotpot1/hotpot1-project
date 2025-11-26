/*
 * NotificationsActivity.java
 *
 * Screen displaying the notification inbox for the current device.
 * Listens to Firestore "notifications" documents filtered by recipientId,
 * applies opt-out preferences from NotifyPrefs, and allows entrants to
 * accept or decline selected invitations.
 *
 * Outstanding issues:
 * - Identity is based on ANDROID_ID ("deviceId"). If proper user accounts
 *   are introduced, notifications should be keyed by user id instead.
 * - Error handling is limited to Toast messages and does not surface all
 *   failure cases to the user.
 * - Client-side sorting assumes that "sentAt" is present and consistent.
 */

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
import com.example.lottary.ui.profile.MyProfileActivity;
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

/**
 * Activity that shows a list of notifications for the current device.
 *
 * It subscribes to Firestore updates, filters results according to
 * {@link NotifyPrefs}, and forwards user actions (sign up / decline /
 * opt-out) to the appropriate repositories.
 */
public class NotificationsActivity extends AppCompatActivity implements NotificationsAdapter.Listener {

    private RecyclerView recycler;
    private ProgressBar loading;
    private NotificationsAdapter adapter;
    private ListenerRegistration reg;
    private String deviceId;

    /** Latest raw notifications fetched from Firestore before opt-out filtering. */
    private final List<NotificationItem> latest = new ArrayList<>();

    /**
     * Sets up the toolbar, RecyclerView, adapter, and bottom navigation.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        NotifyPrefs.setAllOptedOut(this, false);

        // Use device id as a lightweight identity for notifications.
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        if (deviceId == null || deviceId.isEmpty()) {
            deviceId = "device_demo";
        }

        // Configure top app bar.
        MaterialToolbar top = findViewById(R.id.top_app_bar);
        top.setTitle(getString(R.string.notifications));
        top.setNavigationOnClickListener(v -> finish());

        // Configure loading indicator and list.
        loading = findViewById(R.id.loading);
        recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationsAdapter(this);
        recycler.setAdapter(adapter);

        // Configure bottom navigation.
        wireBottomNav();
    }

    /**
     * Starts listening for Firestore updates when the activity becomes visible.
     */
    @Override
    protected void onStart() {
        super.onStart();
        startListening();
    }

    /**
     * Stops listening to Firestore updates when the activity is no longer visible.
     */
    @Override
    protected void onStop() {
        super.onStop();
        if (reg != null) {
            reg.remove();
            reg = null;
        }
    }

    /**
     * Subscribes to the "notifications" collection for this device and keeps
     * the {@link #latest} list in sync.
     *
     * Uses {@code whereEqualTo("recipientId", deviceId)} and client side
     * sorting to avoid composite index requirements.
     */
    private void startListening() {
        loading.setVisibility(View.VISIBLE);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Remove any previous listener to avoid leaking registrations.
        if (reg != null) {
            reg.remove();
            reg = null;
        }

        reg = db.collection("notifications")
                .whereEqualTo("recipientId", deviceId)
                .limit(200)
                .addSnapshotListener((snap, err) -> {
                    loading.setVisibility(View.GONE);

                    if (err != null) {
                        // On error, show empty state and surface a message.
                        adapter.submit(new ArrayList<>());
                        Toast.makeText(
                                this,
                                "Load notifications failed: " + err.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                        return;
                    }
                    if (snap == null) {
                        adapter.submit(new ArrayList<>());
                        return;
                    }

                    // Map raw documents into model objects.
                    latest.clear();
                    latest.addAll(mapList(snap));

                    // Sort newest first based on sentAtMs.
                    Collections.sort(latest, new Comparator<NotificationItem>() {
                        @Override
                        public int compare(NotificationItem a, NotificationItem b) {
                            return Long.compare(b.sentAtMs, a.sentAtMs);
                        }
                    });

                    // Apply opt-out preferences before showing to the user.
                    submitWithOptOutFilter();
                });
    }

    /**
     * Maps a Firestore {@link QuerySnapshot} to a list of {@link NotificationItem}.
     *
     * @param snap query result containing notification documents
     * @return list of mapped NotificationItem objects
     */
    private List<NotificationItem> mapList(@NonNull QuerySnapshot snap) {
        List<NotificationItem> out = new ArrayList<>();
        for (DocumentSnapshot d : snap.getDocuments()) {
            // Extract fields; missing values are normalized in NotificationItem.
            String id = d.getId();
            String evId = s(d.get("eventId"));
            String evTi = s(d.get("eventTitle"));
            String grp = s(d.get("targetGroup"));
            String type = s(d.get("type"));
            String msg = s(d.get("message"));
            String org = s(d.get("organizerId"));
            Timestamp ts = d.getTimestamp("sentAt");
            long when = ts == null ? 0L : ts.toDate().getTime();

            out.add(new NotificationItem(id, evId, grp, type, msg, when, evTi, org));
        }
        return out;
    }

    /**
     * Converts a potentially null object to a non-null string.
     */
    private static String s(Object o) {
        return o == null ? "" : o.toString();
    }

    /**
     * Applies opt-out preferences from {@link NotifyPrefs} to the latest list
     * and submits the filtered result to the adapter.
     */
    private void submitWithOptOutFilter() {
        // Global opt-out hides all notifications from the inbox.
        if (NotifyPrefs.isAllOptedOut(this)) {
            adapter.submit(new ArrayList<>());
            return;
        }

        List<NotificationItem> filtered = new ArrayList<>();
        for (NotificationItem n : latest) {
            // Skip notifications from organizers that have been muted.
            if (n.organizerId != null
                    && !n.organizerId.isEmpty()
                    && NotifyPrefs.isOrganizerOptedOut(this, n.organizerId)) {
                continue;
            }
            filtered.add(n);
        }
        adapter.submit(filtered);
    }


    /**
     * Handles the "Sign Up" action for a notification representing a successful
     * lottery selection.
     *
     * Calls the event repository, marks the notification as read,
     * and navigates to {@link MyEventsActivity} focusing on the event.
     *
     * @param item the notification that triggered the action
     */
    @Override
    public void onSignUp(@NonNull NotificationItem item) {
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
                        Toast.makeText(
                                this,
                                "Sign up failed: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show());
    }

    /**
     * Handles the "Decline" action for a selected notification.
     *
     * Calls the event repository to decline and marks the notification as read.
     *
     * @param item the notification that triggered the action
     */
    @Override
    public void onDecline(@NonNull NotificationItem item) {
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
                        Toast.makeText(
                                this,
                                "Decline failed: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show());
    }

    /**
     * Displays the overflow menu for a notification row and handles opt-in/opt-out
     * and reset actions for notification preferences.
     *
     * @param anchor view used as the popup anchor
     * @param item   notification associated with this menu
     */
    @Override
    public void onOverflow(@NonNull View anchor, @NonNull NotificationItem item) {
        PopupMenu pm = new PopupMenu(this, anchor);
        pm.getMenuInflater().inflate(R.menu.menu_notifications_overflow, pm.getMenu());

        // Toggle visibility of global opt-out / opt-in based on current state.
        boolean allMuted = NotifyPrefs.isAllOptedOut(this);
        pm.getMenu().findItem(R.id.action_optout_all).setVisible(!allMuted);
        pm.getMenu().findItem(R.id.action_optin_all).setVisible(allMuted);

        // Configure organizer specific options.
        MenuItem outOrg = pm.getMenu().findItem(R.id.action_optout_org);
        MenuItem inOrg = pm.getMenu().findItem(R.id.action_optin_org);
        if (item.organizerId == null || item.organizerId.isEmpty()) {
            // No organizer id: hide organizer specific menu items.
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
                // Mute all notifications.
                NotifyPrefs.setAllOptedOut(this, true);
                Toast.makeText(this, "Opted out of all notifications", Toast.LENGTH_SHORT).show();
                submitWithOptOutFilter();
                return true;
            } else if (id == R.id.action_optin_all) {
                // Unmute all notifications.
                NotifyPrefs.setAllOptedOut(this, false);
                Toast.makeText(this, "Opted in to all notifications", Toast.LENGTH_SHORT).show();
                submitWithOptOutFilter();
                return true;
            } else if (id == R.id.action_optout_org
                    && item.organizerId != null
                    && !item.organizerId.isEmpty()) {
                // Mute notifications from this organizer only.
                NotifyPrefs.setOrganizerOptedOut(this, item.organizerId, true);
                Toast.makeText(this, "Opted out from this organizer", Toast.LENGTH_SHORT).show();
                submitWithOptOutFilter();
                return true;
            } else if (id == R.id.action_optin_org
                    && item.organizerId != null
                    && !item.organizerId.isEmpty()) {
                // Unmute notifications from this organizer.
                NotifyPrefs.setOrganizerOptedOut(this, item.organizerId, false);
                Toast.makeText(this, "Opted in to this organizer", Toast.LENGTH_SHORT).show();
                submitWithOptOutFilter();
                return true;
            } else if (id == R.id.action_reset_mute) {
                // Clear all mute settings.
                NotifyPrefs.resetAll(this);
                Toast.makeText(this, "Mute settings reset", Toast.LENGTH_SHORT).show();
                submitWithOptOutFilter();
                return true;
            }
            return false;
        });

        pm.show();
    }

    /**
     * Marks the notification as read in Firestore and records an "actedAt" timestamp.
     *
     * @param notifId id of the notification document to update
     */
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

    /**
     * Wires up the bottom navigation bar to switch between main sections
     * of the app: Browse, My Events, Notifications, and Profile.
     */
    private void wireBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottomNav);
        if (nav == null) return;

        nav.setSelectedItemId(R.id.nav_notifications);

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_notifications) {
                // Already on this screen.
                return true;
            }

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
                startActivity(new Intent(this, MyProfileActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }

            return false;
        });
    }
}
