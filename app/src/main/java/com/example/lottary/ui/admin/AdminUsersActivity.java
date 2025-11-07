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

        // ✅ 设置 Adapter
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

        // ✅ Firestore 实时监听用户列表
        repo.listenRecentCreated(users -> {
            fullList = users;
            adapter.submitList(new ArrayList<>(fullList));
        });

        // ✅ 搜索按钮
        searchBtn.setOnClickListener(v -> applySearch());

        // ✅ 输入框实时搜索（可关闭）
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                applySearch();
            }
            @Override public void afterTextChanged(Editable s) { }
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
