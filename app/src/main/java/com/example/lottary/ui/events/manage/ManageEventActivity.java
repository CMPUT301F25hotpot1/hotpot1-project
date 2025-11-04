package com.example.lottary.ui.events.manage;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.lottary.R;
import com.example.lottary.data.FirestoreEventRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Manage Event screen: tabs for All/Chosen/Signed-Up/Cancelled and actions.
 * Requires Intent extra "event_id".
 */
public class ManageEventActivity extends AppCompatActivity {

    public static final String EXTRA_EVENT_ID = "event_id";

    private String eventId;
    private TabLayout tabLayout;
    private ViewPager2 pager;
    private MaterialToolbar topBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_event);

        // 顶部返回
        topBar = findViewById(R.id.top_app_bar);
        topBar.setNavigationOnClickListener(v -> finish());

        eventId = getIntent().getStringExtra(EXTRA_EVENT_ID);
        if (eventId == null || eventId.isEmpty()) {
            toast("Missing event_id");
            finish();
            return;
        }

        TextView title = findViewById(R.id.txt_title);
        tabLayout = findViewById(R.id.tab_layout);
        pager = findViewById(R.id.view_pager);

        // Tabs + ViewPager
        pager.setAdapter(new FragAdapter(this, eventId));
        new TabLayoutMediator(tabLayout, pager, (tab, pos) -> {
            switch (pos) {
                case 0: tab.setText("All Entrants"); break;
                case 1: tab.setText("Chosen Entrants"); break;
                case 2: tab.setText("Signed-Up Entrants"); break;
                default: tab.setText("Cancelled Entrants");
            }
        }).attach();

        // 标题跟随事件
        FirestoreEventRepository.get().listenEvent(eventId, d -> {
            String t = d != null ? d.getString("title") : null;
            String displayTitle = (t == null || t.isEmpty()) ? "Manage Event" : t;
            title.setText(displayTitle);
            topBar.setTitle(displayTitle);
        });

        // 按钮
        Button btnDraw   = findViewById(R.id.btn_draw);
        Button btnNotify = findViewById(R.id.btn_notify);
        Button btnExport = findViewById(R.id.btn_export);
        Button btnQr     = findViewById(R.id.btn_qr);
        Button btnMap    = findViewById(R.id.btn_map);

        // 1) Draw Winners：弹出对话框（填满容量/自定义数量）
        btnDraw.setOnClickListener(v ->
                DrawWinnersDialog.newInstance(eventId)
                        .show(getSupportFragmentManager(), "draw_winners"));

        // 2) Send Notification：跳转到发送页
        btnNotify.setOnClickListener(v -> {
            Intent i = new Intent(this, SendNotificationsActivity.class);
            i.putExtra(EXTRA_EVENT_ID, eventId);
            startActivity(i);
        });

        // 3) Export CSV：一次性拉取文档→生成 CSV→分享
        btnExport.setOnClickListener(v -> exportCsvOnce());

        // 4) View QR Code：进入二维码页（展示 eventId & 标题的二维码/信息）
        btnQr.setOnClickListener(v -> {
            Intent i = new Intent(this, QrCodeActivity.class);
            i.putExtra(EXTRA_EVENT_ID, eventId);
            startActivity(i);
        });

        // 5) View Map：进入地图页（若未配置坐标，则给出提示）
        btnMap.setOnClickListener(v -> {
            Intent i = new Intent(this, MapActivity.class);
            i.putExtra(EXTRA_EVENT_ID, eventId);
            startActivity(i);
        });
    }

    private void exportCsvOnce() {
        FirebaseFirestore.getInstance()
                .collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(this::shareCsvFromDoc)
                .addOnFailureListener(e -> toast("Export failed: " + e.getMessage()));
    }

    private void shareCsvFromDoc(@NonNull DocumentSnapshot d) {
        String csv = FirestoreEventRepository.buildCsvFromEvent(d);
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/csv");
        send.putExtra(Intent.EXTRA_TEXT, csv);
        send.putExtra(Intent.EXTRA_TITLE, "entrants.csv");
        startActivity(Intent.createChooser(send, "Export CSV"));
    }

    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }

    // Adapter for tab fragments
    static class FragAdapter extends FragmentStateAdapter {
        private final String eventId;
        FragAdapter(@NonNull AppCompatActivity a, @NonNull String eventId) {
            super(a); this.eventId = eventId;
        }
        @NonNull @Override public Fragment createFragment(int pos) {
            return EntrantsListFragment.newInstance(eventId, pos); // 0 All,1 Chosen,2 Signed,3 Cancelled
        }
        @Override public int getItemCount() { return 4; }
    }
}
