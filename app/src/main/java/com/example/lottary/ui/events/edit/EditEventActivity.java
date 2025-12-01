package com.example.lottary.ui.events.edit;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lottary.R;
import com.example.lottary.data.FirestoreEventRepository;
import com.example.lottary.data.GlideApp;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
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
 * - Cannot edit venue & city
 * - Limited error handling; network/rules failures are surfaced via Toast and logcat only.
 * - ProgressDialog is legacy; consider replacing with a non-blocking in-UI indicator.
 */
public class EditEventActivity extends AppCompatActivity {

    public static final String EXTRA_EVENT_ID = "event_id";

    private String eventId, currentPosterUrl;

    private MaterialToolbar topBar;
    private EditText etTitle, etDesc, etEventDate, etStart, etEnd, etRegStart, etRegEnd, etCapacity, etPrice, etVenue, etCity;
    private LinearLayout boxUpload;
    private ImageView imgPosterPreview;
    private TextView tvUploadLabel;

    private Switch switchGeo;
    private Button btnEdit;
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
        setContentView(R.layout.activity_edit_event);

        eventId = getIntent().getStringExtra(EXTRA_EVENT_ID);
        if (TextUtils.isEmpty(eventId)) {
            Toast.makeText(this, "Missing event_id", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bindViews();
        wirePickers();
        initImagePicker();

        topBar.setNavigationOnClickListener(v -> finish());

        btnEdit.setOnClickListener(v -> saveChanges());
        boxUpload.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        FirestoreEventRepository.get().listenEvent(eventId, this::populate);
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
        btnEdit    = findViewById(R.id.btn_edit);
    }

    /**
     * Handle the case of choosing an image to replace
     */
    private void initImagePicker() {
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        posterUri = uri;
                        imgPosterPreview.setVisibility(View.VISIBLE);
                        imgPosterPreview.setImageURI(uri);
                        tvUploadLabel.setText("New poster selected");
                    }
                }
        );
    }

    /**
     * Populate the event data onto appropriate fields
     * @param d event document from the data base to populate from
     */
    private void populate(DocumentSnapshot d) {
        if (d == null || !d.exists()) return;

        etTitle.setText(n(d.getString("title")));
        etDesc.setText(n(d.getString("description")));
        etCity.setText(n(d.getString("city")));
        etVenue.setText(n(d.getString("venue")));

        DateFormat dfDate  = DateFormat.getDateInstance(DateFormat.SHORT);
        DateFormat dfTime = DateFormat.getTimeInstance(DateFormat.SHORT);
        setTextOrEmpty(etStart, d.getTimestamp("startTime"), dfTime);
        setTextOrEmpty(etEnd,   d.getTimestamp("endTime"),   dfTime);
        setTextOrEmpty(etEventDate, d.getTimestamp("startTime"), dfDate);
        setTextOrEmpty(etRegStart, d.getTimestamp("registerStart"), dfDate);
        setTextOrEmpty(etRegEnd,   d.getTimestamp("registerEnd"),   dfDate);

        Object cap = d.get("capacity"); etCapacity.setText(cap == null ? "" : String.valueOf(cap));
        Object prc = d.get("price");    etPrice.setText(prc == null ? "" : String.valueOf(prc));

        Boolean geo = d.getBoolean("geolocationEnabled");
        switchGeo.setChecked(Boolean.TRUE.equals(geo));

        currentPosterUrl = n(d.getString("posterUrl"));
        if (!currentPosterUrl.isEmpty()) {
            // Reference to event poster in Cloud Storage
            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(currentPosterUrl);
            // Download directly from StorageReference using Glide
            // (See MyAppGlideModule for Loader registration)
            GlideApp.with(getApplicationContext())
                    .load(storageReference)
                    .into(imgPosterPreview);
        }
        else imgPosterPreview.setVisibility(View.GONE);
    }

    /**
     * Null string handler
     * @param et field to populate empty string to
     * @param ts time stamp
     * @param fmt time format convention to display
     */
    private void setTextOrEmpty(EditText et, Timestamp ts, DateFormat fmt) {
        et.setText(ts == null ? "" : fmt.format(ts.toDate()));
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
     * Update event using the provided info to the database
     */
    private void saveChanges() {
        if (TextUtils.isEmpty(etTitle.getText().toString().trim())) {
            etTitle.setError("Required"); etTitle.requestFocus(); return;
        }

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

        Map<String, Object> update = new HashMap<>();
        update.put("title", etTitle.getText().toString().trim());
        update.put("description", etDesc.getText().toString().trim());
        update.put("venue", etVenue.getText().toString().trim());
        update.put("city", etCity.getText().toString().trim());
        update.put("startTime", new Timestamp(startCal.getTime()));
        update.put("endTime", new Timestamp(endCal.getTime()));
        update.put("registerStart", new Timestamp(regStartCal.getTime()));
        update.put("registerEnd", new Timestamp(regEndCal.getTime()));
        update.put("capacity", parseInt(etCapacity.getText().toString().trim()));
        update.put("price", parseDouble(etPrice.getText().toString().trim()));
        update.put("geolocationEnabled", switchGeo.isChecked());
        update.put("updatedAt", Timestamp.now());

        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Saving...");
        pd.setCancelable(false);
        pd.show();

        btnEdit.setEnabled(false);

        final Handler handler = new Handler(getMainLooper());
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
                    if (posterUri != null) {
                        editPosterAndAttachToEvent(eventId);
                    }

                    Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    handler.removeCallbacks(timeout);
                    if (pd.isShowing()) pd.dismiss();
                    btnEdit.setEnabled(true);
                    Log.e("EditEvent", "update failed", e);
                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    /**
     * Handling the case where user wants to change to a different poster
     * @param eventId the event id to upload the poster for
     */
    private void editPosterAndAttachToEvent(String eventId) {
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
                                    // delete old poster if available
                                    // FirestoreImageRepository.get().deleteByStorageUrl(currentPosterUrl, );
                                    if (!currentPosterUrl.isEmpty()) storage.getReferenceFromUrl(currentPosterUrl).delete();
                                    // update poster URL in event
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
     * Convert a value from database to int
     * @param s number on the database
     * @return an int of the value
     */
    private static int parseInt(String s) { try { return Integer.parseInt(s); } catch (Exception e) { return 0; } }

    /**
     * Convert a value from database to double
     * @param s number on the database
     * @return a double of the value
     */
    private static double parseDouble(String s) { try { return Double.parseDouble(s); } catch (Exception e) { return 0d; } }

    /**
     * Null string handler.
     * @param v - a String to transform
     * @return an empty String if parameter is null, if not return the original String.
     */
    private static String n(String v){ return v == null ? "" : v; }
}
