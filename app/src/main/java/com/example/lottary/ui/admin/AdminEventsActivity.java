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
import java.util.List;

public class AdminEventsActivity extends AppCompatActivity {

    private AdminRepository repo;
    private AdminEventsAdapter adapter;

    private EditText etSearch;
    private Button btnSearch, btnFilter;
    private BottomNavigationView bottomNav;

    private final TextWatcher searchWatcher = new TextWatcher() {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void afterTextChanged(Editable s) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (repo != null) repo.search(s.toString());
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_events);

        repo = AdminRepository.get();

        // RecyclerView
        RecyclerView rv = findViewById(R.id.admin_events_list);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminEventsAdapter((Event e) -> repo.removeEvent(e));
        rv.setAdapter(adapter);

        // 搜索 & 过滤
        etSearch  = findViewById(R.id.search_events);
        btnSearch = findViewById(R.id.btn_search);
        btnFilter = findViewById(R.id.btn_filter);

        btnSearch.setOnClickListener(v -> repo.search(etSearch.getText().toString()));
        etSearch.addTextChangedListener(searchWatcher);

        btnFilter.setOnClickListener(v -> {
            String[] options = {"All", "Open", "Closed", "Full"};
            new AlertDialog.Builder(this)
                    .setTitle("Filter by status")
                    .setItems(options, (dialog, which) -> {
                        String choice = options[which].toUpperCase();
                        repo.setFilter(choice.equals("ALL") ? "ALL" : choice);
                    })
                    .show();
        });

        // 底部导航
        bottomNav = findViewById(R.id.bottomNavAdmin);
        // 确保与 menu_admin_bottom_nav.xml 中的 id 对应
        bottomNav.setSelectedItemId(R.id.nav_admin_events);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_admin_events) {
                // 当前页，不跳转
                return true;
            } else if (id == R.id.nav_admin_users) {
                startActivity(new Intent(this, AdminUsersActivity.class));
            } else if (id == R.id.nav_admin_images) {
                startActivity(new Intent(this, AdminImagesActivity.class));
            } else if (id == R.id.nav_admin_dashboard) {
                startActivity(new Intent(this, AdminDashboardActivity.class));
            } else {
                return false;
            }
            // 切换页面无过渡 & 结束当前，避免堆栈增长
            overridePendingTransition(0, 0);
            finish();
            return true;
        });

        // 观察数据
        repo.events().observe(this, list -> {
            List<Event> data = (list == null) ? new ArrayList<>() : new ArrayList<>(list);
            adapter.submitList(data);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 进入页面开始订阅
        if (repo != null) repo.startAdminEventsRealtime();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 离开页面停止订阅 & 移除监听，避免泄漏
        if (repo != null) repo.stopAdminEventsRealtime();
        if (etSearch != null) etSearch.removeTextChangedListener(searchWatcher);
    }
}
