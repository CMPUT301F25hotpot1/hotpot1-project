/**
 * Detail screen for a single uploaded image.
 * Shows a full preview and allows deleting the image that is attached
 * to an Event (via the event's posterUrl field).
 */
package com.example.lottary.ui.admin;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.lottary.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Shows a single image in full size and lets the admin delete it.
 * Deletion means:
 *  1) Clear the corresponding Event's posterUrl field in Firestore.
 *  2) Best-effort delete of the underlying Firebase Storage object.
 */
public class ImageDetailActivity extends AppCompatActivity {

    public static final String EXTRA_IMAGE_ID = "extra_image_id";       // here: eventId
    public static final String EXTRA_IMAGE_URL = "extra_image_url";
    public static final String EXTRA_IMAGE_TITLE = "extra_image_title";

    public static final int RESULT_DELETED = 1001;

    private String imageId;      // actually the eventId
    private String imageUrl;
    private String imageTitle;

    // Collection that holds events with a posterUrl field
    private static final String COLLECTION_EVENTS = "events";
    private static final String FIELD_POSTER_URL   = "posterUrl";

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
                        .setPositiveButton(R.string.yes,
                                (DialogInterface d, int which) -> doDelete())
                        .setNeutralButton(R.string.no,
                                (d, which) -> d.dismiss())
                        .show()
        );
    }

    /**
     * Main delete flow:
     * 1) Clear the posterUrl on the Event document.
     * 2) Try to delete the corresponding Firebase Storage object.
     * 3) Finish this Activity and notify caller.
     */
    private void doDelete() {
        if (imageId == null || imageId.isEmpty()) {
            Toast.makeText(this, R.string.delete_failed, Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Step 1: clear posterUrl on the Event document.
        db.collection(COLLECTION_EVENTS)
                .document(imageId)
                .update(FIELD_POSTER_URL, null)  // or "" if you prefer empty string
                .addOnSuccessListener(unused -> {
                    // Step 2: best-effort delete from Storage.
                    deleteFromStorageAndFinish();
                })
                .addOnFailureListener(e -> {
                    // If we cannot clear the field, consider the whole delete failed.
                    Toast.makeText(this, R.string.delete_failed, Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Tries to delete the underlying image file in Firebase Storage.
     * If this fails (for example, object already removed), we still treat
     * the delete as successful because Firestore state is already correct.
     */
    private void deleteFromStorageAndFinish() {
        // Nothing to delete in Storage -> just finish with success.
        if (imageUrl == null || imageUrl.isEmpty()) {
            finishWithSuccess();
            return;
        }

        try {
            StorageReference ref = FirebaseStorage.getInstance()
                    .getReferenceFromUrl(imageUrl);

            ref.delete()
                    .addOnSuccessListener(unused -> {
                        // Storage deleted successfully.
                        finishWithSuccess();
                    })
                    .addOnFailureListener(e -> {
                        // Storage delete failed (e.g. object missing or permission),
                        // but Firestore posterUrl is already cleared, so we still
                        // treat this as success from the UI perspective.
                        finishWithSuccess();
                    });

        } catch (IllegalArgumentException e) {
            // imageUrl was not a valid Firebase Storage URL; ignore and finish.
            finishWithSuccess();
        }
    }

    /**
     * Common helper: show success toast, set result, and close the Activity.
     */
    private void finishWithSuccess() {
        Toast.makeText(this, R.string.image_deleted, Toast.LENGTH_SHORT).show();
        Intent result = new Intent().putExtra(EXTRA_IMAGE_ID, imageId);
        setResult(RESULT_DELETED, result);
        finish();
    }
}
