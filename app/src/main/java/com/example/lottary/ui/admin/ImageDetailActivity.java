/**
 * Detail screen for a single uploaded image.
 *
 * High-level purpose:
 * - Shows a full preview of an uploaded image (poster).
 * - Displays the image title in the description bar.
 * - Lets the admin close this screen with an "X" button.
 * - Lets the admin delete the image from the `images` collection.
 * - When deleting, also finds all `events` whose `posterUrl` equals this
 *   image's URL and clears their `posterUrl` field (so no event still points
 *   to a non-existent poster).
 *
 * This activity only handles UI and Firestore side-effects for a single image.
 * It reports back to the calling Activity via RESULT_DELETED so the caller
 * can refresh or remove this item from its own list if needed.
 */
package com.example.lottary.ui.admin;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.lottary.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

public class ImageDetailActivity extends AppCompatActivity {

    // --------- Intent extras ---------
    // Keys used by the caller to pass the image's basic information.
    public static final String EXTRA_IMAGE_ID    = "extra_image_id";
    public static final String EXTRA_IMAGE_URL   = "extra_image_url";
    public static final String EXTRA_IMAGE_TITLE = "extra_image_title";

    // --------- Result code ---------
    // Returned to the caller when this image has been deleted successfully.
    public static final int RESULT_DELETED = 1001;

    // --------- Fields for this image ---------
    // Local copies of the image metadata for this screen.
    private String imageId;
    private String imageUrl;
    private String imageTitle;

    // --------- Firestore collections ---------
    // Collection names used in all Firestore operations.
    private static final String COLLECTION_IMAGES = "images";
    private static final String COLLECTION_EVENTS = "events";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);

        // ----- Retrieve extras -----
        // Pull the image metadata from the Intent that started this Activity.
        imageId    = getIntent().getStringExtra(EXTRA_IMAGE_ID);
        imageUrl   = getIntent().getStringExtra(EXTRA_IMAGE_URL);
        imageTitle = getIntent().getStringExtra(EXTRA_IMAGE_TITLE);

        // ----- Description bar: show poster name -----
        // The text bar at the top shows the human-readable name of this image.
        TextView tvTitle = findViewById(R.id.tv_title);
        if (imageTitle != null && !imageTitle.trim().isEmpty()) {
            tvTitle.setText(imageTitle);
        } else {
            // If there is no title, fall back to a generic hint.
            tvTitle.setText(R.string.hint_desc);
        }

        // ----- Preview image -----
        // Display the actual image content in the large preview area.
        ImageView iv = findViewById(R.id.ivPreview);
        Glide.with(this)
                .load(imageUrl == null || imageUrl.isEmpty() ? null : imageUrl)
                .placeholder(R.drawable.placeholder_square)
                .error(R.drawable.placeholder_square)
                .into(iv);

        // ----- Top-right close "X" -----
        // Simple close action: finishes this Activity without any side effects.
        ImageButton btnClose = findViewById(R.id.btnClose);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> finish());
        }

        // ----- Delete button: confirmation dialog -----
        // When the admin taps "Remove Image", show a confirm dialog first.
        MaterialButton btnDelete = findViewById(R.id.btnDeleteImage);
        btnDelete.setOnClickListener(v ->
                new MaterialAlertDialogBuilder(ImageDetailActivity.this,
                        R.style.LotteryDialog_Admin)
                        .setTitle(R.string.remove_image)
                        .setMessage(R.string.remove_image_confirm)
                        // User cancelled: just close the detail screen.
                        .setNeutralButton(R.string.no, (dialog, which) -> {
                            dialog.dismiss();
                            finish();
                        })
                        // User confirmed: proceed with Firestore deletion & cleanup.
                        .setPositiveButton(R.string.yes,
                                (DialogInterface d, int w) -> doDelete())
                        // Treat dialog cancellation as cancelling the delete and leaving the screen.
                        .setOnCancelListener(dialog -> finish())
                        .show()
        );
    }

    /**
     * Performs the actual deletion workflow:
     * 1) Query the `events` collection for all documents whose `posterUrl`
     *    matches this image's URL.
     * 2) For each matching event, clear its `posterUrl` field (set to "") so
     *    no event still references this deleted poster.
     * 3) Delete this image document from the `images` collection.
     * 4) Commit everything in a single WriteBatch so either all succeed or all fail.
     * 5) On success, show a toast and return RESULT_DELETED to the caller.
     */
    private void doDelete() {
        if (imageId == null || imageId.isEmpty()) {
            Toast.makeText(this, R.string.delete_failed, Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Step 1: find all events that currently use this image as their poster.
        db.collection(COLLECTION_EVENTS)
                .whereEqualTo("posterUrl", imageUrl)   // ⚠️ 用 posterUrl 关联
                .get()
                .addOnSuccessListener(snap -> {

                    // Prepare a batch so updates + delete happen together.
                    WriteBatch batch = db.batch();

                    // Step 2: for each event, clear its posterUrl field.
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        DocumentReference ref = doc.getReference();

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("posterUrl", "");  // or null if you prefer

                        batch.update(ref, updates);
                    }

                    // Step 3: delete the image document from the images collection.
                    DocumentReference imageRef =
                            db.collection(COLLECTION_IMAGES).document(imageId);
                    batch.delete(imageRef);

                    // Step 4: commit the batch (all updates + image delete).
                    batch.commit()
                            .addOnSuccessListener(unused -> {
                                // Notify the user and the caller that deletion succeeded.
                                Toast.makeText(this,
                                        R.string.image_deleted,
                                        Toast.LENGTH_SHORT).show();

                                // Step 5: send back the deleted imageId as a result.
                                Intent data = new Intent()
                                        .putExtra(EXTRA_IMAGE_ID, imageId);
                                setResult(RESULT_DELETED, data);

                                finish();
                            })
                            .addOnFailureListener(e -> {
                                // Batch failed: let the user know.
                                Toast.makeText(this,
                                        R.string.delete_failed,
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e ->
                        // Initial query failed: cannot proceed with cleanup/delete.
                        Toast.makeText(this,
                                R.string.delete_failed,
                                Toast.LENGTH_SHORT).show()
                );
    }
}
