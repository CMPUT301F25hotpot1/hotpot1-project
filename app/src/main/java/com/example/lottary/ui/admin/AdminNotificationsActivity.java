/**
 * Admin screen for viewing system notification messages.
 * Displays a simple list backed by an ArrayAdapter.
 */
package com.example.lottary.ui.admin;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lottary.R;
import com.example.lottary.data.AdminRepository;

import java.util.ArrayList;

public class AdminNotificationsActivity extends AppCompatActivity {

    private final AdminRepository repo = AdminRepository.get();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_notifications);

        ListView list = findViewById(R.id.admin_notifications_list);

        // Basic string adapter for notifications
        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                new ArrayList<>()
        );

        list.setAdapter(adapter);

        // If needed later: observe Firestore notification logs here
        // repo.notifications().observe(this, logs -> { ... });
    }
}
