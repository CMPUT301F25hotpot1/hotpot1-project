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

/**
 * BrowseListFragment
 *
 * Role / Purpose:
 * - Displays a scrollable list of events for the Browse tab using a RecyclerView.
 * - Subscribes to Firestore for "recently created" events and keeps an in-memory list {@code all}.
 * - Applies keyword and structured filters (open only / geolocation / date range / types) on the UI thread.
 * - Forwards item interactions (open details / join waitlist) to the appropriate destinations.
 *
 * Lifecycle:
 * - Firestore listener is registered in {@link #onViewCreated(View, Bundle)} and removed in {@link #onDestroyView()}.
 * - Adapter is bound/unbound together with the fragment's view lifecycle (not the activity lifecycle).
 *
 * Threading:
 * - All list mutations and adapter submissions occur on the main thread.
 * - Firestore callbacks are delivered on the main thread by default in Android.
 */
public class BrowseListFragment extends Fragment implements BrowseEventsAdapter.Listener {

    /** RecyclerView that renders the event list. */
    private RecyclerView recyclerView;
    /** Adapter responsible for binding {@link Event} items to cards. */
    private BrowseEventsAdapter adapter;
    /** Active Firestore subscription; must be removed in onDestroyView() to avoid leaks. */
    private ListenerRegistration reg;

    /** Unfiltered in-memory snapshot from Firestore. */
    private final List<Event> all = new ArrayList<>();
    /** Current free-text query (lowercased/trimmed before use). */
    private String query = "";
    /** Current structured filter options provided by the filter sheet. */
    private FilterOptions options = new FilterOptions();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the fragment layout that contains the RecyclerView with id @id/recycler.
        return inflater.inflate(R.layout.fragment_events_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        // Recycler setup (linear vertical list).
        recyclerView = v.findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new BrowseEventsAdapter(this);
        recyclerView.setAdapter(adapter);

        // Subscribe to Firestore for recently created events.
        // The callback replaces the in-memory list and reapplies filters so the UI stays in sync.
        reg = FirestoreEventRepository.get().listenRecentCreated(items -> {
            all.clear();
            all.addAll(items);
            applyCurrentFilters();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Detach Firestore listener and release view references to prevent memory leaks.
        if (reg != null) { reg.remove(); reg = null; }
        recyclerView = null; adapter = null;
    }

    /** Apply a new free-text query; re-filters the in-memory list. */
    public void applyFilter(@NonNull String q) { query = q.trim(); applyCurrentFilters(); }

    /** Apply new structured options (open/geo/date/types); re-filters the in-memory list. */
    public void applyOptions(@NonNull FilterOptions opts) { options = opts; applyCurrentFilters(); }

    /**
     * Recompute the filtered result from {@link #all} using the current {@link #query} and {@link #options},
     * then submit the resulting list to the adapter.
     */
    private void applyCurrentFilters() {
        if (adapter == null) return;

        final String q = query.toLowerCase(Locale.ROOT);
        final FilterOptions fo = options == null ? new FilterOptions() : options;

        List<Event> out = new ArrayList<>();
        for (Event e : all) {
            // Keyword search (title/city/venue).
            if (!q.isEmpty()) {
                String blob = (e.getTitle() + " " + e.getCity() + " " + e.getVenue()).toLowerCase(Locale.ROOT);
                if (!blob.contains(q)) continue;
            }
            // "Open only" filter: exclude full events.
            if (fo.isOpenOnly() && e.isFull()) continue;

            // Geolocation-enabled only.
            if (fo.isGeoOnly() && !e.isGeolocationEnabled()) continue;

            // Date range filter based on startTimeMs.
            long startMs = e.getStartTimeMs();
            if (fo.getFromDateMs() > 0 && (startMs == 0 || startMs < fo.getFromDateMs())) continue;
            if (fo.getToDateMs()   > 0 && (startMs == 0 || startMs > fo.getToDateMs())) continue;

            // Type matching: prefer the explicit type field; fall back to keywords in the title.
            if (!matchesTypes(e, fo.getTypes())) continue;

            out.add(e);
        }
        // Submit the filtered snapshot to the adapter.
        adapter.submit(out);
    }

    /**
     * Returns true if the event matches any of the selected types.
     * Strategy:
     * 1) If no type is selected, it's a match.
     * 2) If event has a concrete {@code type}, compare case-insensitively.
     * 3) Otherwise, infer a coarse type bucket from title keywords as a fallback.
     */
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

    /** Open the details screen for the tapped event, passing its id as an extra. */
    @Override public void onEventClick(@NonNull Event e) {
        startActivity(new Intent(requireContext(), EventDetailsActivity.class)
                .putExtra(EventDetailsActivity.EXTRA_EVENT_ID, e.getId()));
    }

    /**
     * Attempt to join the waitlist for the selected event.
     * Implementation notes:
     * - Uses the device ANDROID_ID as a lightweight unique user identifier (demo purpose).
     * - Updates two array fields in Firestore (waitingList, allParticipants) atomically.
     * - Shows a short Toast for success/failure feedback.
     */
    @Override public void onJoinClick(@NonNull Event e) {
        // Use a device-unique id as a stand-in "user id".
        String uid = Settings.Secure.getString(requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
        if (uid == null || uid.isEmpty()) uid = "device_demo";

        // Write to Firestore and show feedback via Toast callbacks.
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference ref = db.collection("events").document(e.getId());

        ref.update(
                "waitingList",     FieldValue.arrayUnion(uid),
                "allParticipants", FieldValue.arrayUnion(uid)
        ).addOnSuccessListener(v -> {
            toast("Success! You have joined the waitlist.");
            // Optional: refresh or disable the button if you want immediate visual feedback.
            // applyCurrentFilters(); // Enable if list item should reflect the new state instantly.
        }).addOnFailureListener(err -> {
            toast("Join failed: " + err.getMessage());
        });
    }

    /** Safe Toast helper (no-op if the fragment is detached). */
    private void toast(String msg) {
        if (getContext() != null) {
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        }
    }
}
