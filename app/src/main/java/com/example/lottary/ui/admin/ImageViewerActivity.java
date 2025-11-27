/**
 * Full-screen image viewer for admin.
 * Shows a large preview, supports long-press to open detail,
 * and allows deleting both Firestore doc and optional Storage file.
 */
package com.example.lottary.ui.admin;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.lottary.R;
import com.google.android.material.button.MaterialButton;

// Firebase
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ImageViewerActivity extends AppCompatActivity {

    private static final String EXTRA_URL = "extra_url";
    private static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_IMAGE_ID = "extra_image_id";

    private static final String IMAGES_COLLECTION = "images";

    // Original API
    public static void launch(Context ctx, String url, @Nullable String title) {
        Intent i = new Intent(ctx, ImageViewerActivity.class);
        i.putExtra(EXTRA_URL, url);
        i.putExtra(EXTRA_TITLE, title);
        ctx.startActivity(i);
    }

    // Preferred API: includes Firestore doc id
    public static void launch(Context ctx, String url, @Nullable String title, @Nullable String imageId) {
        Intent i = new Intent(ctx, ImageViewerActivity.class);
        i.putExtra(EXTRA_URL, url);
        i.putExtra(EXTRA_TITLE, title);
        if (imageId != null) i.putExtra(EXTRA_IMAGE_ID, imageId);
        ctx.startActivity(i);
    }

    private String imageId;
    private String imageUrl;
    private String imageTitle;

    private MaterialButton btnDeleteImage;
    private ProgressBar progressViewer;

    private FirebaseFirestore db;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        // Get extras
        imageUrl = getIntent().getStringExtra(EXTRA_URL);
        imageTitle = getIntent().getStringExtra(EXTRA_TITLE);
        imageId = getIntent().getStringExtra(EXTRA_IMAGE_ID);

        TextView tv = findViewById(R.id.tv_title);
        ImageView iv = findViewById(R.id.ivFull);
        btnDeleteImage = findViewById(R.id.btnDeleteImage);
        progressViewer = findViewById(R.id.progressViewer);

        // Title text
        tv.setText(imageTitle == null ? "" : imageTitle);

        // Load full-size preview
        Glide.with(this).load(imageUrl)
                .placeholder(android.R.drawable.ic_menu_report_image)
                .into(iv);

        // Tap to exit
        iv.setOnClickListener(v -> finish());

        // Long press → open detail view
        iv.setOnLongClickListener(v -> {
            if (imageId != null && !imageId.isEmpty()) {
                Intent detail = new Intent(this, ImageDetailActivity.class);
                detail.putExtra(ImageDetailActivity.EXTRA_IMAGE_ID, imageId);
                detail.putExtra(ImageDetailActivity.EXTRA_IMAGE_URL, imageUrl);
                detail.putExtra(ImageDetailActivity.EXTRA_IMAGE_TITLE, imageTitle);
                startActivity(detail);
                return true;
            }
            return false;
        });

        // Delete button
        if (btnDeleteImage != null) {
            btnDeleteImage.setOnClickListener(v -> showDeleteConfirmDialog());
        }

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    // Delete confirmation dialog
    private void showDeleteConfirmDialog() {
        new MaterialAlertDialogBuilder(this, R.style.LotteryDialog_Admin)
                .setTitle(R.string.remove_image)
                .setMessage(R.string.remove_image_confirm)
                .setPositiveButton(R.string.yes, (d, w) -> {
                    d.dismiss();
                    performDelete();
                })
                .setNeutralButton(R.string.no, (d, w) -> d.dismiss())
                .setCancelable(true)
                .show();
    }

    // Start Firestore delete
    private void performDelete() {
        setBusy(true);

        if (imageId == null || imageId.isEmpty()) {
            Toast.makeText(this, "Missing document id; cannot delete.", Toast.LENGTH_LONG).show();
            setBusy(false);
            return;
        }

        db.collection(IMAGES_COLLECTION)
                .document(imageId)
                .delete()
                .addOnSuccessListener(v -> {
                    // Then try Storage delete if URL points to Firebase Storage
                    maybeDeleteStorageThenFinish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            getString(R.string.delete_failed_fmt, e.getMessage() == null ? "" : e.getMessage()),
                            Toast.LENGTH_LONG).show();
                    setBusy(false);
                });
    }

    // Delete from Storage only if URL is a Firebase Storage link
    private void maybeDeleteStorageThenFinish() {
        if (imageUrl != null &&
                (imageUrl.startsWith("gs://")
                        || imageUrl.startsWith("https://firebasestorage.googleapis.com/"))) {

            try {
                StorageReference ref = storage.getReferenceFromUrl(imageUrl);
                ref.delete()
                        .addOnSuccessListener(v -> finishOk())
                        .addOnFailureListener(e -> {
                            Toast.makeText(this,
                                    getString(R.string.delete_failed_fmt, e.getMessage() == null ? "" : e.getMessage()),
                                    Toast.LENGTH_LONG).show();
                            finishOk(); // Still complete since Firestore doc is deleted
                        });
                return;
            } catch (IllegalArgumentException ignored) {
                // Not a Storage link → skip
            }
        }

        // Not a Firebase Storage URL → no need to delete file
        finishOk();
    }

    // Finish activity after successful deletion
    private void finishOk() {
        Toast.makeText(this, R.string.image_deleted, Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    // Toggle loading UI
    private void setBusy(boolean busy) {
        if (btnDeleteImage != null) btnDeleteImage.setEnabled(!busy);
        if (progressViewer != null) progressViewer.setVisibility(busy ? View.VISIBLE : View.GONE);
    }
}
