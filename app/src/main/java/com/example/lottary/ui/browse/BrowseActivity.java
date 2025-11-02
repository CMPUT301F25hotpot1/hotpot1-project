package com.example.lottary.ui.browse;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lottary.R;
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

        findViewById(R.id.btn_filter).setOnClickListener(v -> FilterBottomSheet.newInstance(opts).show(getSupportFragmentManager(), "filter"));
        findViewById(R.id.btn_scan_qr).setOnClickListener(v -> startActivity(new Intent(this, QrScanActivity.class)));

        BottomNavigationView nav = findViewById(R.id.bottomNav);
        nav.setSelectedItemId(R.id.nav_browse);
        nav.setOnItemSelectedListener(item -> true);
    }

    @Override
    public void onApply(FilterOptions o) {
        opts = o;
        if (list != null) list.applyOptions(o);
    }
}


