package com.example.lottary.ui.admin;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottary.R;
import com.example.lottary.data.AdminRepository;
import com.example.lottary.data.Event;
import com.example.lottary.ui.admin.adapters.AdminEventsAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class AdminEventsActivity extends AppCompatActivity {

    private AdminRepository repo;
    private AdminEventsAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_events);

        repo = AdminRepository.get();

        // =====================
        // ✅ RecyclerView setup
        // =====================
        RecyclerView rv = findViewById(R.id.admin_events_list);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AdminEventsAdapter((Event e) -> repo.removeEvent(e));
        rv.setAdapter(adapter);

        // ✅ Firestore 实时订阅
        repo.startAdminEventsRealtime();

        // ✅ LiveData → UI
        repo.events().observe(this, list ->
                adapter.submitList(list == null ? new ArrayList<>() : new ArrayList<>(list))
        );

        setupSearchBar();
        setupFilterButton();
        setupBottomNav();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        repo.stopAdminEventsRealtime();
    }

    // =====================
    // ✅ Search 功能
    // =====================
    private void setupSearchBar() {
        EditText search = findViewById(R.id.search_events);
        Button btnSearch = findViewById(R.id.btn_search);

        // 点击 SEARCH 按钮
        btnSearch.setOnClickListener(v ->
                repo.search(search.getText().toString())
        );

        // 实时搜索（用户输入时自动过滤）
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                repo.search(s.toString());
            }
        });
    }

    // =====================
    // ✅ Filter 按钮功能
    // =====================
    private void setupFilterButton() {
        Button filterBtn = findViewById(R.id.btn_filter);

        filterBtn.setOnClickListener(v -> {
            String[] options = { "All", "Open", "Closed", "Full" };

            new AlertDialog.Builder(this)
                    .setTitle("Filter by status")
                    .setItems(options, (dialog, which) -> {
                        String choice = options[which].toUpperCase();
                        if (choice.equals("ALL"))
                            repo.setFilter("ALL");
                        else
                            repo.setFilter(choice);
                    })
                    .show();
        });
    }

    // =====================
    // ✅ Bottom Navigation
    // =====================
    private void setupBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottomNavAdmin);
        nav.setSelectedItemId(R.id.nav_admin_events);

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_admin_users) {
                startActivity(new Intent(this, AdminUsersActivity.class));
                return true;
            }
            if (id == R.id.nav_admin_images) {
                startActivity(new Intent(this, AdminImagesActivity.class));
                return true;
            }
            if (id == R.id.nav_admin_dashboard) {
                startActivity(new Intent(this, AdminDashboardActivity.class));
                return true;
            }

            return true;
        });
    }
}