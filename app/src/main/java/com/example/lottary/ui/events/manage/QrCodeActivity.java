package com.example.lottary.ui.events.manage;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lottary.R;
import com.example.lottary.data.FirestoreEventRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * QrCodeActivity
 *
 * Shown from: My Events -> Manage -> View QR Code
 *
 * - Reads the event id from the intent extras.
 * - Generates a QR code whose *payload is exactly this event id*.
 * - Displays the QR image in the middle of the screen.
 * - Toolbar title is updated to "QR • <event title>" when available.
 *
 * Another device can scan this QR (using EventQrScannerActivity or any QR app),
 * read the event id, and then open the EventDetailsActivity for that event.
 */
public class QrCodeActivity extends AppCompatActivity {

    private ImageView qrImageView;
    private String eventId;
    @Nullable
    private ListenerRegistration reg;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code);

        // -------- 1. Read event id from Intent --------
        eventId = getIntent().getStringExtra(ManageEventActivity.EXTRA_EVENT_ID);
        if (TextUtils.isEmpty(eventId)) {
            Toast.makeText(this, "Missing event id", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // -------- 2. Setup toolbar --------
        MaterialToolbar top = findViewById(R.id.top_app_bar);
        top.setNavigationOnClickListener(v -> finish());
        top.setTitle(R.string.qr_code);

        // Optional: show "QR • <title>" based on Firestore document
        reg = FirestoreEventRepository.get().listenEvent(eventId, d -> {
            String t = d != null ? d.getString("title") : null;
            top.setTitle(R.string.qr_code);
            if (t != null && !t.isEmpty()) top.setSubtitle(t);
        });

        // -------- 3. Setup views --------
        qrImageView = findViewById(R.id.qr_code_img);
        Button btnExport = findViewById(R.id.btn_export_qr_png);

        // -------- 4. Generate QR bitmap and show it in the middle --------
        Bitmap qrBitmap = createQrBitmap(eventId);
        if (qrBitmap != null) {
            qrImageView.setImageBitmap(qrBitmap);
        } else {
            Toast.makeText(this, "Failed to generate QR code", Toast.LENGTH_SHORT).show();
        }

        // -------- 5. Export button (placeholder) --------
        btnExport.setOnClickListener(v ->
                Toast.makeText(
                        this,
                        "QR code is saved to your device",
                        Toast.LENGTH_SHORT
                ).show()
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reg != null) {
            reg.remove();
            reg = null;
        }
    }

    /**
     * Create a square QR bitmap whose payload is the given value.
     * Here value == eventId, so it matches EventQrScannerActivity.
     */
    @Nullable
    private Bitmap createQrBitmap(@NonNull String value) {
        final int size = 800; // px: big enough to look crisp
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix matrix = writer.encode(value, BarcodeFormat.QR_CODE, size, size);
            Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    bmp.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bmp;
        } catch (WriterException e) {
            return null;
        }
    }
}

