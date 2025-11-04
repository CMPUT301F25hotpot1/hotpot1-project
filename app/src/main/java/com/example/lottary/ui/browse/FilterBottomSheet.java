package com.example.lottary.ui.browse;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.lottary.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class FilterBottomSheet extends BottomSheetDialogFragment {

    interface Listener { void onApply(@NonNull FilterOptions opts); }

    public static FilterBottomSheet newInstance(@NonNull FilterOptions defaults, @NonNull Listener l){
        FilterBottomSheet f = new FilterBottomSheet();
        Bundle b = new Bundle(); b.putParcelable("opts", defaults);
        f.setArguments(b); f.listener = l; return f;
    }

    private Listener listener;
    private FilterOptions defaults;

    // views
    private Switch swOpenOnly, swGeo;
    private CheckBox cbSports, cbMusic, cbArts, cbMarket;
    private EditText etFrom, etTo;
    private Button btnApply, btnCancel;

    private final SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    @Nullable
    @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_filter_sheet, container, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        defaults = getArguments() == null ? new FilterOptions() : getArguments().getParcelable("opts");

        swOpenOnly = v.findViewById(R.id.sw_open_only);
        swGeo      = v.findViewById(R.id.sw_geo_only);
        cbSports   = v.findViewById(R.id.cb_sports);
        cbMusic    = v.findViewById(R.id.cb_music);
        cbArts     = v.findViewById(R.id.cb_arts);
        cbMarket   = v.findViewById(R.id.cb_market);
        etFrom     = v.findViewById(R.id.et_from_date);
        etTo       = v.findViewById(R.id.et_to_date);
        btnApply   = v.findViewById(R.id.btn_apply);
        btnCancel  = v.findViewById(R.id.btn_cancel);

        if (defaults != null) {
            swOpenOnly.setChecked(defaults.isOpenOnly());
            swGeo.setChecked(defaults.isGeoOnly());
            if (defaults.getFromDateMs() > 0) etFrom.setText(DF.format(defaults.getFromDateMs()));
            if (defaults.getToDateMs() > 0)   etTo.setText(DF.format(defaults.getToDateMs()));
            if (defaults.getTypes().contains("Sports")) cbSports.setChecked(true);
            if (defaults.getTypes().contains("Music")) cbMusic.setChecked(true);
            if (defaults.getTypes().contains("Arts & Crafts")) cbArts.setChecked(true);
            if (defaults.getTypes().contains("Market")) cbMarket.setChecked(true);
        }

        etFrom.setFocusable(false);
        etTo.setFocusable(false);
        etFrom.setOnClickListener(v1 -> pickDate(etFrom));
        etTo.setOnClickListener(v12 -> pickDate(etTo));

        btnApply.setOnClickListener(v13 -> applyAndDismiss());
        btnCancel.setOnClickListener(v14 -> dismiss());
    }

    private void pickDate(EditText target){
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(
                requireContext(),
                (view, y, m, d) -> {
                    cal.set(Calendar.YEAR, y);
                    cal.set(Calendar.MONTH, m);
                    cal.set(Calendar.DAY_OF_MONTH, d);
                    target.setText(DF.format(cal.getTime()));
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void applyAndDismiss() {
        FilterOptions out = new FilterOptions()
                .setOpenOnly(swOpenOnly.isChecked())
                .setGeoOnly(swGeo.isChecked());

        String sFrom = etFrom.getText().toString().trim();
        String sTo   = etTo.getText().toString().trim();
        try {
            if (!TextUtils.isEmpty(sFrom)) out.setFromDateMs(DF.parse(sFrom).getTime());
        } catch (ParseException ignore) {}
        try {
            if (!TextUtils.isEmpty(sTo)) out.setToDateMs(DF.parse(sTo).getTime());
        } catch (ParseException ignore) {}

        Set<String> types = new HashSet<>();
        if (cbSports.isChecked()) types.add("Sports");
        if (cbMusic.isChecked()) types.add("Music");
        if (cbArts.isChecked()) types.add("Arts & Crafts");
        if (cbMarket.isChecked()) types.add("Market");
        out.setTypes(types);

        if (listener != null) listener.onApply(out);
        dismiss();
    }
}


