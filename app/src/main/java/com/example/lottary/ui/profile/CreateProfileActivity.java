package com.example.lottary.ui.profile;

import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lottary.R;
import com.example.lottary.data.FirestoreUserRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.firebase.Timestamp;

import java.util.HashMap;
import java.util.Map;

public class CreateProfileActivity extends AppCompatActivity {

    private MaterialToolbar topBar;
    private EditText etName, etEmail, etPhoneNum;
    private Button btnCreateProfile;
    private MaterialCheckBox cbIDConsent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);

        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPhoneNum = findViewById(R.id.et_phone_number);
        cbIDConsent = findViewById(R.id.cb_device_id_consent);
        btnCreateProfile = findViewById(R.id.btn_create_profile);
        topBar = findViewById(R.id.top_app_bar);

        // listener
        topBar.setNavigationOnClickListener(v -> finish());
        btnCreateProfile.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        if (!require(etName)) return;
        else if (!require(etEmail)) return;
        else if (!(cbIDConsent.isChecked())) {
            cbIDConsent.setError("Required");
            cbIDConsent.requestFocus();
            return;
        }

        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        if (TextUtils.isEmpty(deviceId)) deviceId = "device_demo";

        Map<String, Object> fields = new HashMap<>();
        fields.put("name", etName.getText().toString().trim());
        fields.put("email", etEmail.getText().toString().trim());
        fields.put("phoneNumber", etPhoneNum.getText().toString().trim());
        fields.put("userDeviceId", deviceId);
        fields.put("createdAt", Timestamp.now());

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

    private boolean require(EditText et) {
        if (TextUtils.isEmpty(et.getText().toString().trim())) {
            et.setError("Required");
            et.requestFocus();
            return false;
        }
        return true;
    }
}