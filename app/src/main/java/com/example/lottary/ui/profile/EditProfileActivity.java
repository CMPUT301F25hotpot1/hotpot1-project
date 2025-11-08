package com.example.lottary.ui.profile;

import static android.content.ContentValues.TAG;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lottary.R;
import com.example.lottary.data.FirestoreUserRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

/**
 * A {@link AppCompatActivity} subclass that allow user to edit their profile information.
 * @author Tianyi Zhang (for base code of populate(), n(), saveChanges(), require()) & Han Nguyen
 * @version 1.0
 * @see ProfileActivity
 * @see ProfileInfoFragment
 */
public class EditProfileActivity extends AppCompatActivity {


    private String userDeviceID;

    private MaterialToolbar topBar;
    private EditText etName, etEmail, etPhoneNumber;
    private Button btnEditProfile;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        userDeviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPhoneNumber = findViewById(R.id.et_phone_number);
        topBar = findViewById(R.id.top_app_bar);
        btnEditProfile = findViewById((R.id.btn_edit_profile));

        FirestoreUserRepository.get().listenUser(userDeviceID, this::populate);

        // listener
        topBar.setNavigationOnClickListener(v -> finish());


        btnEditProfile.setOnClickListener(v -> saveChanges());
        }

    /**
     * Put information from an user document on display on the appropriate fields. Will automatically
     * return if document is empty
     * @param d - a DocumentSnapshot containing user info to populate
     * @see FirestoreUserRepository
     */
    private void populate(DocumentSnapshot d) {
        if (d == null || !d.exists()) return;

        etName.setText(n(d.getString("name")));
        etEmail.setText(n(d.getString("email")));
        etPhoneNumber.setText(n(d.getString("phoneNumber")));
    }

    /**
     * Update edits to the database. Nothing will be changed if there are errors while updating.
     * @see FirestoreUserRepository
     */
    private void saveChanges() {
        if (!require(etName)) return;
        else if (!require(etEmail)) return;

        Map<String, Object> update = new HashMap<>();
        update.put("name", etName.getText().toString().trim());
        update.put("email", etEmail.getText().toString().trim());
        update.put("phoneNumber", etPhoneNumber.getText().toString().trim());
        update.put("updatedAt", Timestamp.now());

        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Saving...");
        pd.setCancelable(false);
        pd.show();

        btnEditProfile.setEnabled(false);

        final android.os.Handler handler = new android.os.Handler(getMainLooper());
        final Runnable timeout = () -> {
            if (pd.isShowing()) {
                pd.dismiss();
                btnEditProfile.setEnabled(true);
                Toast.makeText(this,
                        "Saving is taking too long.\nCheck network or Firestore rules.",
                        Toast.LENGTH_LONG).show();
            }
        };
        handler.postDelayed(timeout, 10_000);

        FirestoreUserRepository.get().updateUser(userDeviceID, update)
                .addOnSuccessListener(x -> {
                    handler.removeCallbacks(timeout);
                    if (pd.isShowing()) pd.dismiss();
                    Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    handler.removeCallbacks(timeout);
                    if (pd.isShowing()) pd.dismiss();
                    btnEditProfile.setEnabled(true);
                    android.util.Log.e("EditUser", "update failed", e);
                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    /**
     * Null string handler.
     * @param v - a String to transform
     * @return an empty String if parameter is null, if not return the original String.
     */
    private static String n(String v){ return v == null ? "" : v; }

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
