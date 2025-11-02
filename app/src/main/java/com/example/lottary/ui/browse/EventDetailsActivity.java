package com.example.lottary.ui.browse;

import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lottary.R;
import com.example.lottary.data.Event;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EventDetailsActivity extends AppCompatActivity implements LotteryPolicyDialogFragment.Listener {
    public static final String EXTRA_EVENT_ID = "event_id";
    private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);
        eventId = getIntent().getStringExtra(EXTRA_EVENT_ID);
        MaterialButton btnPolicy = findViewById(R.id.btn_policy);
        MaterialButton btnJoin = findViewById(R.id.btn_join);
        btnPolicy.setOnClickListener(v -> LotteryPolicyDialogFragment.newInstance().show(getSupportFragmentManager(), "policy"));
        btnJoin.setOnClickListener(v -> tryJoin());
        load();
    }

    private void load() {
        if (eventId == null) return;
        FirebaseFirestore.getInstance().collection("events").document(eventId)
                .get().addOnSuccessListener(this::bind);
    }

    private void bind(DocumentSnapshot d) {
        TextView title = findViewById(R.id.tvTitle);
        TextView city = findViewById(R.id.tvLocation);
        TextView time = findViewById(R.id.tvTime);
        TextView venue = findViewById(R.id.tvVenue);
        String t = d.getString("title");
        String c = d.getString("city");
        String v = d.getString("venue");
        Timestamp ts = d.getTimestamp("startTime");
        String pretty = ts == null ? "" : DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(ts.toDate());
        title.setText(t == null ? "" : t);
        city.setText(c == null ? "" : c);
        venue.setText(v == null ? "" : v);
        time.setText(pretty);
    }

    private void tryJoin() {
        if (!LotteryPolicyPrefs.isAccepted(this)) {
            LotteryPolicyDialogFragment.newInstance().show(getSupportFragmentManager(), "policy");
            return;
        }
        doJoin();
    }

    private void doJoin() {
        if (eventId == null) return;
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        final String did = deviceId == null || deviceId.isEmpty() ? "device_demo" : deviceId;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference ref = db.collection("events").document(eventId);
        db.runTransaction(tr -> {
            DocumentSnapshot d = tr.get(ref);
            if (!d.exists()) return null;
            List<String> arr = (List<String>) d.get("waitingList");
            if (arr == null) arr = new ArrayList<>();
            if (!arr.contains(did)) arr.add(did);
            HashMap<String,Object> up = new HashMap<>();
            up.put("waitingList", arr);
            tr.set(ref, up, SetOptions.merge());
            return null;
        }).addOnSuccessListener(x -> finish());
    }

    @Override
    public void onAcceptPolicy(boolean dontAskAgain) {
        if (dontAskAgain) LotteryPolicyPrefs.setAccepted(this, true);
        doJoin();
    }
}

