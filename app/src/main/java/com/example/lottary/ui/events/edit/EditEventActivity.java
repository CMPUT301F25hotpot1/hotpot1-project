package com.example.lottary.ui.events.edit;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lottary.R;
import com.example.lottary.data.FirestoreEventRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.DateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * EditEventActivity
 *
 * Purpose:
 * Screen for editing an existing event. It observes the event document from Firestore,
 * populates UI fields, allows the user to modify editable attributes (title, description,
 * capacity, price, and geolocation flag), and submits an update back to Firestore.
 *
 * Design Role:
 * - “Update” part of the CRUD flow for events.
 * - Uses FirestoreEventRepository to listen to a single event and to push updates.
 * - Displays a modal ProgressDialog while saving and disables the edit button to avoid duplicates.
 *
 * Data & UX Notes:
 * - Reads the target event id from intent extra {@link #EXTRA_EVENT_ID}; finishes early if missing.
 * - Timestamp fields are rendered as formatted strings for display only; they are not edited here.
 * - Poster upload entry is currently a stub trigger.
 *
 * Outstanding Issues / TODOs:
 * - No cross-field validation for date/time consistency.
 * - Limited error handling; network/rules failures are surfaced via Toast and logcat only.
 * - ProgressDialog is legacy; consider replacing with a non-blocking in-UI indicator.
 */
public class EditEventActivity extends AppCompatActivity {

    public static final String EXTRA_EVENT_ID = "event_id";

    private String eventId;

    private MaterialToolbar topBar;
    private EditText etTitle, etDesc, etStart, etEnd, etRegStart, etRegEnd, etCapacity, etPrice;
    private Switch switchGeo;
    private Button btnEdit;
    private LinearLayout boxUpload;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        eventId = getIntent().getStringExtra(EXTRA_EVENT_ID);
        if (TextUtils.isEmpty(eventId)) {
            Toast.makeText(this, "Missing event_id", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bindViews();
        topBar.setNavigationOnClickListener(v -> finish());

        FirestoreEventRepository.get().listenEvent(eventId, this::populate);

        btnEdit.setOnClickListener(v -> saveChanges());
        boxUpload.setOnClickListener(v -> Toast.makeText(this, "Upload poster (stub)", Toast.LENGTH_SHORT).show());
    }

    private void bindViews() {
        topBar     = findViewById(R.id.top_app_bar);
        etTitle    = findViewById(R.id.et_title);
        etDesc     = findViewById(R.id.et_desc);
        etStart    = findViewById(R.id.et_start);
        etEnd      = findViewById(R.id.et_end);
        etRegStart = findViewById(R.id.et_reg_start);
        etRegEnd   = findViewById(R.id.et_reg_end);
        etCapacity = findViewById(R.id.et_capacity);
        etPrice    = findViewById(R.id.et_price);
        switchGeo  = findViewById(R.id.switch_geo);
        btnEdit    = findViewById(R.id.btn_edit);
        boxUpload  = findViewById(R.id.box_upload);
    }

    private void populate(DocumentSnapshot d) {
        if (d == null || !d.exists()) return;

        etTitle.setText(n(d.getString("title")));
        etDesc.setText(n(d.getString("description")));

        DateFormat dfDate  = DateFormat.getDateInstance(DateFormat.SHORT);
        DateFormat dfDateTime = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
        setTextOrEmpty(etStart, d.getTimestamp("startTime"), dfDateTime);
        setTextOrEmpty(etEnd,   d.getTimestamp("endTime"),   dfDateTime);
        setTextOrEmpty(etRegStart, d.getTimestamp("registerStart"), dfDate);
        setTextOrEmpty(etRegEnd,   d.getTimestamp("registerEnd"),   dfDate);

        Object cap = d.get("capacity"); etCapacity.setText(cap == null ? "" : String.valueOf(cap));
        Object prc = d.get("price");    etPrice.setText(prc == null ? "" : String.valueOf(prc));

        Boolean geo = d.getBoolean("geolocationEnabled");
        switchGeo.setChecked(Boolean.TRUE.equals(geo));
    }

    private void setTextOrEmpty(EditText et, Timestamp ts, DateFormat fmt) {
        et.setText(ts == null ? "" : fmt.format(ts.toDate()));
    }

    private void saveChanges() {
        if (TextUtils.isEmpty(etTitle.getText().toString().trim())) {
            etTitle.setError("Required"); etTitle.requestFocus(); return;
        }

        Map<String, Object> update = new HashMap<>();
        update.put("title", etTitle.getText().toString().trim());
        update.put("description", etDesc.getText().toString().trim());
        update.put("capacity", parseInt(etCapacity.getText().toString().trim()));
        update.put("price", parseDouble(etPrice.getText().toString().trim()));
        update.put("geolocationEnabled", switchGeo.isChecked());
        update.put("updatedAt", Timestamp.now());

        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Saving...");
        pd.setCancelable(false);
        pd.show();

        btnEdit.setEnabled(false);

        final android.os.Handler handler = new android.os.Handler(getMainLooper());
        final Runnable timeout = () -> {
            if (pd.isShowing()) {
                pd.dismiss();
                btnEdit.setEnabled(true);
                Toast.makeText(this,
                        "Saving is taking too long.\nCheck network or Firestore rules.",
                        Toast.LENGTH_LONG).show();
            }
        };
        handler.postDelayed(timeout, 10_000);

        FirestoreEventRepository.get().updateEvent(eventId, update)
                .addOnSuccessListener(x -> {
                    handler.removeCallbacks(timeout);
                    if (pd.isShowing()) pd.dismiss();
                    Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    handler.removeCallbacks(timeout);
                    if (pd.isShowing()) pd.dismiss();
                    btnEdit.setEnabled(true);
                    android.util.Log.e("EditEvent", "update failed", e);
                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private static int parseInt(String s) { try { return Integer.parseInt(s); } catch (Exception e) { return 0; } }
    private static double parseDouble(String s) { try { return Double.parseDouble(s); } catch (Exception e) { return 0d; } }
    private static String n(String v){ return v == null ? "" : v; }
}
