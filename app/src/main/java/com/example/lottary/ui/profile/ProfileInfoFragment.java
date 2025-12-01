package com.example.lottary.ui.profile;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.Toast;

import com.example.lottary.R;
import com.example.lottary.data.FirestoreUserRepository;
import com.example.lottary.ui.admin.AdminEventsActivity;
import com.example.lottary.ui.admin.AdminUsersActivity;
import com.example.lottary.ui.browse.BrowseActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link Fragment} subclass that displays user information & options to edit profile, delete profile
 * and swap to Admin View (conditional)
 * @author Han Nguyen, Mengxi Zhang (for admin button config) & Tianyi Zhang (for populate() and n())
 * @version 1.2
 * @see MyProfileActivity
 * @see EditProfileActivity
 * @see com.example.lottary.ui.admin.AdminEventsActivity
 *
 * Outstanding Issues / Notes:
 * Assigned admins are hard-coded and won't persist if the admin decides to change their device
 */
public class ProfileInfoFragment extends Fragment {

    private String userDeviceID = "device_demo";
    private TextView infoName, infoEmail, infoPhoneNum;
    private Button btnEditProfile, btnDeleteProfile, adminBtn;
    private List<String> adminIDList = new ArrayList<String>();
    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = requireContext();

        // get current user ID
        userDeviceID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.i("deviceID2", userDeviceID);
        // maintain an ID list of admin
        adminIDList.add("2a03e2bac0988d0f");  // Yitong
        adminIDList.add("c8b0b8e87f4433fd");  // Mengxi
        adminIDList.add("ce9affac94d94f5e");  // Tianyi
        adminIDList.add("8abccf496d141336");  // Han

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

        // fetch info and populate data
        FirestoreUserRepository.get().listenUser(userDeviceID, this::populate);

        // set button listeners
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        btnEditProfile.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), EditProfileActivity.class)));

        btnDeleteProfile = view.findViewById(R.id.btn_delete_profile);
        btnDeleteProfile.setOnClickListener(v -> warnBeforeDelete());

        // ✅ Swap to Admin View — go straight to AdminEventsActivity
        adminBtn = view.findViewById(R.id.btn_swap_admin); // keep your existing id
        // only show to selected admin device IDs
        if (adminBtn != null && adminIDList.contains(userDeviceID)) {
            adminBtn.setVisibility(VISIBLE);
            adminBtn.setOnClickListener(v -> {
                Intent intent = new Intent(context, AdminEventsActivity.class);
                intent.putExtra("open_from_profile", true);
                startActivity(intent);
                // finish current to avoid back-stack/lifecycle weirdness
                requireActivity().finish();
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        FirestoreUserRepository.get().listenUser(userDeviceID, this::populate);
    }

    /**
     * Put information from an user document on display on the appropriate fields. Will automatically
     * return if document is empty
     * @param d - a DocumentSnapshot containing user info to populate
     * @see FirestoreUserRepository
     */
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

    /**
     * Warn the user before allow them to delete the profile. Process deletion when the user confirm
     * and dismiss the dialog when the user deny.
     */
    private void warnBeforeDelete() {
        // create & display dialogue
        new MaterialAlertDialogBuilder(context, R.style.LotteryDialog_Entrant)
                .setTitle(R.string.delete_profile)
                .setMessage("Are you sure that you want to delete your profile?")
                .setNeutralButton(R.string.no, null)
                .setPositiveButton(R.string.yes, (d, w) -> {
                    FirestoreUserRepository.get().deleteUser(userDeviceID)
                            .addOnSuccessListener(ref -> {
                                Toast.makeText(context, "Profile deleted", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(context, BrowseActivity.class);
                                intent.putExtra("new_user", true);
                                startActivity(intent);
                                // finish current to avoid back-stack/lifecycle weirdness
                                requireActivity().finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "Deletion failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                })
                .show();
    }

    /**
     * Null string handler.
     * @param v - a String to transform
     * @return an empty String if parameter is null, if not return the original String.
     */
    private static String n(String v){ return v == null ? "" : v; }
}