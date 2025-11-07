package com.example.lottary.ui.admin;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lottary.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        setupBottomNav();
    }

    private void setupBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottomNavAdmin);
        nav.setSelectedItemId(R.id.nav_admin_dashboard);

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_admin_events) {
                startActivity(new Intent(this, AdminEventsActivity.class));
                return true;
            }
            if (id == R.id.nav_admin_users) {
                startActivity(new Intent(this, AdminUsersActivity.class));
                return true;
            }
            if (id == R.id.nav_admin_images) {
                startActivity(new Intent(this, AdminImagesActivity.class));
                return true;
            }
            // 当前页面
            return true;
        });
    }
}
