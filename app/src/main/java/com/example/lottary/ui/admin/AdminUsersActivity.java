package com.example.lottary.ui.admin;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottary.R;
import com.example.lottary.data.FirestoreUserRepository;
import com.example.lottary.data.User;
import com.example.lottary.ui.admin.adapters.AdminUsersAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class AdminUsersActivity extends AppCompatActivity {

    private FirestoreUserRepository repo;
    private AdminUsersAdapter adapter;

    private RecyclerView rv;
    private EditText searchBar;
    private Button searchBtn;

    private List<User> fullList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_users);

        repo = FirestoreUserRepository.get();

        rv = findViewById(R.id.recycler_users);
        rv.setLayoutManager(new LinearLayoutManager(this));

        searchBar = findViewById(R.id.search_bar);
        searchBtn = findViewById(R.id.btn_search);

        adapter = new AdminUsersAdapter(new AdminUsersAdapter.UserClickListener() {
            @Override
            public void onViewLogs(User u) {
                Intent i = new Intent(AdminUsersActivity.this, NotificationLogsActivity.class);
                i.putExtra("deviceID", u.getId());
                startActivity(i);
            }

            @Override
            public void onRemoveUser(User u) {
                new AlertDialog.Builder(AdminUsersActivity.this)
                        .setTitle("Remove User?")
                        .setMessage("Are you sure you want to delete this user?")
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Delete", (d, w) ->
                                FirestoreUserRepository.get().deleteUser(u.getId()))
                        .show();
            }
        });

        rv.setAdapter(adapter);

        repo.listenRecentCreated(users -> {
            fullList = users;
            adapter.submitList(new ArrayList<>(fullList));
        });

        searchBtn.setOnClickListener(v -> applySearch());

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { applySearch(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        setupBottomNav();
    }

    private void setupBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        nav.setSelectedItemId(R.id.nav_admin_users);

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_admin_events) {
                startActivity(new Intent(this, AdminEventsActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                overridePendingTransition(0,0);
                finish();
                return true;
            }

            if (id == R.id.nav_admin_users) return true;

            if (id == R.id.nav_admin_images) {
                startActivity(new Intent(this, AdminImagesActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                overridePendingTransition(0,0);
                finish();
                return true;
            }

            if (id == R.id.nav_admin_dashboard) {
                startActivity(new Intent(this, AdminProfileActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                overridePendingTransition(0,0);
                finish();
                return true;
            }

            return false;
        });
    }

    private void applySearch() {
        String keyword = searchBar.getText().toString().trim().toLowerCase();

        if (keyword.isEmpty()) {
            adapter.submitList(new ArrayList<>(fullList));
            return;
        }

        List<User> filtered = new ArrayList<>();
        for (User u : fullList) {
            if (u.getName().toLowerCase().contains(keyword)) {
                filtered.add(u);
            }
        }

        adapter.submitList(filtered);
    }
}
