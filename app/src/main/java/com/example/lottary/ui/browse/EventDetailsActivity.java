package com.example.lottary.ui.browse;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lottary.R;
import com.example.lottary.data.FirestoreEventRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * EventDetailsActivity
 *
 * Displays a single event's details and lets the user join the waiting list
 * (after acknowledging lottery policy).
 *
 * Can be opened by:
 *  - explicit intent with EXTRA_EVENT_ID, or
 *  - deep-link URI such as lottary://event/{eventId}.
 */
public class EventDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_EVENT_ID = "event_id";

    private ListenerRegistration reg;
    private String eventId;

    private ImageView ivPoster;
    private TextView tvTitle, tvVenue, tvCityDate, tvDescription, tvStatus, tvCapacity, tvWaitlist;
    private MaterialButton btnJoin;
    private ImageButton btnClose;

    /** Build a deep-link payload string to be embedded into a QR code. */
    @NonNull
    public static String buildDeepLinkPayload(@NonNull String eventId) {
        // simple custom scheme: lottary://event/<id>
        return "lottary://event/" + eventId;
    }

    /** Parse an event id from either a deep-link URL or a bare id string. */
    @Nullable
    public static String parseEventIdFromPayload(@Nullable String payload) {
        if (payload == null || payload.isEmpty()) return null;

        try {
            Uri uri = Uri.parse(payload);
            if (uri.getScheme() != null) {
                // Our custom scheme: lottary://event/<id>
                if ("lottary".equalsIgnoreCase(uri.getScheme())) {
                    String last = uri.getLastPathSegment();
                    return (last == null || last.isEmpty()) ? null : last;
                }
                // HTTPS or other schemes: take last path segment as id.
                String last = uri.getLastPathSegment();
                if (last != null && !last.isEmpty()) return last;
            }
        } catch (Exception ignore) {
            // fall through and treat as raw id
        }
        // payload is not a URI, treat it as a raw event id.
        return payload;
    }

    public static Intent makeIntent(Context ctx, String eventId) {
        return new Intent(ctx, EventDetailsActivity.class).putExtra(EXTRA_EVENT_ID, eventId);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        // 1) Try to read explicit extra.
        eventId = getIntent().getStringExtra(EXTRA_EVENT_ID);

        // 2) If missing, try to parse from deep-link URI.
        if (TextUtils.isEmpty(eventId)) {
            Uri data = getIntent().getData();
            if (data != null) {
                eventId = parseEventIdFromPayload(data.toString());
            }
        }

        if (TextUtils.isEmpty(eventId)) {
            Toast.makeText(this, "Missing event id", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ivPoster      = findViewById(R.id.iv_poster);
        tvTitle       = findViewById(R.id.tv_title);
        tvVenue       = findViewById(R.id.tv_venue);
        tvCityDate    = findViewById(R.id.tvCityDate);
        tvDescription = findViewById(R.id.tvDescription);
        tvStatus      = findViewById(R.id.tv_status);
        tvCapacity    = findViewById(R.id.tvCapacity);
        tvWaitlist    = findViewById(R.id.tvWaitlist);
        btnJoin       = findViewById(R.id.btn_join);
        btnClose      = findViewById(R.id.btnClose);

        if (tvDescription != null) tvDescription.setMovementMethod(new ScrollingMovementMethod());
        if (btnClose != null) btnClose.setOnClickListener(v -> finish());

        reg = FirestoreEventRepository.get().listenEvent(eventId, this::bindDoc);
        btnJoin.setOnClickListener(v -> showPolicyThenJoin());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reg != null) {
            reg.remove();
            reg = null;
        }
    }

    private void bindDoc(DocumentSnapshot d) {
        if (d == null || !d.exists()) {
            Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        String title = safe(d.getString("title"));
        String venue = safe(d.getString("venue"));
        String city  = safe(d.getString("city"));
        String desc  = firstNonEmpty(d.getString("description"), d.getString("desc"));

        Integer capacityInt = getInt(d.get("capacity"));
        int capacity = capacityInt == null ? 0 : capacityInt;

        boolean fullField = Boolean.TRUE.equals(d.getBoolean("full"));

        Timestamp tsStart = d.getTimestamp("startTime");
        long startMs = tsStart == null ? 0L : tsStart.toDate().getTime();
        String pretty = tsStart == null ? "" :
                DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault())
                        .format(tsStart.toDate());

        List<String> waiting = strList(d.get("waitingList"));
        List<String> signed  = strList(d.get("signedUp"));
        int waitingCount = waiting.size();
        int signedCount  = signed.size();

        boolean ended = startMs > 0 && startMs < System.currentTimeMillis();
        boolean fullByCap = capacity > 0 && signedCount >= capacity;
        boolean isOpen = !ended && !fullByCap && !fullField;

        if (tvTitle != null) tvTitle.setText(title);
        if (tvVenue != null) tvVenue.setText(venue);
        if (tvCityDate != null) tvCityDate.setText(
                city + (TextUtils.isEmpty(pretty) ? "" : (", " + pretty)));
        if (tvDescription != null) tvDescription.setText(desc);

        if (tvStatus != null) {
            if (ended) {
                tvStatus.setText("Ended");
                tvStatus.setTextColor(getColor(android.R.color.darker_gray));
            } else if (!isOpen) {
                tvStatus.setText("Full");
                tvStatus.setTextColor(getColor(R.color.full_red));
            } else {
                tvStatus.setText("Open");
                tvStatus.setTextColor(getColor(R.color.open_green));
            }
        }

        if (tvCapacity != null) {
            tvCapacity.setText(capacity > 0 ? (capacity + " slots") : "No capacity limit");
        }
        if (tvWaitlist != null) {
            tvWaitlist.setText(waitingCount + " on waiting list");
        }

        btnJoin.setEnabled(isOpen);
    }

    private void showPolicyThenJoin() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_lottery_policy, null, false);

        final AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(view)
                .create();

        MaterialCheckBox cb = view.findViewById(R.id.cb_dont_ask);
        MaterialButton   btnYes = view.findViewById(R.id.btn_yes);
        MaterialButton   btnNo  = view.findViewById(R.id.btn_no);

        if (cb == null || btnYes == null || btnNo == null) {
            dialog.dismiss();
            buildSimplePolicyDialog().show();
            return;
        }

        btnYes.setEnabled(false);
        cb.setOnCheckedChangeListener((buttonView, isChecked) -> btnYes.setEnabled(isChecked));

        btnNo.setOnClickListener(v -> dialog.dismiss());
        btnYes.setOnClickListener(v -> {
            dialog.dismiss();
            doJoin();
        });

        dialog.show();
    }

    private AlertDialog buildSimplePolicyDialog() {
        final android.widget.LinearLayout root = new android.widget.LinearLayout(this);
        root.setOrientation(android.widget.LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        root.setPadding(pad, pad, pad, pad);

        android.widget.TextView tv = new android.widget.TextView(this);
        tv.setText(
                "Lottery Rules & Guidelines\n\n" +
                        "• Winners are selected randomly from the waiting list.\n" +
                        "• If selected entrants decline or do not sign up, we may draw again " +
                        "  from the remaining waiting list.\n" +
                        "• Entrant should meet the age and location requirements of the organizer.\n" +
                        "• Please only join if you are genuinely interested in attending."
        );
        root.addView(tv);

        final android.widget.CheckBox cb = new android.widget.CheckBox(this);
        cb.setText("I have read the rules and guidelines.");
        root.addView(cb);

        final AlertDialog dlg = new MaterialAlertDialogBuilder(this)
                .setView(root)
                .setNegativeButton("Don't Join", (d, w) -> d.dismiss())
                .setPositiveButton("Join Waiting List", null)
                .create();

        dlg.setOnShowListener(d -> {
            dlg.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            cb.setOnCheckedChangeListener((buttonView, isChecked) ->
                    dlg.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(isChecked));
            dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                dlg.dismiss();
                doJoin();
            });
        });
        return dlg;
    }

    private void doJoin() {
        String did = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        if (TextUtils.isEmpty(did)) did = "device_demo";
        FirestoreEventRepository.get().joinWaitingList(eventId, did)
                .addOnSuccessListener(v ->
                        Toast.makeText(this, "Success! You have joined the waitlist.", Toast.LENGTH_LONG).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to join: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    // -------------- helpers --------------

    private static Integer getInt(Object o) {
        if (o instanceof Number) return ((Number) o).intValue();
        try { return o == null ? null : Integer.parseInt(o.toString()); } catch (Exception ignore) { return null; }
    }

    private static String safe(String s){ return s == null ? "" : s; }

    @SuppressWarnings("unchecked")
    private static List<String> strList(Object o) {
        if (o instanceof List<?>) {
            List<String> out = new ArrayList<>();
            for (Object e : (List<?>) o) if (e != null) out.add(e.toString());
            return out;
        }
        return new ArrayList<>();
    }

    private static String firstNonEmpty(String a, String b){
        return !TextUtils.isEmpty(a) ? a : (b == null ? "" : b);
    }
}
