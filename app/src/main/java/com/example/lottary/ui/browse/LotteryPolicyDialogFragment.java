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

/**
 * LotteryPolicyDialogFragment
 *
 * Purpose:
 * - Presents the "Lottery policy" confirmation UI as a dialog.
 * - Provides a simple callback contract to the hosting Activity for when the user accepts.
 *
 * Contract:
 * - The hosting Activity may implement {@link Listener} to receive a one-shot callback
 *   when the user taps the "Yes" button. The boolean parameter indicates the state
 *   of the "Don't ask again" checkbox at the moment of acceptance.
 *
 * Notes:
 * - The content view is inflated from {@code R.layout.dialog_lottery_policy} and embedded
 *   into a MaterialAlertDialog.
 * - This fragment does not persist the "don't ask again" preference; it only reports it.
 *   Persistence (if desired) should be handled by the host.
 */
public class LotteryPolicyDialogFragment extends DialogFragment {

    /**
     * Host callback interface.
     * Implement this on the Activity that shows the dialog if it needs to be notified
     * when the user accepts the policy.
     */
    public interface Listener { void onAcceptPolicy(boolean dontAskAgain); }

    /** Factory method for a new instance with no arguments. */
    public static LotteryPolicyDialogFragment newInstance() { return new LotteryPolicyDialogFragment(); }

    @NonNull @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Inflate the dialog content view.
        View v = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_lottery_policy, null, false);

        // Lookup views inside the inflated layout.
        MaterialCheckBox cb = v.findViewById(R.id.cb_dont_ask);
        MaterialButton btnNo = v.findViewById(R.id.btn_no);
        MaterialButton btnYes = v.findViewById(R.id.btn_yes);

        // Build a Material-styled dialog that hosts the custom view.
        MaterialAlertDialogBuilder b = new MaterialAlertDialogBuilder(requireContext()).setView(v);
        Dialog d = b.create();

        // "No" simply dismisses the dialog without notifying the host.
        btnNo.setOnClickListener(view -> dismiss());

        // "Yes" notifies the host (if it implements Listener) and then dismisses the dialog.
        // The checkbox state is passed as the 'dontAskAgain' parameter.
        btnYes.setOnClickListener(view -> {
            if (getActivity() instanceof Listener) ((Listener) getActivity()).onAcceptPolicy(cb.isChecked());
            dismiss();
        });

        return d;
    }
}


