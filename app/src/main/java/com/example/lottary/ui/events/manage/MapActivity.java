package com.example.lottary.ui.events.manage;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lottary.R;
import com.example.lottary.data.FirestoreEventRepository;
import com.example.lottary.data.FirestoreUserRepository;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;

import java.util.ArrayList;
import java.util.List;
// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/**
 * A {@link AppCompatActivity} subclass that let the organizer sees approximate locations of the people who joined the event
 * The code was adapted from Google LLC's Example on Display Maps and Custom Map Pins
 * @author Han Nguyen, Tianyi Zhang, Google LLC
 * @version 2.0
 * @see ManageEventActivity
 * @see com.example.lottary.ui.profile.CreateProfileActivity
 * @see <a href="https://developers.google.com/maps/documentation/android-sdk/map-with-marker"/>Google LLC's Example on Display Maps and Custom Map Pins</a>
 * @see <a href="https://github.com/googlemaps-samples/android-samples/blob/main/ApiDemos/project/java-app/src/main/java/com/example/mapdemo/UiSettingsDemoActivity.java"/>Google LLC's Example on Map Interaction Features</a>
 */
public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private String eventId;
    private UiSettings mUiSettings;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        eventId = getIntent().getStringExtra(ManageEventActivity.EXTRA_EVENT_ID);

        MaterialToolbar top = findViewById(R.id.top_app_bar);
        top.setNavigationOnClickListener(v -> finish());

        // Create the map and add markers when map is ready
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_view);
        if (mapFragment == null) Toast.makeText(this, "Cannot display map.", Toast.LENGTH_LONG).show();
        else {
            mapFragment.getMapAsync(this);
        }
    }

    /**
     * Generate markers to display on the map when it's ready to be manipulated
     * @param googleMap the map that was loaded
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        // Enable zoom
        mUiSettings = googleMap.getUiSettings();
        mUiSettings.setZoomControlsEnabled(true);

        // Event venue marker (not needed)
        // FirestoreEventRepository.get().listenEvent(eventId, this::getAllUserLocations);
        // LatLng venueMarker = new LatLng(allLocationsList.get(0).getLat(), allLocationsList.get(0).getLng());
        // googleMap.addMarker(new MarkerOptions()
        // .position(venueMarker)
        // .title(allLocationsList.get(0).getName()));
        // googleMap.moveCamera(CameraUpdateFactory.newLatLng(venueMarker));

        // Get all event IDs of users
        FirestoreEventRepository.get().listenEvent(eventId, documentEvent -> {
            if (documentEvent != null && documentEvent.exists()) {
                List<String> allParticipants = toList(documentEvent.get("allParticipants"));
                for (int i = 0; i < allParticipants.size(); i++) {
                    // Get user info
                    FirestoreUserRepository.get().listenUser(allParticipants.get(i), documentUser -> {
                        Object la = documentUser.get("latitude");
                        Object lo = documentUser.get("longitude");
                        String name = documentUser.get("name").toString();

                        // only make markers for users who have valid location coordinations
                        // which means user who didn't provide location will be skipped
                        if (la instanceof Number && lo instanceof Number) {
                            Double entrantLat = ((Number) la).doubleValue();
                            Double entrantLng = ((Number) lo).doubleValue();

                            LatLng userMarker = new LatLng(entrantLat, entrantLng);
                            googleMap.addMarker(new MarkerOptions()
                                    .position(userMarker)
                                    .title(name));
                        }
                    });
                }
            }
        });
    }


    /**
     * Convert a list of user IDs from the database into a Java String list
     * @param o: Data object to turn into a String list
     * @return A String list of the user IDs
     */
    private List<String> toList(Object o){ return (o instanceof List)? new ArrayList<>((List<String>)o) : new ArrayList<>(); }
}
