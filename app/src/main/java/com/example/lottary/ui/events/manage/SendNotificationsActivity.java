package com.example.lottary.ui.events.manage;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lottary.R;
import com.google.android.material.appbar.MaterialToolbar;

public class SendNotificationsActivity extends AppCompatActivity {

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_notifications);

        String eventId = getIntent().getStringExtra(ManageEventActivity.EXTRA_EVENT_ID);
        MaterialToolbar top = findViewById(R.id.top_app_bar);
        top.setNavigationOnClickListener(v -> finish());
        top.setTitle("Send Notifications");

        RadioGroup group = findViewById(R.id.grp_target);
        EditText etMsg = findViewById(R.id.et_message);
        Button btnSend = findViewById(R.id.btn_send);

        btnSend.setOnClickListener(v -> {
            int checked = group.getCheckedRadioButtonId();
            String target = checked == R.id.rb_chosen ? "chosen"
                    : checked == R.id.rb_signed ? "signedUp"
                    : "waitingList";
            String msg = etMsg.getText() == null ? "" : etMsg.getText().toString().trim();
            if (TextUtils.isEmpty(msg)) {
                etMsg.setError("Message required");
                return;
            }
            // 这里直接 Toast 模拟。若你接入 FCM 或 Firestore 通知集合，可在此写入消息。
            Toast.makeText(this, "Sent to " + target + " (event " + eventId + "): " + msg,
                    Toast.LENGTH_LONG).show();
            finish();
        });
    }
}
