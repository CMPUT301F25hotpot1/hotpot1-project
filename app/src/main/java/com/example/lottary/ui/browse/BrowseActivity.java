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
import com.example.lottary.ui.profile.MyProfileActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * BrowseActivity
 *
 * Role / Purpose:
 * - The entry point of the "Browse" tab. Hosts a {@link BrowseListFragment} that shows a
 *   scrollable list of events and exposes top-level actions: free-text search, filter sheet,
 *   QR tools (generate + scan), and bottom navigation to other top destinations.
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
        if (getSupportFragmentManager().findFragmentById(R.id.container)
                instanceof BrowseListFragment) {
            list = (BrowseListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.container);
        } else {
            list = new BrowseListFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, list)
                    .commitNow();
        }

        // --- Search wiring ---
        EditText input = findViewById(R.id.input_search);
        Button btnSearch = findViewById(R.id.btn_search);
        btnSearch.setOnClickListener(v -> {
            String q = input.getText() == null ? "" : input.getText().toString();
            list.applyFilter(TextUtils.isEmpty(q) ? "" : q);
        });

        // --- Filter bottom sheet ---
        findViewById(R.id.btn_filter).setOnClickListener(v ->
                FilterBottomSheet.newInstance(opts, this)
                        .show(getSupportFragmentManager(), "filter")
        );

        // --- QR tools entry points ---

        // 1) Generate a QR code for an event (this is the page你刚刚已经做好的).
        findViewById(R.id.btn_generate_qr).setOnClickListener(v ->
                startActivity(new Intent(this, QrScanActivity.class)));

        // 2) In-app scanner: open camera and scan a QR code to jump to EventDetailsActivity.
        findViewById(R.id.btn_scan_event_qr).setOnClickListener(v ->
                startActivity(new Intent(this, EventQrScannerActivity.class)));

        // --- Bottom navigation setup ---
        BottomNavigationView nav = findViewById(R.id.bottomNav);
        nav.setSelectedItemId(R.id.nav_browse);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_browse) {
                return true;
            } else if (id == R.id.nav_my_events) {
                Intent i = new Intent(this, MyEventsActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_notifications) {
                Intent i = new Intent(this, NotificationsActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, MyProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView nav = findViewById(R.id.bottomNav);
        nav.setSelectedItemId(R.id.nav_browse);
    }

    /** Callback from {@link FilterBottomSheet}: user confirmed new options. */
    @Override
    public void onApply(FilterOptions o) {
        opts = o;
        if (list != null) list.applyOptions(o);
    }
}
