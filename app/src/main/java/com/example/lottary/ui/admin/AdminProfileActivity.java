package com.example.lottary.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lottary.R;

public class AdminProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_profile);

        // ✅ 直接找到你 XML 里的按钮：swap_to_admin_btn
        View swapBtn = findViewById(R.id.swap_to_admin_btn);

        if (swapBtn != null) {
            swapBtn.setOnClickListener(v -> {

                // ✅ 一点击就直接跳到 AdminEventsActivity
                Intent intent = new Intent(this, AdminEventsActivity.class);

                // ✅ 告诉 AdminEventsActivity：这是从 profile 跳过去的
                intent.putExtra("open_from_profile", true);

                startActivity(intent);
            });
        }
    }
}
