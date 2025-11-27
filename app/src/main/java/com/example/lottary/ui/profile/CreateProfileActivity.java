package com.example.lottary.ui.profile;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
 * @author Tianyi Zhang (for base code of saveProfile(), require()) & Han Nguyen
 * @version 1.0
 * @see MyProfileActivity
 * @see NewProfileFragment
 */
public class CreateProfileActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private MaterialToolbar topBar;
    private EditText etName, etEmail, etPhoneNum;
    private Button btnCreateProfile;
    private MaterialCheckBox cbIDConsent, cbIDLocation;

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
    }

    /**
     * Add the new profile to the database. Nothing will be changed if there are errors while attempting to update.
     * @see FirestoreUserRepository
     */
    private void saveProfile() {
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

        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        if (TextUtils.isEmpty(deviceId)) deviceId = "device_demo";

        Map<String, Object> fields = new HashMap<>();
        fields.put("name", etName.getText().toString().trim());
        fields.put("email", etEmail.getText().toString().trim());
        fields.put("phoneNumber", etPhoneNum.getText().toString().trim());
        fields.put("latitude", etPhoneNum.getText().toString().trim());
        fields.put("longitude", etPhoneNum.getText().toString().trim());
        fields.put("userDeviceId", deviceId);
        fields.put("createdAt", Timestamp.now());

        // https://www.geeksforgeeks.org/android/how-to-get-user-location-in-android/
        // https://developers.google.com/maps/documentation/android-sdk/examples/my-location?_gl=1*1d0qewx*_up*MQ..*_ga*MTYxNjI3NTgzMi4xNzY0MjQxOTA4*_ga_SM8HXJ53K2*czE3NjQyNDE5MDgkbzEkZzAkdDE3NjQyNDE5MDgkajYwJGwwJGgw*_ga_NRWSTWS78N*czE3NjQyNDE5MDgkbzEkZzEkdDE3NjQyNDE5MjckajQxJGwwJGgw#maps_android_sample_my_location-java

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    fields.put("latitude", location.getLatitude());
                    fields.put("longitude", location.getLongitude());
                }
                else {
                    fields.put("latitude", null);
                    fields.put("longitude", null);
            }
        });

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
