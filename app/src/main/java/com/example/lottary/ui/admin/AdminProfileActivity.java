package com.example.lottary.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lottary.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_profile);

        // ✅ Swap to User View
        Button btnSwap = findViewById(R.id.btnSwapToUser);
        btnSwap.setOnClickListener(v -> {
            // ✅ 回到用户浏览首页（保持你自己的逻辑）
            startActivity(new Intent(this, com.example.lottary.ui.browse.BrowseActivity.class));
            finish();
        });

        // ✅ Bottom Nav
        BottomNavigationView nav = findViewById(R.id.bottomNavAdmin);
        nav.setSelectedItemId(R.id.nav_admin_dashboard);   // 当前页面

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_admin_dashboard) {
                return true; // 当前页面
            }

            if (id == R.id.nav_admin_events) {
                startActivity(new Intent(this, AdminEventsActivity.class));
                finish();
                return true;
            }

            if (id == R.id.nav_admin_users) {
                startActivity(new Intent(this, AdminUsersActivity.class));
                finish();
                return true;
            }

            if (id == R.id.nav_admin_images) {
                startActivity(new Intent(this, AdminImagesActivity.class));
                finish();
                return true;
            }

            return false;
        });
    }
}
