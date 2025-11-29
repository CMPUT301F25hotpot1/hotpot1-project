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

/**
 * A {@link AppCompatActivity} subclass that let the organizer sees approximate locations of the people who joined the event
 * @author Han Nguyen, Tianyi Zhang, Google Developers
 * @version 2.0
 * @see ManageEventActivity
 * @see com.example.lottary.ui.profile.CreateProfileActivity
 * @see <a href="https://developers.google.com/maps/documentation/android-sdk/examples/my-location?_gl=1*1d0qewx*_up*MQ..*_ga*MTYxNjI3NTgzMi4xNzY0MjQxOTA4*_ga_SM8HXJ53K2*czE3NjQyNDE5MDgkbzEkZzAkdDE3NjQyNDE5MDgkajYwJGwwJGgw*_ga_NRWSTWS78N*czE3NjQyNDE5MDgkbzEkZzEkdDE3NjQyNDE5MjckajQxJGwwJGgw#maps_android_sample_my_location-java"/>
 * Google Developers's Example on Display Maps and Custom Map Pins</a>
 */
public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private String eventId;
    private List<LocationRow> allLocationsList = new ArrayList<>();

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        eventId = getIntent().getStringExtra(ManageEventActivity.EXTRA_EVENT_ID);

        MaterialToolbar top = findViewById(R.id.top_app_bar);
        top.setNavigationOnClickListener(v -> finish());

        // Get participants info to map markers
        FirestoreEventRepository.get().listenEvent(eventId, this::getAllUserLocations);
        // FirestoreEventRepository.get().listenEvent(eventId, this::openMapIfPossible);

        // Create the map and add markers when map is ready
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_view);
        if (mapFragment == null) Toast.makeText(this, "Cannot display map.", Toast.LENGTH_LONG).show();
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
                LocationRow userCoordi = new LocationRow(venue, venueLat, venueLng);
                allLocationsList.add(userCoordi);
            }
        } catch (Exception ignore) {}

        List<String> allParticipants = toList(document.get("allParticipants"));

        // get name and coordination for each user who participates
        for (int i = 1; i < allParticipants.size(); i++) {
            DocumentSnapshot documentUser = FirestoreUserRepository.get().getUser(allParticipants.get(i));
            try {
                Object la = documentUser.get("latitude");
                Object lo = documentUser.get("longitude");
                String name = documentUser.get("name").toString();

                // only make markers for users who have valid location coordinations
                // which means user who didn't provide location will be skipped
                if (la instanceof Number && lo instanceof Number) {
                    Double entrantLat = ((Number) la).doubleValue();
                    Double entrantLng = ((Number) lo).doubleValue();
                    LocationRow userCoordi = new LocationRow(name, entrantLat, entrantLng);
                    allLocationsList.add(userCoordi);
                }
            } catch (Exception ignore) {}
        }

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        // Event venue marker (not needed)
        // FirestoreEventRepository.get().listenEvent(eventId, this::getAllUserLocations);
        // LatLng venueMarker = new LatLng(allLocationsList.get(0).getLat(), allLocationsList.get(0).getLng());
        // googleMap.addMarker(new MarkerOptions()
                // .position(venueMarker)
                // .title(allLocationsList.get(0).getName()));
        // googleMap.moveCamera(CameraUpdateFactory.newLatLng(venueMarker));

        // create custom markers for users who have valid information
        for (int i = 0; i < allLocationsList.size(); i++) {
            LatLng userMarker = new LatLng(allLocationsList.get(i).getLat(), allLocationsList.get(i).getLng());
            googleMap.addMarker(new MarkerOptions()
                    .position(userMarker)
                    .title(allLocationsList.get(i).getName()));
        }
    }

    /**
     * Convert a list of user IDs from the database into a Java String list
     * @param o: Data object to turn into a String list
     * @return A String list of the user IDs
     */
    private List<String> toList(Object o){ return (o instanceof List)? new ArrayList<>((List<String>)o) : new ArrayList<>(); }

    /**
     * A local class that hold user information needed to map custom markers
     */
    static class LocationRow { final String name; final Double lat; final Double lng;

        public LocationRow(String id, Double lat, Double lng) {
            this.name = id;
            this.lat = lat;
            this.lng = lng;
        }

        public String getName() {
            return name;
        }

        public Double getLat() {
            return lat;
        }

        public Double getLng() {
            return lng;
        }
    }
}
