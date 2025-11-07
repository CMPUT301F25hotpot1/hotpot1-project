/**
 * Admin profile screen showing the organizer's account info.
 * Loads user data from Firestore and supports switching
 * to the normal (Browse) user view via a button.
 */
package com.example.lottary.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lottary.R;
import com.example.lottary.data.FirestoreUserRepository;
import com.example.lottary.data.User;
import com.example.lottary.ui.browse.BrowseActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminProfileActivity extends AppCompatActivity {

    private TextView tvName, tvEmail, tvPhone;
    private FirestoreUserRepository repo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_profile);

        repo = FirestoreUserRepository.get();

        // UI references
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);

        // Load device ID used as Firestore user key
        String deviceId = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
        if (deviceId == null || deviceId.trim().isEmpty()) {
            deviceId = "device_demo"; // fallback
        }

        // Realtime Firestore user listener
        repo.listenUser(deviceId, snap -> {
            User u = snap.toObject(User.class);
            if (u != null) {
                tvName.setText(u.getName());
                tvEmail.setText(u.getEmail());
                tvPhone.setText(
                        (u.getPhoneNum() == null || u.getPhoneNum().isEmpty())
                                ? "None" : u.getPhoneNum()
                );
            }
        });

        // Switch to normal Browse UI
        Button btnSwap = findViewById(R.id.btnSwapToUser);
        btnSwap.setOnClickListener((View v) -> {
            startActivity(new Intent(this, BrowseActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
            overridePendingTransition(0, 0);
            finish();
        });

        // Bottom navigation
        BottomNavigationView nav = findViewById(R.id.bottomNavAdmin);
        nav.setSelectedItemId(R.id.nav_admin_dashboard);

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_admin_events) {
                startActivity(new Intent(this, AdminEventsActivity.class)
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
                return true; // already here
            }

            return false;
        });
    }
}
