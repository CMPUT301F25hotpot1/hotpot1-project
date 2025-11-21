package com.example.lottary.ui.profile;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.lottary.R;
import com.example.lottary.data.FirestoreUserRepository;
import com.example.lottary.ui.admin.AdminEventsActivity;  // <-- important
import com.example.lottary.ui.browse.BrowseActivity;
import com.example.lottary.ui.events.MyEventsActivity;
import com.example.lottary.ui.notifications.NotificationsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

/**
 * A {@link AppCompatActivity} subclass that manage how the Profile tab is displayed depending on the
 * user status
 * @author Han Nguyen & Tianyi Zhang (for navigation bar)
 * @version 1.0
 * @see NewProfileFragment
 * @see ProfileInfoFragment
 */
public class ProfileActivity extends AppCompatActivity {

    private String deviceID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        // get current device ID
        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.i("deviceID1", deviceID);

        // check if current user exists in the database
        if (savedInstanceState == null) {
            DocumentReference docSnap = FirestoreUserRepository.get().hasUser(deviceID);
            docSnap.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    // if user exists, attach user info
                    if (document.exists()) {
                        getSupportFragmentManager().beginTransaction()
                                .setReorderingAllowed(true)
                                .add(R.id.profile_fragment_container_view, ProfileInfoFragment.class, null)
                                .commit();

                    // if user is new, attach prompt to create profile
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
            if (id == R.id.nav_browse) {  // browse event tab
                Intent i = new Intent(this, BrowseActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_my_events) {  // my events tab
                Intent i = new Intent(this, MyEventsActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_notifications) {  // notifications tab
                Intent i = new Intent(this, NotificationsActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_profile) {  // already here
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // check if current user exists in the database
        DocumentReference docSnap = FirestoreUserRepository.get().hasUser(deviceID);
        docSnap.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();

                // if user exists, attach user info
                if (document.exists()) {
                    getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .replace(R.id.profile_fragment_container_view, ProfileInfoFragment.class, null)
                            .commit();

                // if user is new, attach prompt to create profile
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