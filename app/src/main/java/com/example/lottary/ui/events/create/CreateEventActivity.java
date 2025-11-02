package com.example.lottary.ui.events.create;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lottary.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CreateEventActivity extends AppCompatActivity {

    private MaterialToolbar topBar;
    private EditText etTitle, etDesc, etStart, etEnd, etRegStart, etRegEnd, etCapacity, etPrice;
    private LinearLayout boxUpload;
    private Switch switchGeo;
    private Button btnCreate;

    private final Calendar calStart = Calendar.getInstance();
    private final Calendar calEnd = Calendar.getInstance();
    private final Calendar calRegStart = Calendar.getInstance();
    private final Calendar calRegEnd = Calendar.getInstance();

    private final SimpleDateFormat fmtTime = new SimpleDateFormat("h:mm a", Locale.getDefault());
    private final SimpleDateFormat fmtDate = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        bindViews();
        wireTopBar();
        wirePickers();
        wireActions();
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
        boxUpload  = findViewById(R.id.box_upload);
        switchGeo  = findViewById(R.id.switch_geo);
        btnCreate  = findViewById(R.id.btn_create);
    }

    private void wireTopBar() {
        topBar.setNavigationOnClickListener(v -> finish());
    }

    private void wirePickers() {

        etStart.setText(fmtTime.format(calStart.getTime()));
        etEnd.setText(fmtTime.format(calEnd.getTime()));
        etRegStart.setText(fmtDate.format(calRegStart.getTime()));
        etRegEnd.setText(fmtDate.format(calRegEnd.getTime()));

        etStart.setFocusable(false);
        etEnd.setFocusable(false);
        etRegStart.setFocusable(false);
        etRegEnd.setFocusable(false);

        etStart.setOnClickListener(v -> pickTime(etStart, calStart));
        etEnd.setOnClickListener(v -> pickTime(etEnd, calEnd));
        etRegStart.setOnClickListener(v -> pickDate(etRegStart, calRegStart));
        etRegEnd.setOnClickListener(v -> pickDate(etRegEnd, calRegEnd));
    }

    private void wireActions() {
        boxUpload.setOnClickListener(v -> Toast.makeText(this, "Upload Poster (stub)", Toast.LENGTH_SHORT).show());
        btnCreate.setOnClickListener(v -> saveEvent());
    }

    private void pickTime(EditText target, Calendar cal) {
        new TimePickerDialog(
                this,
                (view, hour, minute) -> {
                    cal.set(Calendar.HOUR_OF_DAY, hour);
                    cal.set(Calendar.MINUTE, minute);
                    cal.set(Calendar.SECOND, 0);
                    target.setText(fmtTime.format(cal.getTime()));
                },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                false
        ).show();
    }

    private void pickDate(EditText target, Calendar cal) {
        new DatePickerDialog(
                this,
                (view, y, m, d) -> {
                    cal.set(Calendar.YEAR, y);
                    cal.set(Calendar.MONTH, m);
                    cal.set(Calendar.DAY_OF_MONTH, d);
                    target.setText(fmtDate.format(cal.getTime()));
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void saveEvent() {
        if (!require(etTitle)) return;

        int capacity = parseIntOr(etCapacity.getText().toString().trim(), 0);
        double price = parseDoubleOr(etPrice.getText().toString().trim(), 0d);

        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        if (TextUtils.isEmpty(deviceId)) deviceId = "device_demo";

        Calendar today = Calendar.getInstance();

        Calendar startCal = (Calendar) calStart.clone();
        startCal.set(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));

        Calendar endCal = (Calendar) calEnd.clone();
        endCal.set(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));

        Calendar regStartCal = (Calendar) calRegStart.clone();
        regStartCal.set(Calendar.HOUR_OF_DAY, 0); regStartCal.set(Calendar.MINUTE, 0); regStartCal.set(Calendar.SECOND, 0);

        Calendar regEndCal = (Calendar) calRegEnd.clone();
        regEndCal.set(Calendar.HOUR_OF_DAY, 0); regEndCal.set(Calendar.MINUTE, 0); regEndCal.set(Calendar.SECOND, 0);

        Map<String, Object> fields = new HashMap<>();
        fields.put("title", etTitle.getText().toString().trim());
        fields.put("description", etDesc.getText().toString().trim());
        fields.put("startTime", new Timestamp(startCal.getTime()));
        fields.put("endTime", new Timestamp(endCal.getTime()));
        fields.put("registerStart", new Timestamp(regStartCal.getTime()));
        fields.put("registerEnd", new Timestamp(regEndCal.getTime()));
        fields.put("capacity", capacity);
        fields.put("price", price);
        fields.put("geolocationEnabled", switchGeo.isChecked());

        fields.put("creatorDeviceId", deviceId);
        fields.put("organizerId", deviceId);
        fields.put("createdAt", Timestamp.now());

        btnCreate.setEnabled(false);
        FirestoreEventRepository.get()
                .createEvent(fields)
                .addOnSuccessListener(ref -> {
                    Toast.makeText(this, "Created: " + ref.getId(), Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Create failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    btnCreate.setEnabled(true);
                });
    }

    private boolean require(EditText et) {
        if (TextUtils.isEmpty(et.getText().toString().trim())) {
            et.setError("Required");
            et.requestFocus();
            return false;
        }
        return true;
    }

    private int parseIntOr(String s, int def) { try { return Integer.parseInt(s); } catch (Exception e) { return def; } }
    private double parseDoubleOr(String s, double def) { try { return Double.parseDouble(s); } catch (Exception e) { return def; } }
}
