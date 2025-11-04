package com.example.lottary.ui.events.manage;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lottary.R;
import com.example.lottary.data.FirestoreEventRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentSnapshot;

public class MapActivity extends AppCompatActivity {

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        String eventId = getIntent().getStringExtra(ManageEventActivity.EXTRA_EVENT_ID);

        MaterialToolbar top = findViewById(R.id.top_app_bar);
        top.setNavigationOnClickListener(v -> finish());
        top.setTitle("Map");

        FirestoreEventRepository.get().listenEvent(eventId, this::openMapIfPossible);
    }

    private void openMapIfPossible(DocumentSnapshot d) {
        if (d == null || !d.exists()) return;

        Double lat = null, lng = null;
        try {
            Object la = d.get("latitude");
            Object lo = d.get("longitude");
            if (la instanceof Number && lo instanceof Number) {
                lat = ((Number) la).doubleValue();
                lng = ((Number) lo).doubleValue();
            }
        } catch (Exception ignore) {}

        String venue = d.getString("venue");
        String query;
        if (lat != null && lng != null) {
            query = lat + "," + lng;
        } else if (venue != null && !venue.isEmpty()) {
            query = Uri.encode(venue);
        } else {
            Toast.makeText(this, "No location for this event", Toast.LENGTH_LONG).show();
            return;
        }

        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + query);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        try {
            startActivity(mapIntent);
        } catch (ActivityNotFoundException e) {
            // 没有谷歌地图，尝试任意地图
            startActivity(new Intent(Intent.ACTION_VIEW, gmmIntentUri));
        }
    }
}
