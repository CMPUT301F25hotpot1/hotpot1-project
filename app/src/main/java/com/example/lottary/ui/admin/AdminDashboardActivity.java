package com.example.lottary.ui.admin;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lottary.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Activity for the Admin "Latest Events" section.
 * Handles bottom-navigation switching between Events, Users, Images, and Admin tabs.
 * This screen simply displays the dashboard layout and routes navigation actions.
 */
public class AdminDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        BottomNavigationView nav = findViewById(R.id.bottomNavAdmin);
        nav.setSelectedItemId(R.id.nav_admin_events);

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_admin_events) {
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
                startActivity(new Intent(this, AdminProfileActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }
}
