package com.example.lottary.ui.admin;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lottary.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Admin "Latest Events" screen (your activity_admin_dashboard.xml).
 * Bottom bar: Events / Users / Images / Admin (profile).
 * NOTE: This class does NOT touch repositories or adapters to keep it compile-clean.
 */
public class AdminDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Bottom nav
        BottomNavigationView nav = findViewById(R.id.bottomNavAdmin);

        // This page shows “Latest Events”, so highlight the Events tab.
        nav.setSelectedItemId(R.id.nav_admin_events);

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_admin_events) {
                // already here (events area)
                return true;
            }
            if (id == R.id.nav_admin_users) {
                startActivity(new Intent(this, AdminUsersActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                overridePendingTransition(0, 0);
                return true;
            }
            if (id == R.id.nav_admin_images) {
                startActivity(new Intent(this, AdminImagesActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                overridePendingTransition(0, 0);
                return true;
            }
            if (id == R.id.nav_admin_dashboard) {
                // “Admin” tab goes to the Admin Profile page
                startActivity(new Intent(this, AdminProfileActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }
}