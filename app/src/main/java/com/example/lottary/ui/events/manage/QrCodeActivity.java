package com.example.lottary.ui.events.manage;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lottary.R;
import com.example.lottary.data.FirestoreEventRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Shows a QR code for the given event and allows exporting it as PNG.
 * Payload of the QR is simply the Firestore event id.
 */
public class QrCodeActivity extends AppCompatActivity {

    private Bitmap currentQrBitmap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code);

        final String eventId = getIntent().getStringExtra(ManageEventActivity.EXTRA_EVENT_ID);

        MaterialToolbar top = findViewById(R.id.top_app_bar);
        if (top != null) {
            top.setNavigationOnClickListener(v -> finish());
            top.setTitle(R.string.qr_code);
        }

        ImageView imgQr = findViewById(R.id.qr_code_img);
        Button btnExport = findViewById(R.id.btn_export_qr_png);

        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Missing event id", Toast.LENGTH_SHORT).show();
        } else {

            DisplayMetrics dm = getResources().getDisplayMetrics();
            int size = (int) (Math.min(dm.widthPixels, dm.heightPixels) * 0.6f);
            if (size < 400) size = 400;

            currentQrBitmap = createQrBitmap(eventId, size);
            if (currentQrBitmap != null && imgQr != null) {
                imgQr.setImageBitmap(currentQrBitmap);
            }
        }

        btnExport.setOnClickListener(v -> {
            if (currentQrBitmap == null) {
                Toast.makeText(this, "QR code not ready", Toast.LENGTH_SHORT).show();
                return;
            }
            boolean ok = saveQrToGallery(currentQrBitmap);
            if (ok) {
                Toast.makeText(this, "QR code is saved to your device", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to save QR code", Toast.LENGTH_SHORT).show();
            }
        });

        if (eventId != null) {
            FirestoreEventRepository.get().listenEvent(eventId, d -> {
                String t = d != null ? d.getString("title") : null;
                if (top != null && t != null && !t.isEmpty()) {
                    top.setTitle("QR • " + t);
                }
            });
        }
    }

    /**
     * Create a square QR bitmap for the given text.
     */
    private Bitmap createQrBitmap(@NonNull String text, int sizePx) {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix matrix = writer.encode(text, BarcodeFormat.QR_CODE, sizePx, sizePx);
            Bitmap bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
            for (int x = 0; x < sizePx; x++) {
                for (int y = 0; y < sizePx; y++) {
                    bmp.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bmp;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Save the given bitmap into Pictures/EventLottery as PNG using MediaStore.
     */
    private boolean saveQrToGallery(@NonNull Bitmap bmp) {
        String fileName = "event_qr_" + System.currentTimeMillis() + ".png";

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");

        values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/EventLottery");

        Uri uri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (uri == null) return false;

        try (OutputStream os = getContentResolver().openOutputStream(uri)) {
            if (os == null) return false;
            boolean ok = bmp.compress(Bitmap.CompressFormat.PNG, 100, os);
            os.flush();
            return ok;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
