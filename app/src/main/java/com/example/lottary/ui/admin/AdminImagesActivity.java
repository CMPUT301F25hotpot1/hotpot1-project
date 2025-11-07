package com.example.lottary.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottary.R;
import com.example.lottary.data.FirestoreImageRepository;
import com.example.lottary.data.Image;
import com.example.lottary.ui.admin.adapters.ImageGridAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

    private FirestoreImageRepository repo;
    private FirestoreImageRepository.ListenerHandle handle;

    // 接收详情页删除成功的结果
    private final ActivityResultLauncher<Intent> detailLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == ImageDetailActivity.RESULT_DELETED) {
                    // 立即重渲染（也会很快被 Firestore 实时监听刷新覆盖）
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

        // ==== Bottom Nav ====
        BottomNavigationView nav = findViewById(R.id.bottomNavAdmin);
        nav.setSelectedItemId(R.id.nav_admin_images);
        nav.setOnItemReselectedListener(item -> { /* no-op */ });
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_admin_images) return true;
            if (id == R.id.nav_admin_events) {
                startActivity(new Intent(this, AdminEventsActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                overridePendingTransition(0, 0);
                return true;
            }
            if (id == R.id.nav_admin_users) {
                startActivity(new Intent(this, AdminUsersActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                overridePendingTransition(0, 0);
                return true;
            }
            if (id == R.id.nav_admin_dashboard) {
                startActivity(new Intent(this, AdminDashboardActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                overridePendingTransition(0, 0);
                return true;
            }
            return true;
        });

        // ==== 列表 ====
        rv.setLayoutManager(new GridLayoutManager(this, 3));
        rv.addItemDecoration(new SpacesItemDecoration(
                getResources().getDimensionPixelSize(R.dimen.grid_gap)));

        // 单击：仍旧打开你原来的 ImageViewerActivity（不改动）
        adapter = new ImageGridAdapter(img -> {
            if (img.getUrl() != null && !img.getUrl().isEmpty()) {
                ImageViewerActivity.launch(this, img.getUrl(), img.getTitle());
            }
        });
        rv.setAdapter(adapter);

        // 长按：进入可删除的详情页
        rv.addOnItemTouchListener(new LongPressOpener(rv, position -> {
            if (position >= 0 && position < current.size()) {
                launchDetail(current.get(position));
            }
        }));

        // 先显示“骨架格子”
        showSkeleton(18);

        btnSearch.setOnClickListener(v -> {
            query = etSearch.getText() == null ? "" : etSearch.getText().toString().trim();
            render();
        });
        btnSort.setOnClickListener(v -> {
            sortByLatest(current);
            adapter.submitList(new ArrayList<>(current));
        });

        repo = new FirestoreImageRepository();
    }

    @Override
    protected void onStart() {
        super.onStart();
        progress.setVisibility(View.VISIBLE);
        handle = repo.listenLatest((images, error) -> {
            progress.setVisibility(View.GONE);
            all.clear();
            if (error == null && images != null && !images.isEmpty()) {
                all.addAll(images);
                query = etSearch.getText() == null ? "" : etSearch.getText().toString().trim();
                render();
            } else {
                showSkeleton(18);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (handle != null) handle.remove();
    }

    private void render() {
        current.clear();
        if (TextUtils.isEmpty(query)) {
            current.addAll(all);
        } else {
            String q = query.toLowerCase();
            for (Image img : all) {
                String t = img.getTitle() == null ? "" : img.getTitle();
                if (t.toLowerCase().contains(q)) current.add(img);
            }
        }
        sortByLatest(current);
        adapter.submitList(new ArrayList<>(current));
    }

    private static void sortByLatest(List<Image> list) {
        Collections.sort(list, new Comparator<Image>() {
            @Override public int compare(Image a, Image b) {
                long ta = a.getCreatedAt()==null?0:a.getCreatedAt().getSeconds();
                long tb = b.getCreatedAt()==null?0:b.getCreatedAt().getSeconds();
                return Long.compare(tb, ta); // DESC：最新在前
            }
        });
    }

    private void showSkeleton(int count) {
        List<Image> skeleton = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Image img = new Image();
            img.setId("skeleton_" + i);
            img.setTitle("");
            img.setUrl(""); // 触发占位图
            img.setCreatedAt(Timestamp.now());
            skeleton.add(img);
        }
        adapter.submitList(skeleton);
    }

    // ---- 辅助：网格间距 ----
    static class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private final int space;
        SpacesItemDecoration(int s) { space = s; }
        @Override
        public void getItemOffsets(@NonNull android.graphics.Rect outRect, @NonNull View view,
                                   @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            outRect.left = space; outRect.right = space;
            outRect.top = space;  outRect.bottom = space;
        }
    }

    // ====== 长按打开详情 ======
    private void launchDetail(Image img) {
        Intent i = new Intent(this, ImageDetailActivity.class);
        i.putExtra(ImageDetailActivity.EXTRA_IMAGE_ID, img.getId());
        i.putExtra(ImageDetailActivity.EXTRA_IMAGE_URL, img.getUrl());
        i.putExtra(ImageDetailActivity.EXTRA_IMAGE_TITLE, img.getTitle());
        detailLauncher.launch(i);
    }

    interface OnLongPressPositionListener { void onLongPress(int position); }

    static class LongPressOpener extends RecyclerView.SimpleOnItemTouchListener {
        private final GestureDetector detector;
        private final RecyclerView recyclerView;
        private final OnLongPressPositionListener listener;

        LongPressOpener(RecyclerView rv, OnLongPressPositionListener l) {
            recyclerView = rv;
            listener = l;
            detector = new GestureDetector(rv.getContext(), new GestureDetector.SimpleOnGestureListener() {
                @Override public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null) {
                        int pos = recyclerView.getChildAdapterPosition(child);
                        if (listener != null) listener.onLongPress(pos);
                    }
                }
                @Override public boolean onDown(MotionEvent e) { return true; }
            });
        }
        @Override
        public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
            detector.onTouchEvent(e);
            return false;
        }
    }
}
