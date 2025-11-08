package com.example.lottary.ui.profile;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

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

        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.i("deviceID1", deviceID);

        if (savedInstanceState == null) {
            DocumentReference docSnap = FirestoreUserRepository.get().hasUser(deviceID);
            docSnap.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        getSupportFragmentManager().beginTransaction()
                                .setReorderingAllowed(true)
                                .add(R.id.profile_fragment_container_view, ProfileInfoFragment.class, null)
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

        // bottom nav (unchanged)
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
                return true;
            } else if (id == R.id.nav_notifications) {
                Intent i = new Intent(this, NotificationsActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_profile) {
                return true;
            }
            return false;
        });

        // ✅ Swap to Admin View — go straight to AdminEventsActivity
        Button adminBtn = findViewById(R.id.btn_swap_admin); // keep your existing id
        if (adminBtn != null) {
            adminBtn.setOnClickListener(v -> {
                Intent intent = new Intent(this, AdminEventsActivity.class);
                intent.putExtra("open_from_profile", true);
                startActivity(intent);
                // finish current to avoid back-stack/lifecycle weirdness
                finish();
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        DocumentReference docSnap = FirestoreUserRepository.get().hasUser(deviceID);
        docSnap.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .replace(R.id.profile_fragment_container_view, ProfileInfoFragment.class, null)
                            .commit();
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
