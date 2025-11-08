
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

/**
 * FilterBottomSheet
 *
 * Purpose / Role:
 * - Presents a modal bottom sheet that lets the user configure filter options for the Browse list:
 *   - Open events only
 *   - Geolocation-enabled events only
 *   - From / To date range (yyyy-MM-dd)
 *   - Coarse type buckets (Sports, Music, Arts & Crafts, Market)
 *
 * Lifecycle & Contract:
 * - Use {@link #newInstance(FilterOptions, Listener)} to pass current defaults and a callback.
 * - The selected options are returned via {@link Listener#onApply(FilterOptions)} and the sheet
 *   dismisses itself afterwards.
 *
 * UX Notes:
 * - Date fields are made non-focusable and open a native DatePickerDialog when tapped.
 * - Invalid date strings are safely ignored (field left unset).
 */
public class FilterBottomSheet extends BottomSheetDialogFragment {

    /** Callback interface used to deliver the final options back to the host. */
    interface Listener { void onApply(@NonNull FilterOptions opts); }

    /**
     * Factory method.
     * @param defaults current/default options to pre-populate the sheet.
     * @param l        required listener; invoked when the user taps "Apply".
     */
    public static FilterBottomSheet newInstance(@NonNull FilterOptions defaults, @NonNull Listener l){
        FilterBottomSheet f = new FilterBottomSheet();
        Bundle b = new Bundle(); b.putParcelable("opts", defaults);
        f.setArguments(b); f.listener = l; return f;
    }

    /** Non-null at runtime; set by {@link #newInstance(FilterOptions, Listener)}. */
    private Listener listener;
    /** Defaults used to pre-fill the controls when the sheet is shown. */
    private FilterOptions defaults;

    // --- Views bound from layout ---

    /** "Open events only" switch. */
    private Switch swOpenOnly, swGeo;
    /** Type category checkboxes. */
    private CheckBox cbSports, cbMusic, cbArts, cbMarket;
    /** From/To date fields (formatted as yyyy-MM-dd). */
    private EditText etFrom, etTo;
    /** Primary/secondary actions. */
    private Button btnApply, btnCancel;

    /** Single source of truth for parsing/formatting dates in this sheet. */
    private final SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    @Nullable
    @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the bottom sheet layout.
        return inflater.inflate(R.layout.fragment_filter_sheet, container, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        // Read defaults passed by the host (may be null if not provided).
        defaults = getArguments() == null ? new FilterOptions() : getArguments().getParcelable("opts");

        // View lookups.
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

        // Initialize controls from defaults (if any).
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

        // Make date fields open a date picker instead of the keyboard.
        etFrom.setFocusable(false);
        etTo.setFocusable(false);
        etFrom.setOnClickListener(v1 -> pickDate(etFrom));
        etTo.setOnClickListener(v12 -> pickDate(etTo));

        // Primary actions.
        btnApply.setOnClickListener(v13 -> applyAndDismiss());
        btnCancel.setOnClickListener(v14 -> dismiss());
    }

    /**
     * Show a {@link DatePickerDialog} and write the result into the target EditText
     * using the shared {@link #DF} formatter.
     */
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

    /**
     * Collect values from all controls, build a new {@link FilterOptions} instance,
     * deliver it to the host via {@link #listener}, then dismiss the sheet.
     *
     * Error handling:
     * - Date parsing errors are ignored (the corresponding bound is left unset).
     * - Empty text is treated as "no bound".
     */
    private void applyAndDismiss() {
        FilterOptions out = new FilterOptions()
                .setOpenOnly(swOpenOnly.isChecked())
                .setGeoOnly(swGeo.isChecked());

        // Parse from/to dates if present.
        String sFrom = etFrom.getText().toString().trim();
        String sTo   = etTo.getText().toString().trim();
        try {
            if (!TextUtils.isEmpty(sFrom)) out.setFromDateMs(DF.parse(sFrom).getTime());
        } catch (ParseException ignore) {}
        try {
            if (!TextUtils.isEmpty(sTo)) out.setToDateMs(DF.parse(sTo).getTime());
        } catch (ParseException ignore) {}

        // Collect selected type buckets.
        Set<String> types = new HashSet<>();
        if (cbSports.isChecked()) types.add("Sports");
        if (cbMusic.isChecked()) types.add("Music");
        if (cbArts.isChecked()) types.add("Arts & Crafts");
        if (cbMarket.isChecked()) types.add("Market");
        out.setTypes(types);

        // Return to host and close.
        if (listener != null) listener.onApply(out);
        dismiss();
    }
}


