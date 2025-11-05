package com.example.lottary.ui.profile;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lottary.R;
import com.example.lottary.data.FirestoreUserRepository;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileInfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileInfoFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_DEVICEID = "device_demo";
    private String userDeviceID;
    private MaterialToolbar topBar;
    private EditText etName, etEmail, etPhoneNum;
    private Button btnEditProfile, btnDeleteProfile;

    public ProfileInfoFragment() {
        // Required empty public constructor
    }
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment ProfileInfoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileInfoFragment newInstance(String param1) {
        ProfileInfoFragment fragment = new ProfileInfoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DEVICEID, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userDeviceID = getArguments().getString("deviceID");
            Log.i("deviceID", userDeviceID);
        }
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
        etName = view.findViewById(R.id.et_name);
        etEmail = view.findViewById(R.id.et_email);
        etPhoneNum = view.findViewById(R.id.et_phone_number);
        topBar = view.findViewById(R.id.top_app_bar);

        DocumentReference docRef = FirestoreUserRepository.get().hasUser(userDeviceID);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                // if device id is in the database, user is new
                if (document.exists()) {
                    populate(document);
                } else {
                    Log.i("Empty document", "Failed with: ", task.getException());
                }
            } else {
                Log.i("Task failed", "Failed with: ", task.getException());
            }
        });


        // set button listeners
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        btnEditProfile.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), EditProfileActivity.class)));

        btnDeleteProfile = view.findViewById(R.id.btn_delete_profile);
        btnDeleteProfile.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), EditProfileActivity.class)));
    }

    private void populate(DocumentSnapshot d) {
        if (d == null || !d.exists()) return;

        etName.setText(n(d.getString("name")));
        etEmail.setText(n(d.getString("email")));
        String phoneNum = n(d.getString("phoneNum"));
        if (phoneNum.isEmpty()) {
            etPhoneNum.setText("Not provided");
        }
        else {
            etPhoneNum.setText(phoneNum);
        }
    }

    private static String n(String v){ return v == null ? "" : v; }
}