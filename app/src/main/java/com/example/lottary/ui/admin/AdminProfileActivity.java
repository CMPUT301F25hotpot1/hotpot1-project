package com.example.lottary.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lottary.R;
import com.example.lottary.ui.browse.BrowseActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Admin Profile screen (your activity_admin_profile.xml).
 * Has a “Swap to User View” button that goes back to user home (BrowseActivity).
 * Bottom bar still allows switching among admin tabs.
 */
public class AdminProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_profile);

        // Swap to user (Organizer) view
        Button btnSwap = findViewById(R.id.btnSwapToUser);
        btnSwap.setOnClickListener((View v) -> {
            startActivity(new Intent(this, BrowseActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
            overridePendingTransition(0, 0);
            finish(); // close admin profile so back stack returns to user side
        });

        // Bottom nav
        BottomNavigationView nav = findViewById(R.id.bottomNavAdmin);

        // We are on Admin Profile, so highlight the Admin tab.
        nav.setSelectedItemId(R.id.nav_admin_dashboard);

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_admin_events) {
                startActivity(new Intent(this, AdminDashboardActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                overridePendingTransition(0, 0);
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
                // already here
                return true;
            }
            return false;
        });
    }
}