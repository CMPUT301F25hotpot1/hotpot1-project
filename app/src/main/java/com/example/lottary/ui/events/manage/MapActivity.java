package com.example.lottary.ui.events.manage;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lottary.R;
import com.example.lottary.data.FirestoreEventRepository;
import com.example.lottary.data.FirestoreUserRepository;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private String eventId;
    private List<List<Double>> allLocationsList = new ArrayList<>();

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        eventId = getIntent().getStringExtra(ManageEventActivity.EXTRA_EVENT_ID);

        MaterialToolbar top = findViewById(R.id.top_app_bar);
        top.setNavigationOnClickListener(v -> finish());

        // FirestoreEventRepository.get().listenEvent(eventId, this::openMapIfPossible);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_view);
        if (mapFragment == null) Toast.makeText(this, "No location for this event", Toast.LENGTH_LONG).show();
        else mapFragment.getMapAsync(this);
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
            startActivity(new Intent(Intent.ACTION_VIEW, gmmIntentUri));
        }
    }

    private void getAllUserLocations (DocumentSnapshot document) {
        if (document == null || !document.exists()) return;

        try {
            String venue = document.getString("venue");
            Object la = document.get("latitude");
            Object lo = document.get("longitude");
            if (la instanceof Number && lo instanceof Number) {
                Double venueLat = ((Number) la).doubleValue();
                Double venueLng = ((Number) lo).doubleValue();
                List<Double>userCoordi = List.of(venueLat, venueLng);
                allLocationsList.add(userCoordi);
            }
        } catch (Exception ignore) {}

        List<String> allParticipants = toList(document.get("allParticipants"));

        // get coordination for each user who participates
        for (int i = 1; i < allParticipants.size(); i++) {
            DocumentSnapshot documentUser = FirestoreUserRepository.get().getUser(allParticipants.get(i));
            try {
                Object la = documentUser.get("latitude");
                Object lo = documentUser.get("longitude");
                String name = documentUser.get("name").toString();
                if (la instanceof Number && lo instanceof Number) {
                    Double entrantLat = ((Number) la).doubleValue();
                    Double entrantLng = ((Number) lo).doubleValue();
                    List<Double>userCoordi = List.of(entrantLat, entrantLng);
                    allLocationsList.add(userCoordi);
                }
            } catch (Exception ignore) {}
        }

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        FirestoreEventRepository.get().listenEvent(eventId, this::getAllUserLocations);
        LatLng venueMarker = new LatLng(allLocationsList.get(0).get(0), allLocationsList.get(0).get(1));
        googleMap.addMarker(new MarkerOptions()
                .position(venueMarker)
                .title("Event venue"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(venueMarker));

        for (int i = 1; i < allLocationsList.size(); i++) {
            LatLng userMarker = new LatLng(allLocationsList.get(i).get(0), allLocationsList.get(i).get(1));
            googleMap.addMarker(new MarkerOptions()
                    .position(userMarker)
                    .title("Entrants"));
        }
    }

    /**
     * Convert a list of user IDs from the database into a Java String list
     * @param o: Data object to turn into a String list
     * @return A String list of the user IDs
     */
    private List<String> toList(Object o){ return (o instanceof List)? new ArrayList<>((List<String>)o) : new ArrayList<>(); }
    static class Row { final String id; final Double lat; final Double lng;

        public Row(String id, Double lat, Double lng) {
            this.id = id;
            this.lat = lat;
            this.lng = lng;
        }
    }
}
