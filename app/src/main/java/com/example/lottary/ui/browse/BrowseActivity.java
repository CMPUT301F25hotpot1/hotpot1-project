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
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Merged BrowseActivity:
 * - Reuse or create BrowseListFragment safely (your version).
 * - BottomNavigationView navigation to MyEvents / Notifications (first person's version).
 * - Search box + Search button.
 * - Filter button opens FilterBottomSheet with Listener (matches current newInstance signature).
 * - QR Scan button to QrScanActivity (first person's version).
 */
public class BrowseActivity extends AppCompatActivity implements FilterBottomSheet.Listener {

    private BrowseListFragment list;
    private FilterOptions opts = new FilterOptions();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse);

        // Reuse existing fragment if present, otherwise create a new one
        if (getSupportFragmentManager().findFragmentById(R.id.container) instanceof BrowseListFragment) {
            list = (BrowseListFragment) getSupportFragmentManager().findFragmentById(R.id.container);
        } else {
            list = new BrowseListFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, list)
                    .commitNow();
        }

        // Search
        EditText input = findViewById(R.id.input_search);
        Button btnSearch = findViewById(R.id.btn_search);
        btnSearch.setOnClickListener(v -> {
            String q = input.getText() == null ? "" : input.getText().toString();
            list.applyFilter(TextUtils.isEmpty(q) ? "" : q);
        });

        // Filter
        findViewById(R.id.btn_filter).setOnClickListener(v ->
                FilterBottomSheet.newInstance(opts, this)
                        .show(getSupportFragmentManager(), "filter")
        );

        // QR Scan
        findViewById(R.id.btn_scan_qr).setOnClickListener(v ->
                startActivity(new Intent(this, QrScanActivity.class)));

        // Bottom navigation
        BottomNavigationView nav = findViewById(R.id.bottomNav);
        nav.setSelectedItemId(R.id.nav_browse);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_browse) {
                return true; // already here
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
                // TODO: start your Profile activity if/when it exists
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

    /** FilterBottomSheet.Listener */
    @Override
    public void onApply(FilterOptions o) {
        opts = o;
        if (list != null) list.applyOptions(o);
    }
}


