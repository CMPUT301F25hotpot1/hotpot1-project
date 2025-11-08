package com.example.lottary.ui.events.manage;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
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
import com.example.lottary.ui.events.edit.EditEventActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.DateFormat;

public class ManageEventActivity extends AppCompatActivity {

    public static final String EXTRA_EVENT_ID = "event_id";

    private String eventId;
    private ListenerRegistration reg;

    private MaterialToolbar topBar;
    private TextView txtTitle;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private Button btnDraw, btnNotify, btnExport, btnQr, btnMap;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_event);

        eventId = getIntent().getStringExtra(EditEventActivity.EXTRA_EVENT_ID);
        if (TextUtils.isEmpty(eventId)) {
            eventId = getIntent().getStringExtra(EXTRA_EVENT_ID);
        }
        if (TextUtils.isEmpty(eventId)) {
            toast("Missing event_id");
            finish();
            return;
        }

        topBar    = findViewById(R.id.top_app_bar);
        txtTitle  = findViewById(R.id.txt_title);
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);
        btnDraw   = findViewById(R.id.btn_draw);
        btnNotify = findViewById(R.id.btn_notify);
        btnExport = findViewById(R.id.btn_export);
        btnQr     = findViewById(R.id.btn_qr);
        btnMap    = findViewById(R.id.btn_map);

        if (topBar != null) topBar.setNavigationOnClickListener(v -> finish());

        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull @Override public Fragment createFragment(int position) {
                return EntrantsListFragment.newInstance(eventId, position);
            }
            @Override public int getItemCount() { return 4; }
        });

        new TabLayoutMediator(tabLayout, viewPager, (tab, pos) -> {
            switch (pos) {
                case 0: tab.setText("All Entrants"); break;
                case 1: tab.setText("Chosen Entrants"); break;
                case 2: tab.setText("Signed-Up Entrants"); break;
                default: tab.setText("Cancelled Entrants");
            }
        }).attach();

        reg = FirestoreEventRepository.get().listenEvent(eventId, this::bindEventHeader);

        btnDraw.setOnClickListener(v -> {
            btnDraw.setEnabled(false);
            FirestoreEventRepository.get()
                    .drawWinnersAndNotify(eventId,
                            "Congratulations! You are chosen. Please sign up to secure your spot.")
                    .addOnSuccessListener(x -> {
                        toast("Winners drawn & notifications sent.");
                        btnDraw.setEnabled(true);
                    })
                    .addOnFailureListener(e -> {
                        toast("Failed: " + e.getMessage());
                        btnDraw.setEnabled(true);
                    });
        });

        btnNotify.setOnClickListener(v ->
                startIfExists("com.example.lottary.ui.events.manage.SendNotificationsActivity"));

        btnExport.setOnClickListener(v ->
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("events").document(eventId).get()
                        .addOnSuccessListener(this::shareCsvFromDoc)
                        .addOnFailureListener(e -> toast("Export failed: " + e.getMessage())));

        btnQr.setOnClickListener(v ->
                startIfExists("com.example.lottary.ui.events.manage.QrCodeActivity"));

        btnMap.setOnClickListener(v ->
                startIfExists("com.example.lottary.ui.events.manage.MapActivity"));
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        if (reg != null) { reg.remove(); reg = null; }
    }

    private void bindEventHeader(@Nullable DocumentSnapshot d) {
        if (d == null || !d.exists()) return;

        String title = val(d.get("title"));
        if (txtTitle != null) txtTitle.setText(TextUtils.isEmpty(title) ? "Manage Event" : title);
        if (topBar != null)  topBar.setTitle(TextUtils.isEmpty(title) ? "Manage Event" : title);

        Timestamp ts = d.getTimestamp("startTime");
        if (ts != null && txtTitle != null) {
            String pretty = DateFormat.getDateTimeInstance(
                    DateFormat.MEDIUM, DateFormat.SHORT).format(ts.toDate());
            txtTitle.setText((TextUtils.isEmpty(title) ? "Manage Event" : title) + "  ·  " + pretty);
        }
    }

    private void shareCsvFromDoc(@NonNull DocumentSnapshot d) {
        String csv = FirestoreEventRepository.buildCsvFromEvent(d);
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/csv");
        send.putExtra(Intent.EXTRA_TEXT, csv);
        send.putExtra(Intent.EXTRA_TITLE, "entrants.csv");
        startActivity(Intent.createChooser(send, "Export CSV"));
    }

    private void startIfExists(@NonNull String fqcn) {
        try {
            Class<?> cls = Class.forName(fqcn);
            Intent i = new Intent(this, cls);
            i.putExtra(EXTRA_EVENT_ID, eventId);
            startActivity(i);
        } catch (ClassNotFoundException e) {
            toast("Screen not implemented yet.");
        }
    }

    private static String val(Object o){ return o == null ? "" : o.toString(); }
    private void toast(String s){
        Toast t = Toast.makeText(this, s, Toast.LENGTH_SHORT);
        t.setGravity(Gravity.CENTER, 0, 0);
        t.show();
    }
}
