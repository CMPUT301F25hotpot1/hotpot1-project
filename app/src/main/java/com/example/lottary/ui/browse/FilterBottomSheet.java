package com.example.lottary.ui.browse;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.lottary.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class FilterBottomSheet extends BottomSheetDialogFragment {
    public interface Listener { void onApply(FilterOptions opts); }

    private static final String ARG = "opts";
    public static FilterBottomSheet newInstance(FilterOptions o) {
        Bundle b = new Bundle(); b.putParcelable(ARG, o);
        FilterBottomSheet f = new FilterBottomSheet(); f.setArguments(b); return f;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_filter_sheet, container, false);
        FilterOptions opts = getArguments() == null ? new FilterOptions() : getArguments().getParcelable(ARG);
        SwitchMaterial swOpen = v.findViewById(R.id.sw_open_only);
        MaterialButton btnCancel = v.findViewById(R.id.btn_cancel);
        MaterialButton btnApply = v.findViewById(R.id.btn_apply);
        if (opts != null) swOpen.setChecked(opts.openOnly);
        btnCancel.setOnClickListener(view -> dismiss());
        btnApply.setOnClickListener(view -> {
            FilterOptions out = new FilterOptions();
            out.openOnly = swOpen.isChecked();
            if (getParentFragment() instanceof Listener) ((Listener) getParentFragment()).onApply(out);
            if (getActivity() instanceof Listener) ((Listener) getActivity()).onApply(out);
            dismiss();
        });
        return v;
    }
}

