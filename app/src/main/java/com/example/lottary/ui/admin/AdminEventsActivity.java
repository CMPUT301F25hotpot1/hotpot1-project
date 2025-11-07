package com.example.lottary.ui.admin;

import android.content.Intent;
import android.os.Bundle;

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

        // RecyclerView
        RecyclerView rv = findViewById(R.id.admin_events_list);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminEventsAdapter(event -> repo.removeEvent(event));
        rv.setAdapter(adapter);

        // ✅ 从 Firestore 加载
        repo.loadEventsFromFirestore();   // <-- 这里是正确的方法名

        // ✅ 观察 LiveData，自动刷新 UI
        repo.events().observe(this, events -> {
            if (events != null) {
                // 提交新列表（拷贝一份，避免 Diff 冲突）
                adapter.submitList(new ArrayList<>(events));
            }
        });

        setupBottomNav();
    }

    private void setupBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottomNavAdmin);
        nav.setSelectedItemId(R.id.nav_admin_events);

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_admin_events) {
                // 当前页
                return true;
            } else if (id == R.id.nav_admin_users) {
                startActivity(new Intent(this, AdminUsersActivity.class));
                return true;
            } else if (id == R.id.nav_admin_images) {
                startActivity(new Intent(this, AdminImagesActivity.class));
                return true;
            } else if (id == R.id.nav_admin_dashboard) {
                startActivity(new Intent(this, AdminDashboardActivity.class));
                return true;
            }
            return false;
        });
    }
}
