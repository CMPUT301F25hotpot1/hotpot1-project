package com.example.lottary.ui.profile;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.lottary.R;

/**
 * A {@link Fragment} subclass that prompts the user to create a profile
 * @author Han Nguyen
 * @version 1.0
 * @see MyProfileActivity
 * @see CreateProfileActivity
 */
public class NewProfileFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // start profile creation activity
        Button btnCreateProfile = view.findViewById(R.id.btn_create_profile);

        btnCreateProfile.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), CreateProfileActivity.class)));

    }
}
