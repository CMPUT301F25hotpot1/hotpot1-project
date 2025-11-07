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

        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                new ArrayList<>()
        );

        list.setAdapter(adapter);

        repo.notices().observe(this, notices -> {
            adapter.clear();
            adapter.addAll(notices);
            adapter.notifyDataSetChanged();
        });

        repo.loadNotices();
    }
}
