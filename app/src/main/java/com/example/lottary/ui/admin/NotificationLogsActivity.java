package com.example.lottary.ui.admin;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottary.R;
import com.example.lottary.data.FirestoreNotificationRepository;
import com.example.lottary.ui.admin.adapters.NotificationLogsAdapter;

public class NotificationLogsActivity extends AppCompatActivity {

    private NotificationLogsAdapter adapter;
    private FirestoreNotificationRepository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_logs);

        String uid = getIntent().getStringExtra("uid");

        repo = FirestoreNotificationRepository.get();
        adapter = new NotificationLogsAdapter();

        RecyclerView rv = findViewById(R.id.recycler_logs);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        repo.getLogsForUser(uid, logs ->
                adapter.submitList(logs)
        );
    }
}
