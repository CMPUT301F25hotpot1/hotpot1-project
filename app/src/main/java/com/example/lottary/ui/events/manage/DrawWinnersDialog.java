package com.example.lottary.ui.events.manage;

import android.app.Dialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.lottary.R;
import com.example.lottary.data.FirestoreEventRepository;

public class DrawWinnersDialog extends DialogFragment {

    private static final String ARG_EVENT_ID = "ARG_EVENT_ID";

    public static DrawWinnersDialog newInstance(@NonNull String eventId) {
        DrawWinnersDialog d = new DrawWinnersDialog();
        Bundle b = new Bundle();
        b.putString(ARG_EVENT_ID, eventId);
        d.setArguments(b);
        return d;
    }

    @NonNull @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        String eventId = getArguments() != null ? getArguments().getString(ARG_EVENT_ID) : null;
        if (eventId == null) eventId = "";

        View v = getLayoutInflater().inflate(R.layout.dialog_draw_winners, null);
        RadioGroup group = v.findViewById(R.id.grp_mode);
        RadioButton rbFill = v.findViewById(R.id.rb_fill);
        RadioButton rbSpecify = v.findViewById(R.id.rb_specify);
        EditText etAmount = v.findViewById(R.id.et_amount);
        etAmount.setInputType(InputType.TYPE_CLASS_NUMBER);

        rbFill.setChecked(true);
        etAmount.setEnabled(false);

        group.setOnCheckedChangeListener((g, checkedId) -> {
            boolean specify = checkedId == R.id.rb_specify;
            etAmount.setEnabled(specify);
            if (!specify) etAmount.setText("");
        });

        String finalEventId = eventId;
        return new AlertDialog.Builder(requireContext())
                .setTitle("Draw Winners")
                .setView(v)
                .setNegativeButton("Cancel", (d, w) -> dismiss())
                .setPositiveButton("Draw", (d, w) -> {
                    int maxToDraw = 0; // 0 = fill capacity（在仓库里会自动按剩余名额抽）
                    if (rbSpecify.isChecked()) {
                        try {
                            maxToDraw = Integer.parseInt(etAmount.getText().toString().trim());
                            if (maxToDraw < 0) maxToDraw = 0;
                        } catch (Exception ignore) { maxToDraw = 0; }
                    }
                    FirestoreEventRepository.get().drawWinners(finalEventId, maxToDraw)
                            .addOnSuccessListener(x -> Toast.makeText(requireContext(), "Winners drawn", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
                })
                .create();
    }
}
