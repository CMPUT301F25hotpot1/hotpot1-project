package com.example.lottary.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottary.R;
import com.example.lottary.data.FirestoreNotificationRepository;
import com.example.lottary.data.NotificationLog;
import com.example.lottary.ui.admin.adapters.UserNotificationLogsAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

/**
 * Admin screen that shows the notification log for a single user.
 *
 * <p>This activity is opened from {@link AdminUsersActivity} when
 * the admin taps the "view logs" button on a user row. It displays
 * all documents from the {@code notifications} collection whose
 * {@code recipientId} matches the selected user's device id.</p>
 *
 * <p>The UI reuses the existing admin bottom navigation bar, but
 * the logical "section" is considered to be the Users tab.</p>
 */
public class UserNotificationLogsActivity extends AppCompatActivity {

    /** Intent extra: selected user's id (device id / recipientId). */
    public static final String EXTRA_USER_ID = "extra_user_id";

    /** Intent extra: selected user's display name (for subtitle only). */
    public static final String EXTRA_USER_NAME = "extra_user_name";

    private RecyclerView recyclerView;
    private UserNotificationLogsAdapter adapter;

    private String userId;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_notification_logs);

        // Read user id + name passed from AdminUsersActivity.
        Intent intent = getIntent();
        userId = intent.getStringExtra(EXTRA_USER_ID);
        userName = intent.getStringExtra(EXTRA_USER_NAME);

        if (userId == null || userId.isEmpty()) {
            // Without a target user there is nothing to show.
            Toast.makeText(this, "No user specified", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupTopBar();
        setupRecyclerView();
        setupBottomNav();
        loadLogsForUser();
    }

    /**
     * Configures the custom top bar: back arrow, title, and subtitle.
     * Subtitle shows either the user's name or a generic label.
     */
    private void setupTopBar() {
        ImageButton backButton = findViewById(R.id.btnBack);
        TextView titleTextView = findViewById(R.id.textTitle);
        TextView subtitleTextView = findViewById(R.id.textSubtitle);

        titleTextView.setText("Notifications Log");

        if (userName != null && !userName.isEmpty()) {
            subtitleTextView.setText("to " + userName);
        } else {
            subtitleTextView.setText("CMPUT Corp.");
        }

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    /**
     * Sets up the RecyclerView and attaches the adapter that renders
     * {@link NotificationLog} rows.
     */
    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerUserNotificationLogs);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserNotificationLogsAdapter();
        recyclerView.setAdapter(adapter);
    }

    /**
     * Wires the admin bottom navigation so the admin can move between
     * Events, Users, Images, and Profile screens.
     */
    private void setupBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        // This screen logically belongs to the Users section.
        nav.setSelectedItemId(R.id.nav_admin_users);

        nav.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_admin_events) {
                    startActivity(new Intent(UserNotificationLogsActivity.this,
                            AdminEventsActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                }

                if (id == R.id.nav_admin_users) {
                    startActivity(new Intent(UserNotificationLogsActivity.this,
                            AdminUsersActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                }

                if (id == R.id.nav_admin_images) {
                    startActivity(new Intent(UserNotificationLogsActivity.this,
                            AdminImagesActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                }

                if (id == R.id.nav_admin_profile) {
                    startActivity(new Intent(UserNotificationLogsActivity.this,
                            AdminProfileActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                }

                return false;
            }
        });
    }

    /**
     * Reads notifications for the selected user from Firestore via
     * {@link FirestoreNotificationRepository} and submits them to the adapter.
     */
    private void loadLogsForUser() {
        FirestoreNotificationRepository.get()
                .getLogsForUser(userId, new FirestoreNotificationRepository.LogsListener() {
                    @Override
                    public void onChanged(List<NotificationLog> list) {
                        adapter.setItems(list);
                    }
                });
    }
}
