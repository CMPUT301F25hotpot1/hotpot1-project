package com.example.lottary.ui.profile;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.lottary.R;
import com.example.lottary.data.FirestoreUserRepository;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.firebase.Timestamp;

import java.util.HashMap;
import java.util.Map;

/**
 * A {@link AppCompatActivity} subclass that allow user to create their profile information.
 * @author Han Nguyen, Tianyi Zhang (for base code of saveProfile(), require()), Google Developers & GeekForGeeks
 * @version 2.0
 * @see MyProfileActivity
 * @see NewProfileFragment
 * @see <a href="https://www.geeksforgeeks.org/android/how-to-get-user-location-in-android"/>GeekForGeeks's Tutorial on Getting User Location</a>
 * @see <a href="https://developers.google.com/maps/documentation/android-sdk/examples/my-location?_gl=1*1d0qewx*_up*MQ..*_ga*MTYxNjI3NTgzMi4xNzY0MjQxOTA4*_ga_SM8HXJ53K2*czE3NjQyNDE5MDgkbzEkZzAkdDE3NjQyNDE5MDgkajYwJGwwJGgw*_ga_NRWSTWS78N*czE3NjQyNDE5MDgkbzEkZzEkdDE3NjQyNDE5MjckajQxJGwwJGgw#maps_android_sample_my_location-java"/>Google Developers's Example on Getting Location Permission</a>
 */
public class CreateProfileActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback {

    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private MaterialToolbar topBar;
    private EditText etName, etEmail, etPhoneNum;
    private Button btnCreateProfile;
    private MaterialCheckBox cbIDConsent, cbIDLocation;
    private Double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPhoneNum = findViewById(R.id.et_phone_number);
        cbIDConsent = findViewById(R.id.cb_device_id_consent);
        cbIDLocation = findViewById(R.id.cb_location_consent);
        btnCreateProfile = findViewById(R.id.btn_create_profile);
        topBar = findViewById(R.id.top_app_bar);

        // listener
        topBar.setNavigationOnClickListener(v -> finish());
        btnCreateProfile.setOnClickListener(v -> saveProfile());
        cbIDLocation.setOnCheckedChangeListener((b, isChecked) -> {
            // ask for user permission to get location when location checkbox is checked
            if (cbIDLocation.isChecked()) {
                String[] permissionList = {Manifest.permission.ACCESS_COARSE_LOCATION};
                ActivityCompat.requestPermissions(this, permissionList, LOCATION_PERMISSION_REQUEST_CODE);
            }
        });
    }

    /**
     * Add the new profile to the database. Nothing will be changed if there are errors while attempting to update.
     * @see FirestoreUserRepository
     */
    private void saveProfile() {
        // Make sure required fields are filled
        if (!require(etName)) return;
        else if (!require(etEmail)) return;
        else if (!(cbIDConsent.isChecked())) {
            cbIDConsent.setError("Required");
            cbIDConsent.requestFocus();
            return;
        }
        else if (!(cbIDLocation.isChecked())) {
            cbIDLocation.setError("Required");
            cbIDLocation.requestFocus();
            return;
        }

        // get user device ID
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        if (TextUtils.isEmpty(deviceId)) deviceId = "device_demo";

        // map data in preparation for the server
        Map<String, Object> fields = new HashMap<>();
        fields.put("name", etName.getText().toString().trim());
        fields.put("email", etEmail.getText().toString().trim());
        fields.put("phoneNumber", etPhoneNum.getText().toString().trim());
        fields.put("userDeviceId", deviceId);
        fields.put("createdAt", Timestamp.now());
        fields.put("latitude", latitude);
        fields.put("longitude", longitude);

        // send data to server
        btnCreateProfile.setEnabled(false);
        FirestoreUserRepository.get()
                .createUser(deviceId, fields)
                .addOnSuccessListener(ref -> {
                    Toast.makeText(this, "Profile created", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Create failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    btnCreateProfile.setEnabled(true);
                });
    }

    /**
     * Handle the result of requesting a new user to provide their location
     * @param requestCode The request code passed when the app ask the user for location
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either {@link PackageManager#PERMISSION_GRANTED}
     *     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     */
    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // If allowed, get the last known location. If not, it will be set to 0 by default;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                // Record coordinators of user's last known location.
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    Toast.makeText(this, "Location temporarily recorded.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Forces the user to provide input on the given field
     * @param et - the EditText field that requires a String input
     */
    private boolean require(EditText et) {
        if (TextUtils.isEmpty(et.getText().toString().trim())) {
            et.setError("Required");
            et.requestFocus();
            return false;
        }
        return true;
    }
}
