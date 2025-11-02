package com.example.lottary.ui.browse;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.lottary.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class LotteryPolicyDialogFragment extends DialogFragment {
    public interface Listener { void onAcceptPolicy(boolean dontAskAgain); }

    public static LotteryPolicyDialogFragment newInstance() { return new LotteryPolicyDialogFragment(); }

    @NonNull @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View v = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_lottery_policy, null, false);
        MaterialCheckBox cb = v.findViewById(R.id.cb_dont_ask);
        MaterialButton btnNo = v.findViewById(R.id.btn_no);
        MaterialButton btnYes = v.findViewById(R.id.btn_yes);
        MaterialAlertDialogBuilder b = new MaterialAlertDialogBuilder(requireContext()).setView(v);
        Dialog d = b.create();
        btnNo.setOnClickListener(view -> dismiss());
        btnYes.setOnClickListener(view -> {
            if (getActivity() instanceof Listener) ((Listener) getActivity()).onAcceptPolicy(cb.isChecked());
            dismiss();
        });
        return d;
    }
}

