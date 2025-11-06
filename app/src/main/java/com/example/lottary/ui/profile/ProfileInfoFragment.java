package com.example.lottary.ui.profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.lottary.R;
import com.example.lottary.data.FirestoreUserRepository;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

/**
 * A simple {@link Fragment} subclass.
 * .
 */
public class ProfileInfoFragment extends Fragment {

    private String userDeviceID = "device_demo";
    private TextView infoName, infoEmail, infoPhoneNum;
    private Button btnEditProfile, btnDeleteProfile;

    public ProfileInfoFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context context = requireContext();
        userDeviceID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.i("deviceID2", userDeviceID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // display user info
        infoName = view.findViewById(R.id.user_name);
        infoEmail = view.findViewById(R.id.user_email);
        infoPhoneNum = view.findViewById(R.id.user_phone_num);

        fetchInfo(userDeviceID);
        // FirestoreUserRepository.get().listenUser(userDeviceID, this::populate);

        // set button listeners


        btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        btnEditProfile.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), EditProfileActivity.class)));

        btnDeleteProfile = view.findViewById(R.id.btn_delete_profile);
        btnDeleteProfile.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), EditProfileActivity.class)));
    }

    @Override
    public void onResume() {
        super.onResume();
        // FirestoreUserRepository.get().listenUser(userDeviceID, this::populate);
        fetchInfo(userDeviceID);
    }

    private void populate(DocumentSnapshot d) {
        if (d == null || !d.exists()) return;

        infoName.setText(d.getString("name"));
        infoEmail.setText(n(d.getString("email")));
        String phoneNum = n(d.getString("phoneNumber"));
        if (phoneNum.isEmpty()) {
            infoPhoneNum.setText("Not provided");
        }
        else {
            infoPhoneNum.setText(phoneNum);
        }
    }

    private void fetchInfo(String deviceID) {
        DocumentReference docRef = FirestoreUserRepository.get().hasUser(deviceID);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                // if device id is in the database, user is new
                if (document.exists()) {
                    populate(document);
                } else {
                    Log.i("EmptyDocument", "Failed with: ", task.getException());
                }
            } else {
                Log.i("TaskFailed", "Failed with: ", task.getException());
            }
        });
    }

    private static String n(String v){ return v == null ? "" : v; }
}