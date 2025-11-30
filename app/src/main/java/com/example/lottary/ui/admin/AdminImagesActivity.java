/**
 * Admin screen for managing all uploaded images.
 * Uses AdminRepository images (built from events.posterUrl),
 * supports real-time updates, searching, sorting.
 *
 * Tap an item to open ImageDetailActivity, where the admin
 * can preview and delete the image. Long-press delete is
 * no longer used.
 */
package com.example.lottary.ui.admin;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottary.R;
import com.example.lottary.data.AdminRepository;
import com.example.lottary.data.Image;
import com.example.lottary.ui.admin.adapters.ImageGridAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdminImagesActivity extends AppCompatActivity {

    private RecyclerView rv;
    private EditText etSearch;
    private Button btnSearch, btnSort;
    private ProgressBar progress;

    private final List<Image> all = new ArrayList<>();
    private final List<Image> current = new ArrayList<>();
    private ImageGridAdapter adapter;
    private String query = "";

    private AdminRepository adminRepo;

    private enum SortMode {
        TIME_DESC,
        TITLE_ASC
    }

    private SortMode sortMode = SortMode.TIME_DESC;

    // Used when opening ImageDetailActivity (tap item to delete there)
    private final ActivityResultLauncher<Intent> detailLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == ImageDetailActivity.RESULT_DELETED) {
                    // AdminRepository listens to Firestore and will refresh images,
                    // so here a local re-render is enough.
                    render();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_images);

        rv = findViewById(R.id.admin_images_list);
        etSearch = findViewById(R.id.etSearchAdminImages);
        btnSearch = findViewById(R.id.btnSearchAdminImages);
        btnSort = findViewById(R.id.btnSortAdminImages);
        progress = findViewById(R.id.progressAdminImages);

        setupBottomNav();
        updateSortButtonLabel();

        // Grid + adapter
        rv.setLayoutManager(new GridLayoutManager(this, 3));
        rv.addItemDecoration(new SpacesItemDecoration(
                getResources().getDimensionPixelSize(R.dimen.grid_gap)));

        // Tap item -> open ImageDetailActivity (preview + delete)
        adapter = new ImageGridAdapter(this::launchDetail);
        adapter.enableStableIds(true);

        // Long-press delete from the grid is no longer used.
        // adapter.setOnItemLongClick(this::showDeleteDialog);

        rv.setAdapter(adapter);

        // Initial skeleton while waiting for data
        showSkeleton(8);

        // Search button
        btnSearch.setOnClickListener(v -> doSearch());

        // Keyboard search / enter
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || (event != null
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_DOWN)) {
                doSearch();
                return true;
            }
            return false;
        });

        // Smooth, live filtering as user types
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                query = s.toString().trim();
                render();
            }
        });

        // Sort popup (English)
        btnSort.setOnClickListener(v -> {
            CharSequence[] items = new CharSequence[]{
                    "Sort by time (newest first)",
                    "Sort by title (A–Z)"
            };
            new AlertDialog.Builder(this)
                    .setTitle("Sort options")
                    .setItems(items, (dialog, which) -> {
                        if (which == 0) {
                            sortMode = SortMode.TIME_DESC;
                        } else {
                            sortMode = SortMode.TITLE_ASC;
                        }
                        updateSortButtonLabel();
                        render();
                    })
                    .show();
        });

        // Use AdminRepository images (rebuilt from events.posterUrl)
        adminRepo = AdminRepository.get();
        adminRepo.images().observe(this, images -> {
            progress.setVisibility(View.GONE);
            all.clear();
            if (images != null) {
                all.addAll(images);
            }
            query = etSearch.getText().toString().trim();
            render();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        progress.setVisibility(View.VISIBLE);
        // Start listening for events; repository will rebuild images list.
        adminRepo.startAdminEventsRealtime();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Stop Firestore listener when leaving this screen.
        adminRepo.stopAdminEventsRealtime();
    }

    // Unified search trigger
    private void doSearch() {
        query = etSearch.getText().toString().trim();
        render();
    }

    // Build current list from "all" using query + sortMode
    private void render() {
        current.clear();

        if (TextUtils.isEmpty(query)) {
            current.addAll(all);
        } else {
            String q = query.toLowerCase();
            for (Image img : all) {
                String t = img.getTitle() == null ? "" : img.getTitle();
                if (t.toLowerCase().contains(q)) {
                    current.add(img);
                }
            }
        }

        if (sortMode == SortMode.TIME_DESC) {
            sortByLatest(current);
        } else {
            sortByTitle(current);
        }

        adapter.submitList(new ArrayList<>(current));
    }

    // Sort by createdAt (newest first)
    private static void sortByLatest(List<Image> list) {
        Collections.sort(list, (a, b) -> {
            long ta = a.getCreatedAt() == null ? 0 : a.getCreatedAt().getSeconds();
            long tb = b.getCreatedAt() == null ? 0 : b.getCreatedAt().getSeconds();
            return Long.compare(tb, ta);
        });
    }

    // Sort by title A–Z
    private static void sortByTitle(List<Image> list) {
        Collections.sort(list, (a, b) -> {
            String ta = a.getTitle() == null ? "" : a.getTitle();
            String tb = b.getTitle() == null ? "" : b.getTitle();
            return ta.compareToIgnoreCase(tb);
        });
    }

    private void updateSortButtonLabel() {
        if (btnSort == null) return;
        if (sortMode == SortMode.TIME_DESC) {
            btnSort.setText("Sort: Time");
        } else {
            btnSort.setText("Sort: Title");
        }
    }

    // Skeleton placeholders
    private void showSkeleton(int count) {
        List<Image> skeleton = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Image img = new Image();
            img.setId("skeleton_" + i);
            img.setUrl("");
            img.setTitle("");
            img.setCreatedAt(Timestamp.now());
            skeleton.add(img);
        }
        adapter.submitList(skeleton);
    }

    // Bottom navigation
    private void setupBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottomNavAdmin);
        nav.setSelectedItemId(R.id.nav_admin_images);

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_admin_images) return true;

            if (id == R.id.nav_admin_events) {
                startActivity(new Intent(this, AdminEventsActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }

            if (id == R.id.nav_admin_users) {
                startActivity(new Intent(this, AdminUsersActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }

            if (id == R.id.nav_admin_profile) {
                startActivity(new Intent(this, AdminProfileActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }

            return false;
        });
    }

    // Simple spacing decoration for the grid
    static class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private final int space;
        SpacesItemDecoration(int s) { space = s; }

        @Override
        public void getItemOffsets(@NonNull Rect outRect,
                                   @NonNull View view,
                                   @NonNull RecyclerView parent,
                                   @NonNull RecyclerView.State state) {
            outRect.left = space;
            outRect.right = space;
            outRect.top = space;
            outRect.bottom = space;
        }
    }

    // Open detail screen (preview + delete)
    private void launchDetail(Image img) {
        Intent i = new Intent(this, ImageDetailActivity.class);
        i.putExtra(ImageDetailActivity.EXTRA_IMAGE_ID, img.getId());
        i.putExtra(ImageDetailActivity.EXTRA_IMAGE_URL, img.getUrl());
        i.putExtra(ImageDetailActivity.EXTRA_IMAGE_TITLE, img.getTitle());
        detailLauncher.launch(i);
    }

    // --- The methods below are kept for reference but no longer used
    // --- because delete is now done in ImageDetailActivity.

    // Long-press delete confirmation (unused for now)
    private void showDeleteDialog(Image img) {
        new AlertDialog.Builder(this)
                .setTitle("Remove image")
                .setMessage("Remove poster for this event?\n\n" + img.getTitle())
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Remove", (dialog, which) -> deleteImagePoster(img))
                .show();
    }

    // Remove poster: delete Storage file + clear events/{id}.posterUrl (unused)
    private void deleteImagePoster(Image img) {
        String eventId = img.getId();
        String url = img.getUrl();

        // Try to delete Storage file; ignore failures.
        if (!TextUtils.isEmpty(url)) {
            try {
                FirebaseStorage.getInstance()
                        .getReferenceFromUrl(url)
                        .delete()
                        .addOnFailureListener(e ->
                                Log.w("AdminImages", "Storage delete failed (ignored)", e));
            } catch (Exception e) {
                Log.w("AdminImages", "Bad storage url (ignored): " + url, e);
            }
        }

        // Clear posterUrl field in the corresponding event document.
        FirebaseFirestore.getInstance()
                .collection("events")
                .document(eventId)
                .update("posterUrl", "")
                .addOnSuccessListener(unused -> {
                    removeImageLocally(eventId);
                    Toast.makeText(this, "Image removed", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to remove image", Toast.LENGTH_SHORT).show();
                    Log.e("AdminImages", "Failed to update event posterUrl", e);
                });
    }

    // Remove image from local lists (unused; kept for reference)
    private void removeImageLocally(String eventId) {
        List<Image> newAll = new ArrayList<>();
        for (Image img : all) {
            if (!eventId.equals(img.getId())) {
                newAll.add(img);
            }
        }
        all.clear();
        all.addAll(newAll);
        render();
    }

    interface OnLongPressPositionListener { void onLongPress(int position); }

    // Gesture helper for long-press on RecyclerView (unused for now)
    static class LongPressOpener extends RecyclerView.SimpleOnItemTouchListener {
        private final GestureDetector detector;
        private final RecyclerView recyclerView;
        private final OnLongPressPositionListener listener;

        LongPressOpener(RecyclerView rv, OnLongPressPositionListener l) {
            recyclerView = rv;
            listener = l;
            detector = new GestureDetector(rv.getContext(),
                    new GestureDetector.SimpleOnGestureListener() {
                        @Override
                        public void onLongPress(MotionEvent e) {
                            View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                            if (child != null) {
                                int pos = recyclerView.getChildAdapterPosition(child);
                                if (listener != null) listener.onLongPress(pos);
                            }
                        }

                        @Override
                        public boolean onDown(MotionEvent e) {
                            // Must return true to receive long-press events.
                            return true;
                        }
                    });
        }

        @Override
        public boolean onInterceptTouchEvent(@NonNull RecyclerView rv,
                                             @NonNull MotionEvent e) {
            detector.onTouchEvent(e);
            return false;
        }
    }
}
