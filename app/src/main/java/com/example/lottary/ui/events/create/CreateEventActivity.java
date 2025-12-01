package com.example.lottary.ui.events.create;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lottary.R;
import com.example.lottary.data.FirestoreEventRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.Timestamp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * CreateEventActivity
 *
 * Purpose:
 * Screen for creating. It allows the user to add event attributes (title, description,
 * city, venue, relevant dates, capacity, price, poster, and geolocation flag),
 * and submits a new event back to Firestore.
 *
 * Design Role:
 * - "Create” part of the CRUD flow for events.
 * - Uses FirestoreEventRepository to listen to a single event and to push creations.
 * - Displays a modal ProgressDialog while saving and disables the create button to avoid duplicates.
 *
 * Outstanding Issues / TODOs:
 * - No cross-field validation for date/time consistency.
 * - Limited error handling; network/rules failures are surfaced via Toast and logcat only.
 * - ProgressDialog is legacy; consider replacing with a non-blocking in-UI indicator.
 */
public class CreateEventActivity extends AppCompatActivity {

    private MaterialToolbar topBar;
    private EditText etTitle, etDesc, etEventDate,
            etStart, etEnd, etRegStart, etRegEnd,
            etCapacity, etPrice, etVenue, etCity;
    private LinearLayout boxUpload;
    private ImageView imgPosterPreview;
    private TextView tvUploadLabel;
    private Switch switchGeo;
    private Button btnCreate;

    private final Calendar calEventDate = Calendar.getInstance();
    private final Calendar calStart = Calendar.getInstance();
    private final Calendar calEnd = Calendar.getInstance();
    private final Calendar calRegStart = Calendar.getInstance();
    private final Calendar calRegEnd = Calendar.getInstance();

    private final SimpleDateFormat fmtTime = new SimpleDateFormat("h:mm a", Locale.getDefault());
    private final SimpleDateFormat fmtDate = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());


    private Uri posterUri = null;
    private ActivityResultLauncher<String> pickImageLauncher;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        bindViews();
        initImagePicker();
        wireTopBar();
        wirePickers();
        wireActions();
    }

    /**
     * Bind views to their respective XML design
     */
    private void bindViews() {
        topBar     = findViewById(R.id.top_app_bar);
        etTitle    = findViewById(R.id.et_title);
        etDesc     = findViewById(R.id.et_desc);
        etEventDate = findViewById(R.id.et_event_start);
        etStart    = findViewById(R.id.et_start);
        etEnd      = findViewById(R.id.et_end);
        etRegStart = findViewById(R.id.et_reg_start);
        etRegEnd   = findViewById(R.id.et_reg_end);
        etVenue    = findViewById(R.id.et_venue);
        etCity     = findViewById(R.id.et_city);
        etCapacity = findViewById(R.id.et_capacity);
        etPrice    = findViewById(R.id.et_price);
        boxUpload  = findViewById(R.id.box_upload);
        imgPosterPreview = findViewById(R.id.img_poster_preview);
        tvUploadLabel    = findViewById(R.id.tv_upload_label);
        switchGeo  = findViewById(R.id.switch_geo);
        btnCreate  = findViewById(R.id.btn_create);
    }

    /**
     * Handle the case of choosing an image to upload
     */
    private void initImagePicker() {
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        posterUri = uri;
                        imgPosterPreview.setVisibility(View.VISIBLE);
                        imgPosterPreview.setImageURI(uri);
                        tvUploadLabel.setText("Poster selected");
                    }
                }
        );
    }

    /**
     * Set click listeners to top bar
     */
    private void wireTopBar() {
        topBar.setNavigationOnClickListener(v -> finish());
    }

    /**
     * Setup calendar pickers and listeners to their respective fields
     */
    private void wirePickers() {

        etEventDate.setText(fmtDate.format(calEventDate.getTime()));
        etStart.setText(fmtTime.format(calStart.getTime()));
        etEnd.setText(fmtTime.format(calEnd.getTime()));
        etRegStart.setText(fmtDate.format(calRegStart.getTime()));
        etRegEnd.setText(fmtDate.format(calRegEnd.getTime()));

        etEventDate.setFocusable(false);
        etStart.setFocusable(false);
        etEnd.setFocusable(false);
        etRegStart.setFocusable(false);
        etRegEnd.setFocusable(false);

        etEventDate.setOnClickListener(v -> pickDate(etEventDate, calEventDate));
        etStart.setOnClickListener(v -> pickTime(etStart, calStart));
        etEnd.setOnClickListener(v -> pickTime(etEnd, calEnd));
        etRegStart.setOnClickListener(v -> pickDate(etRegStart, calRegStart));
        etRegEnd.setOnClickListener(v -> pickDate(etRegEnd, calRegEnd));
    }

    /**
     * Set click listeners to major actions (upload image and submit created event
     */
    private void wireActions() {
        boxUpload.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        btnCreate.setOnClickListener(v -> saveEvent());
    }

    /**
     * Handling date-picking cases
     * @param target the view to return the date values being pick
     * @param cal The calendar picker associated with the view
     */
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

    /**
     * Handling date-picking cases
     * @param target the view to return the date values being pick
     * @param cal The calendar picker associated with the view
     */
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

    /**
     * Create and save event using the provided info to the database
     */
    private void saveEvent() {
        if (!require(etTitle)) return;

        int capacity = parseIntOr(etCapacity.getText().toString().trim(), 0);
        double price = parseDoubleOr(etPrice.getText().toString().trim(), 0d);

        String deviceId = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
        if (TextUtils.isEmpty(deviceId)) deviceId = "device_demo";

        Calendar startCal = Calendar.getInstance();
        startCal.set(
                calEventDate.get(Calendar.YEAR),
                calEventDate.get(Calendar.MONTH),
                calEventDate.get(Calendar.DAY_OF_MONTH),
                calStart.get(Calendar.HOUR_OF_DAY),
                calStart.get(Calendar.MINUTE),
                0
        );

        Calendar endCal = Calendar.getInstance();
        endCal.set(
                calEventDate.get(Calendar.YEAR),
                calEventDate.get(Calendar.MONTH),
                calEventDate.get(Calendar.DAY_OF_MONTH),
                calEnd.get(Calendar.HOUR_OF_DAY),
                calEnd.get(Calendar.MINUTE),
                0
        );

        Calendar regStartCal = (Calendar) calRegStart.clone();
        regStartCal.set(Calendar.HOUR_OF_DAY, 0);
        regStartCal.set(Calendar.MINUTE, 0);
        regStartCal.set(Calendar.SECOND, 0);

        Calendar regEndCal = (Calendar) calRegEnd.clone();
        regEndCal.set(Calendar.HOUR_OF_DAY, 0);
        regEndCal.set(Calendar.MINUTE, 0);
        regEndCal.set(Calendar.SECOND, 0);

        Map<String, Object> fields = new HashMap<>();
        fields.put("title", etTitle.getText().toString().trim());
        fields.put("description", etDesc.getText().toString().trim());
        fields.put("venue", etVenue.getText().toString().trim());
        fields.put("city", etCity.getText().toString().trim());
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
                    String eventId = ref.getId();

                    if (posterUri != null) {
                        uploadPosterAndAttachToEvent(eventId);
                    }

                    Toast.makeText(this, "Created: " + eventId, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Create failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    btnCreate.setEnabled(true);
                });
    }

    /**
     * Handling the case where user wants to upload a poster
     * @param eventId the event id to upload the poster for
     */
    private void uploadPosterAndAttachToEvent(String eventId) {
        if (posterUri == null) return;

        FirebaseStorage storage = FirebaseStorage.getInstance();
        String fileName = eventId + "_" + System.currentTimeMillis() + ".jpg";
        StorageReference ref = storage.getReference()
                .child("event_posters")
                .child(fileName);

        ref.putFile(posterUri)
                .addOnSuccessListener(taskSnapshot ->
                        ref.getDownloadUrl()
                                .addOnSuccessListener(uri -> {
                                    Map<String, Object> update = new HashMap<>();
                                    update.put("posterUrl", uri.toString());
                                    FirestoreEventRepository.get().updateEvent(eventId, update);
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this,
                                                "Poster URL fetch failed: " + e.getMessage(),
                                                Toast.LENGTH_SHORT).show()
                                )
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Poster upload failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }

    /**
     * Forces the user to provide input on the given field
     * @param et - the EditText field that requires a String input
     */
    private boolean require(EditText et) {
        if (TextUtils.isEmpty(et.getText().toString().trim())) {
            et.setError("Required");
            et.requestFocus();
            return false;
        }
        return true;
    }

    /**
     * Convert a value from database to int
     * @param s number on the database
     * @param def default value to return to if an error occur
     * @return
     */
    private int parseIntOr(String s, int def) {
        try { return Integer.parseInt(s); }
        catch (Exception e) { return def; }
    }

    /**
     * Convert a value from database to double
     * @param s number on the database
     * @param def default value to return to if an error occur
     * @return
     */
    private double parseDoubleOr(String s, double def) {
        try { return Double.parseDouble(s); }
        catch (Exception e) { return def; }
    }
}
