package com.example.lottary.ui.events.manage;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lottary.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SendNotificationsActivity extends AppCompatActivity {

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_notifications);

        final String eventId = getIntent().getStringExtra(ManageEventActivity.EXTRA_EVENT_ID);

        MaterialToolbar top = findViewById(R.id.top_app_bar);
        top.setTitle("Send Notifications");
        top.setNavigationOnClickListener(v -> finish());

        RadioGroup grp = findViewById(R.id.grp_target);
        EditText etMsg  = findViewById(R.id.et_message);
        Button btnSend  = findViewById(R.id.btn_send);

        btnSend.setOnClickListener(v -> {
            String msg = etMsg.getText() == null ? "" : etMsg.getText().toString().trim();
            if (TextUtils.isEmpty(msg)) { etMsg.setError("Message required"); return; }

            String target = "waitingList";
            int checked = grp.getCheckedRadioButtonId();
            if (checked == R.id.rb_chosen) target = "chosen";
            else if (checked == R.id.rb_signed) target = "signedUp";

            fanOutAndSend(eventId, target, msg);
        });
    }

    private void fanOutAndSend(@NonNull String eventId, @NonNull String targetField, @NonNull String message) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("events").document(eventId).get()
                .addOnSuccessListener(d -> {
                    if (d == null || !d.exists()) {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_LONG).show();
                        return;
                    }
                    String eventTitle = safe(d.getString("title"));
                    List<String> recipients = asStrList(d.get(targetField));

                    if (recipients.isEmpty()) {
                        Toast.makeText(this, "No recipients in " + targetField, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<Task<?>> writes = new ArrayList<>();
                    for (String recipientId : recipients) {
                        Map<String, Object> doc = new HashMap<>();
                        doc.put("recipientId", recipientId);      // ★ 每条消息一个收件人
                        doc.put("eventId", eventId);
                        doc.put("eventTitle", eventTitle);
                        doc.put("message", message);
                        doc.put("targetGroup", targetField);       // waitingList / chosen / signedUp
                        doc.put("type", targetField.equals("chosen") ? "selected" : "info");
                        doc.put("sentAt", Timestamp.now());
                        doc.put("read", false);
                        writes.add(db.collection("notifications").add(doc));
                    }

                    Tasks.whenAllComplete(writes).addOnCompleteListener(t -> {
                        Toast.makeText(this, "Sent to " + recipients.size() + " recipients", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private static List<String> asStrList(Object o) {
        List<String> out = new ArrayList<>();
        if (o instanceof List<?>) {
            for (Object e : (List<?>) o) if (e != null) out.add(e.toString());
        }
        return out;
    }
    private static String safe(String s){ return s == null ? "" : s; }
}
