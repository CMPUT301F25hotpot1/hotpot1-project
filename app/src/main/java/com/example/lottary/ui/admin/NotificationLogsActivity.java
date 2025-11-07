package com.example.lottary.ui.admin;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottary.R;
import com.example.lottary.data.FirestoreNotificationRepository;
import com.example.lottary.ui.admin.adapters.NotificationLogsAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NotificationLogsActivity extends AppCompatActivity {

    private NotificationLogsAdapter adapter;
    private FirestoreNotificationRepository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_logs);

        String deviceID = getIntent().getStringExtra("deviceID");

        repo = FirestoreNotificationRepository.get();
        adapter = new NotificationLogsAdapter();

        RecyclerView rv = findViewById(R.id.recycler_logs);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        // ✅ Load logs
        repo.getLogsForUser(deviceID, logs -> adapter.submitList(logs));

        // ✅ Setup bottom navigation
        setupBottomNav();
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);

        // ✅ Highlight "Users" tab (same category)
        bottomNav.setSelectedItemId(R.id.nav_admin_users);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_admin_events) {
                startActivity(new Intent(this, AdminEventsActivity.class));
                overridePendingTransition(0,0);
                return true;

            } else if (id == R.id.nav_admin_users) {
                startActivity(new Intent(this, AdminUsersActivity.class));
                overridePendingTransition(0,0);
                return true;

            } else if (id == R.id.nav_admin_images) {
                startActivity(new Intent(this, AdminImagesActivity.class));
                overridePendingTransition(0,0);
                return true;

            } else if (id == R.id.nav_admin_dashboard) {
                startActivity(new Intent(this, AdminDashboardActivity.class));
                overridePendingTransition(0,0);
                return true;
            }

            return false;
        });
    }
}
