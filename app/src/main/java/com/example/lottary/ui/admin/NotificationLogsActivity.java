/**
 * Admin screen showing all notification logs for a specific user.
 * Loads logs from Firestore and supports navigation to other admin sections.
 */
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

        // Retrieve user device ID
        String deviceID = getIntent().getStringExtra("deviceID");

        repo = FirestoreNotificationRepository.get();
        adapter = new NotificationLogsAdapter();

        // Recycler setup
        RecyclerView rv = findViewById(R.id.recycler_logs);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        // Load logs from Firestore
        repo.getLogsForUser(deviceID, logs -> adapter.submitList(logs));

        setupBottomNav();
    }

    // Bottom navigation for admin sections
    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);

        // Highlight Users tab since logs belong to a user
        bottomNav.setSelectedItemId(R.id.nav_admin_users);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_admin_events) {
                startActivity(new Intent(this, AdminEventsActivity.class));
                overridePendingTransition(0,0);
                return true;
            }

            if (id == R.id.nav_admin_users) {
                startActivity(new Intent(this, AdminUsersActivity.class));
                overridePendingTransition(0,0);
                return true;
            }

            if (id == R.id.nav_admin_images) {
                startActivity(new Intent(this, AdminImagesActivity.class));
                overridePendingTransition(0,0);
                return true;
            }

            if (id == R.id.nav_admin_dashboard) {
                startActivity(new Intent(this, AdminDashboardActivity.class));
                overridePendingTransition(0,0);
                return true;
            }

            return false;
        });
    }
}
