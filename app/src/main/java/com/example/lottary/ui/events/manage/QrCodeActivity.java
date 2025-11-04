package com.example.lottary.ui.events.manage;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lottary.R;
import com.example.lottary.data.FirestoreEventRepository;

public class QrCodeActivity extends AppCompatActivity {

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code);

        String eventId = getIntent().getStringExtra(ManageEventActivity.EXTRA_EVENT_ID);

        com.google.android.material.appbar.MaterialToolbar top = findViewById(R.id.top_app_bar);
        top.setNavigationOnClickListener(v -> finish());
        top.setTitle("QR Code");

        TextView txt = findViewById(R.id.txt_payload);
        Button btnCopy = findViewById(R.id.btn_copy);

        // 简单把 eventId 当作“二维码载荷”展示
        txt.setText(eventId == null ? "(no id)" : eventId);

        btnCopy.setOnClickListener(v -> {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText("event_id", txt.getText().toString()));
            Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show();
        });

        // 如果你想显示标题，也可以监听：
        FirestoreEventRepository.get().listenEvent(eventId, d -> {
            String t = d != null ? d.getString("title") : null;
            if (t != null && !t.isEmpty()) top.setTitle("QR • " + t);
        });
    }
}
