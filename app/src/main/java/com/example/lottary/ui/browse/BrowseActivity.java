package com.example.lottary.ui.browse;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lottary.R;
import com.example.lottary.ui.events.MyEventsActivity;
import com.example.lottary.ui.notifications.NotificationsActivity;
import com.example.lottary.ui.profile.ProfileActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * BrowseActivity
 *
 * Role / Purpose:
 * - The entry point of the "Browse" tab. Hosts a {@link BrowseListFragment} that shows a
 *   scrollable list of events and exposes top-level actions: free-text search, filter sheet,
 *   QR scan, and bottom navigation to other top destinations.
 *
 * Architectural Notes:
 * - View/Controller layer. Business/data logic (Firestore listeners, filtering) lives in
 *   {@link BrowseListFragment}. This activity only wires UI and forwards user intents.
 * - Pattern: single-activity-per-tab feel with a fragment container.
 *
 * UI Contract (required IDs in activity_browse.xml):
 * - R.id.container (FrameLayout hosting BrowseListFragment)
 * - R.id.input_search, R.id.btn_search
 * - R.id.btn_filter, R.id.btn_scan_qr
 * - R.id.bottomNav (BottomNavigationView with nav_browse / nav_my_events / nav_notifications / nav_profile)
 *
 * Navigation:
 * - MyEventsActivity, NotificationsActivity, ProfileActivity via BottomNavigationView.
 * - QrScanActivity via explicit button.
 *
 * State:
 * - Keeps the last-applied {@link FilterOptions} in memory (not persisted across process death).
 * - Re-selects the "Browse" tab in onResume() to keep visual state consistent.
 *
 * Testing Tips:
 * - Use ActivityScenario to launch this activity.
 * - Espresso can assert toolbar/search/recycler visibility; list content comes from the fragment.
 *
 * Known Limitations / TODO:
 * - Filter options are not saved/restored on process death (could be moved to a ViewModel/SavedState).
 * - Search text is not persisted across configuration changes (delegate or save if needed).
 */
public class BrowseActivity extends AppCompatActivity implements FilterBottomSheet.Listener {

    /** The hosted list fragment that renders and filters events. */
    private BrowseListFragment list;

    /** In-memory copy of the latest filter options passed to the filter sheet. */
    private FilterOptions opts = new FilterOptions();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse); // Inflate the Browse screen layout.

        // Ensure a BrowseListFragment is present in the container.
        // Reuse the existing instance after a configuration change; otherwise create one.
        if (getSupportFragmentManager().findFragmentById(R.id.container) instanceof BrowseListFragment) {
            list = (BrowseListFragment) getSupportFragmentManager().findFragmentById(R.id.container);
        } else {
            list = new BrowseListFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, list)
                    .commitNow(); // Commit synchronously so 'list' is immediately available.
        }

        // --- Search wiring ---
        // On click, forward the normalized query to the fragment's filter API.
        EditText input = findViewById(R.id.input_search);
        Button btnSearch = findViewById(R.id.btn_search);
        btnSearch.setOnClickListener(v -> {
            String q = input.getText() == null ? "" : input.getText().toString();
            // Treat null/blank as "no keyword" so the fragment shows all items.
            list.applyFilter(TextUtils.isEmpty(q) ? "" : q);
        });

        // --- Filter bottom sheet ---
        // Open the sheet with the current options; results are delivered via onApply().
        findViewById(R.id.btn_filter).setOnClickListener(v ->
                FilterBottomSheet.newInstance(opts, this)
                        .show(getSupportFragmentManager(), "filter")
        );

        // --- QR Scan entry point ---
        // Delegate to QrScanActivity for scanning event/location codes.
        findViewById(R.id.btn_scan_qr).setOnClickListener(v ->
                startActivity(new Intent(this, QrScanActivity.class)));

        // --- Bottom navigation setup ---
        BottomNavigationView nav = findViewById(R.id.bottomNav);
        nav.setSelectedItemId(R.id.nav_browse); // Highlight current destination.
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_browse) {
                return true; // Already here; consume the event.
            } else if (id == R.id.nav_my_events) {
                // Bring an existing MyEventsActivity to front if present to avoid duplicates.
                Intent i = new Intent(this, MyEventsActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
                overridePendingTransition(0, 0); // Seamless tab switch.
                return true;
            } else if (id == R.id.nav_notifications) {
                Intent i = new Intent(this, NotificationsActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false; // Not handled.
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Defensive re-selection in case user navigated away and came back.
        BottomNavigationView nav = findViewById(R.id.bottomNav);
        nav.setSelectedItemId(R.id.nav_browse);
    }

    /** Callback from {@link FilterBottomSheet}: user confirmed new options. */
    @Override
    public void onApply(FilterOptions o) {
        // Cache for the next time the sheet opens and propagate to the fragment immediately.
        opts = o;
        if (list != null) list.applyOptions(o);
    }
}



