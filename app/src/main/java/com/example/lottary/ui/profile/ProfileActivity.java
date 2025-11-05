package com.example.lottary.ui.profile;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lottary.R;
import com.example.lottary.data.FirestoreUserRepository;
import com.example.lottary.ui.browse.BrowseActivity;
import com.example.lottary.ui.events.MyEventsActivity;
import com.example.lottary.ui.notifications.NotificationsActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    private String deviceID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        Bundle bundle = new Bundle();
        bundle.putString("deviceID", deviceID);
        Log.i("deviceID", deviceID);

        if (savedInstanceState == null) {
            DocumentReference docSnap = FirestoreUserRepository.get().hasUser(deviceID);
            docSnap.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    // if device id is not in the database, user is new
                    if (document.exists()) {
                        getSupportFragmentManager().beginTransaction()
                                .setReorderingAllowed(true)
                                .add(R.id.profile_fragment_container_view, ProfileInfoFragment.class, bundle)
                                .commit();
                    } else {
                        getSupportFragmentManager().beginTransaction()
                                .setReorderingAllowed(true)
                                .add(R.id.profile_fragment_container_view, NewProfileFragment.class, null)
                                .commit();
                    }
                } else {
                    Log.d(TAG, "Failed with: ", task.getException());
                }
            });
        }

        // bottom navigation & click handle
        BottomNavigationView nav = findViewById(R.id.bottomNav);
        nav.setSelectedItemId(R.id.nav_profile);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_browse) {
                Intent i = new Intent(this, BrowseActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_my_events) {
                Intent i = new Intent(this, MyEventsActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
                overridePendingTransition(0, 0);
            } else if (id == R.id.nav_notifications) {
                Intent i = new Intent(this, NotificationsActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
                overridePendingTransition(0, 0);
            } else if (id == R.id.nav_profile) {
                return true; // already here
            }
            return false;
        });
    }

    protected void onResume() {
        super.onResume();

        // search for matching deviceID in database
        DocumentReference docSnap = FirestoreUserRepository.get().hasUser(deviceID);
        docSnap.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();

                // if device id is in the database, shows user info
                if (document.exists()) {
                    getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .replace(R.id.profile_fragment_container_view, ProfileInfoFragment.class, null)
                            .commit();
                // if device id is not in the database, user is new
                } else {
                    getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .replace(R.id.profile_fragment_container_view, NewProfileFragment.class, null)
                            .commit();
                }
            } else {
                Log.d(TAG, "Failed with: ", task.getException());
            }
        });
    }
}