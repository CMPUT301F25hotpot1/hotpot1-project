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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_event);

        eventId = getIntent().getStringExtra(EXTRA_EVENT_ID);
        if (eventId == null || eventId.isEmpty()) {
            toast("Missing event_id");
            finish();
            return;
        }

        TextView title = findViewById(R.id.txt_title);
        tabLayout = findViewById(R.id.tab_layout);
        pager = findViewById(R.id.view_pager);

        // ViewPager2 + Tabs
        pager.setAdapter(new FragAdapter(this, eventId));
        new TabLayoutMediator(tabLayout, pager, (tab, pos) -> {
            switch (pos) {
                case 0: tab.setText("All Entrants"); break;
                case 1: tab.setText("Chosen Entrants"); break;
                case 2: tab.setText("Signed-Up Entrants"); break;
                default: tab.setText("Cancelled Entrants");
            }
        }).attach();

        // Title: listen to event title updates (lightweight)
        FirestoreEventRepository.get().listenEvent(eventId, d -> {
            String t = d != null ? d.getString("title") : null;
            title.setText(t == null ? "Manage Event" : t);
        });

        // Actions
        Button btnDraw   = findViewById(R.id.btn_draw);
        Button btnNotify = findViewById(R.id.btn_notify);
        Button btnExport = findViewById(R.id.btn_export);
        Button btnQr     = findViewById(R.id.btn_qr);
        Button btnMap    = findViewById(R.id.btn_map);

        btnDraw.setOnClickListener(v ->
                FirestoreEventRepository.get().drawWinners(eventId, 0)
                        .addOnSuccessListener(x -> toast("Winners drawn"))
                        .addOnFailureListener(e -> toast("Failed: " + e.getMessage()))
        );

        btnNotify.setOnClickListener(v -> toast("Send notifications (stub)"));
        btnExport.setOnClickListener(v -> exportCsvOnce());
        btnQr.setOnClickListener(v -> toast("Show QR (stub)"));
        btnMap.setOnClickListener(v -> toast("Open Map (stub)"));
    }

    /**
     * 导出 CSV：一次性读取该事件文档，然后用 FirestoreEventRepository 的工具方法生成 CSV。
     * 避免在这里注册持续监听，防止多次回调。
     */
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
        shareText(csv, "entrants.csv");
    }

    private void shareText(@NonNull String text, @NonNull String filename) {
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/csv");
        send.putExtra(Intent.EXTRA_TEXT, text);
        send.putExtra(Intent.EXTRA_TITLE, filename);
        startActivity(Intent.createChooser(send, "Export CSV"));
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    static class FragAdapter extends FragmentStateAdapter {
        private final String eventId;

        FragAdapter(@NonNull AppCompatActivity a, @NonNull String eventId) {
            super(a);
            this.eventId = eventId;
        }

        @NonNull
        @Override
        public Fragment createFragment(int pos) {
            // 0=All, 1=Chosen, 2=SignedUp, 3=Cancelled
            return EntrantsListFragment.newInstance(eventId, pos);
        }

        @Override
        public int getItemCount() {
            return 4;
        }
    }
}
