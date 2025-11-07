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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.lottary.R;
import com.google.android.material.button.MaterialButton;

// Firebase
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ImageViewerActivity extends AppCompatActivity {

    private static final String EXTRA_URL = "extra_url";
    private static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_IMAGE_ID = "extra_image_id";

    private static final String IMAGES_COLLECTION = "images";

    /** 保持原 API */
    public static void launch(Context ctx, String url, @Nullable String title) {
        Intent i = new Intent(ctx, ImageViewerActivity.class);
        i.putExtra(EXTRA_URL, url);
        i.putExtra(EXTRA_TITLE, title);
        ctx.startActivity(i);
    }

    /** 推荐使用：带上 Firestore 文档 id */
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

        imageUrl = getIntent().getStringExtra(EXTRA_URL);
        imageTitle = getIntent().getStringExtra(EXTRA_TITLE);
        imageId = getIntent().getStringExtra(EXTRA_IMAGE_ID);

        TextView tv = findViewById(R.id.tvTitle);
        ImageView iv = findViewById(R.id.ivFull);
        btnDeleteImage = findViewById(R.id.btnDeleteImage);
        progressViewer = findViewById(R.id.progressViewer);

        tv.setText(imageTitle == null ? "" : imageTitle);
        Glide.with(this).load(imageUrl)
                .placeholder(android.R.drawable.ic_menu_report_image)
                .into(iv);

        iv.setOnClickListener(v -> finish());

        // 保留你之前的长按跳详情（如果有该 Activity）
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

        if (btnDeleteImage != null) {
            btnDeleteImage.setOnClickListener(v -> showDeleteConfirmDialog());
        }

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    private void showDeleteConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.remove_image_title)
                .setMessage(R.string.remove_image_confirm)
                .setPositiveButton(R.string.yes, (d, w) -> {
                    d.dismiss();
                    performDelete();
                })
                .setNegativeButton(R.string.no, (d, w) -> d.dismiss())
                .setCancelable(true)
                .show();
    }

    private void performDelete() {
        setBusy(true);

        // 先删 Firestore 文档 —— 这是你真正想要的“删掉这条图片数据”
        if (imageId == null || imageId.isEmpty()) {
            // 没有传 id 就无法删文档；给出提示并返回
            Toast.makeText(this, "Missing document id; cannot delete.", Toast.LENGTH_LONG).show();
            setBusy(false);
            return;
        }

        db.collection(IMAGES_COLLECTION)
                .document(imageId)
                .delete()
                .addOnSuccessListener(v -> {
                    // 文档删掉了，再尝试删存储（仅当它确实是 Firebase Storage 链接）
                    maybeDeleteStorageThenFinish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            getString(R.string.delete_failed_fmt, e.getMessage() == null ? "" : e.getMessage()),
                            Toast.LENGTH_LONG).show();
                    setBusy(false);
                });
    }

    /** 只有当 URL 指向 Firebase Storage（gs:// 或 firebasestorage.googleapis.com）时才尝试删除；否则直接结束 */
    private void maybeDeleteStorageThenFinish() {
        if (imageUrl != null &&
                (imageUrl.startsWith("gs://")
                        || imageUrl.startsWith("https://firebasestorage.googleapis.com/"))) {
            try {
                StorageReference ref = storage.getReferenceFromUrl(imageUrl);
                ref.delete()
                        .addOnSuccessListener(v -> finishOk())
                        .addOnFailureListener(e -> {
                            // 存储删不掉不影响文档，给个提示即可
                            Toast.makeText(this,
                                    getString(R.string.delete_failed_fmt, e.getMessage() == null ? "" : e.getMessage()),
                                    Toast.LENGTH_LONG).show();
                            finishOk(); // 文档已删，页面还是结束
                        });
                return;
            } catch (IllegalArgumentException ignored) {
                // 不是合法的 storage URL，直接结束
            }
        }
        // 非 Storage 链接（比如 picsum 外链），直接结束
        finishOk();
    }

    private void finishOk() {
        Toast.makeText(this, R.string.image_deleted, Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    private void setBusy(boolean busy) {
        if (btnDeleteImage != null) btnDeleteImage.setEnabled(!busy);
        if (progressViewer != null) progressViewer.setVisibility(busy ? View.VISIBLE : View.GONE);
    }
}