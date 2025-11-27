/**
 * Detail screen for a single uploaded image.
 * Shows a full preview and allows deleting the image from Firestore.
 */
package com.example.lottary.ui.admin;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lottary.R;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;

public class ImageDetailActivity extends AppCompatActivity {

    public static final String EXTRA_IMAGE_ID = "extra_image_id";
    public static final String EXTRA_IMAGE_URL = "extra_image_url";
    public static final String EXTRA_IMAGE_TITLE = "extra_image_title";

    public static final int RESULT_DELETED = 1001;

    private String imageId;
    private String imageUrl;
    private String imageTitle;

    private static final String COLLECTION_IMAGES = "images";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);

        // Retrieve image info from Intent
        imageId = getIntent().getStringExtra(EXTRA_IMAGE_ID);
        imageUrl = getIntent().getStringExtra(EXTRA_IMAGE_URL);
        imageTitle = getIntent().getStringExtra(EXTRA_IMAGE_TITLE);

        // Load preview
        ImageView iv = findViewById(R.id.ivPreview);
        Glide.with(this)
                .load(imageUrl == null || imageUrl.isEmpty() ? null : imageUrl)
                .placeholder(R.drawable.placeholder_square)
                .error(R.drawable.placeholder_square)
                .into(iv);

        // Delete button
        MaterialButton btnDelete = findViewById(R.id.btnDeleteImage);
        btnDelete.setOnClickListener(v ->
                new MaterialAlertDialogBuilder(this, R.style.LotteryDialog_Admin)
                        .setTitle(R.string.remove_image)
                        .setMessage(R.string.remove_image_confirm)
                        .setPositiveButton(R.string.yes, (DialogInterface d, int w) -> doDelete())
                        .setNeutralButton(R.string.no, (d, w) -> d.dismiss())
                        .show()
        );
    }

    // Executes Firestore delete operation
    private void doDelete() {
        if (imageId == null || imageId.isEmpty()) {
            Toast.makeText(this, R.string.delete_failed, Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection(COLLECTION_IMAGES)
                .document(imageId)
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, R.string.image_deleted, Toast.LENGTH_SHORT).show();
                    setResult(RESULT_DELETED, new Intent().putExtra(EXTRA_IMAGE_ID, imageId));
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, R.string.delete_failed, Toast.LENGTH_SHORT).show()
                );
    }
}
