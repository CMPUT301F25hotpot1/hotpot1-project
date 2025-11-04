package com.example.lottary.ui.browse;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lottary.R;
import com.example.lottary.ui.events.MyEventsActivity;
import com.example.lottary.ui.notifications.NotificationsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BrowseActivity extends AppCompatActivity implements FilterBottomSheet.Listener {
    private BrowseListFragment list;
    private FilterOptions opts = new FilterOptions();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse);

        list = new BrowseListFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.container, list).commitNow();

        EditText input = findViewById(R.id.input_search);
        findViewById(R.id.btn_search).setOnClickListener(v -> {
            String q = input.getText() == null ? "" : input.getText().toString();
            if (list != null) list.applyFilter(TextUtils.isEmpty(q) ? "" : q);
        });

        findViewById(R.id.btn_filter).setOnClickListener(v ->
                FilterBottomSheet.newInstance(opts).show(getSupportFragmentManager(), "filter"));

        findViewById(R.id.btn_scan_qr).setOnClickListener(v ->
                startActivity(new Intent(this, QrScanActivity.class)));

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

    @Override
    public void onApply(FilterOptions o) {
        opts = o;
        if (list != null) list.applyOptions(o);
    }
}
