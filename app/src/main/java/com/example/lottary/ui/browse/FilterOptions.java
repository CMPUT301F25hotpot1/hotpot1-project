package com.example.lottary.ui.browse;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * FilterOptions
 *
 * Purpose / Role:
 * - A simple, parcelable container that holds all filter criteria used by the Browse screen.
 * - Passed between the Filter bottom sheet and the Browse list fragment to keep UI in sync.
 *
 * Invariants:
 * - {@code types} never null; getters return a defensive copy to avoid external mutation.
 *
 * Time Semantics:
 * - {@code fromDateMs} / {@code toDateMs} are UNIX epoch times in milliseconds.
 *   A value of 0 means "unset" (no bound).
 *
 * Mutability:
 * - Fluent setters return {@code this} so callers can chain multiple calls.
 *
 * Parcelable:
 * - Booleans are marshalled as bytes.
 * - {@code types} are written as a list of strings.
 *
 */
public class FilterOptions implements Parcelable {

    /** Show only events that are still open (not ended, not full). */
    private boolean openOnly;
    /** Show only events that have geolocation enabled. */
    private boolean geoOnly;
    /** Inclusive lower bound of event start time (epoch millis); 0 = no lower bound. */
    private long fromDateMs;
    /** Inclusive upper bound of event start time (epoch millis); 0 = no upper bound. */
    private long toDateMs;
    /** Selected coarse type buckets (e.g., "Sports", "Music", "Arts & Crafts", "Market"). */
    private final Set<String> types;

    /** Construct a new instance with all filters disabled/unset. */
    public FilterOptions() {
        this.openOnly = false;
        this.geoOnly = false;
        this.fromDateMs = 0L;
        this.toDateMs = 0L;
        this.types = new HashSet<>();
    }

    /* ===================== getters ===================== */

    /** @return true if "open events only" is enabled. */
    public boolean isOpenOnly() {
        return openOnly;
    }

    /** @return true if "geolocation-only" is enabled. */
    public boolean isGeoOnly() {
        return geoOnly;
    }

    /** @return epoch millis for the start-date lower bound; 0 means "unset". */
    public long getFromDateMs() {
        return fromDateMs;
    }

    /** @return epoch millis for the start-date upper bound; 0 means "unset". */
    public long getToDateMs() {
        return toDateMs;
    }

    /**
     * @return a defensive copy of selected type buckets.
     *         The internal set remains private and unexposed.
     */
    @NonNull
    public Set<String> getTypes() {
        return new HashSet<>(types);
    }

    /* ===================== fluent setters ===================== */

    /** Enable/disable "open events only". */
    public FilterOptions setOpenOnly(boolean openOnly) {
        this.openOnly = openOnly;
        return this;
    }

    /** Enable/disable "geolocation-only". */
    public FilterOptions setGeoOnly(boolean geoOnly) {
        this.geoOnly = geoOnly;
        return this;
    }

    /** Set the inclusive start-date lower bound (epoch millis); use 0 to clear. */
    public FilterOptions setFromDateMs(long fromDateMs) {
        this.fromDateMs = fromDateMs;
        return this;
    }

    /** Set the inclusive start-date upper bound (epoch millis); use 0 to clear. */
    public FilterOptions setToDateMs(long toDateMs) {
        this.toDateMs = toDateMs;
        return this;
    }

    /**
     * Replace the current type selection.
     * Passing {@code null} clears all selections.
     */
    public FilterOptions setTypes(Set<String> types) {
        this.types.clear();
        if (types != null) {
            this.types.addAll(types);
        }
        return this;
    }

    /* ===================== Parcelable ===================== */

    /** Recreate from a Parcel. Field order must match {@link #writeToParcel(Parcel, int)}. */
    protected FilterOptions(Parcel in) {
        openOnly = in.readByte() != 0;
        geoOnly = in.readByte() != 0;
        fromDateMs = in.readLong();
        toDateMs = in.readLong();
        List<String> list = new ArrayList<>();
        in.readStringList(list);
        types = new HashSet<>(list);
    }

    /** Parcelable creator boilerplate. */
    public static final Creator<FilterOptions> CREATOR = new Creator<FilterOptions>() {
        @Override
        public FilterOptions createFromParcel(Parcel in) {
            return new FilterOptions(in);
        }

        @Override
        public FilterOptions[] newArray(int size) {
            return new FilterOptions[size];
        }
    };

    /** No special file descriptors; always returns 0. */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Marshal the object into a Parcel. The read order must mirror the write order.
     * Booleans are written as bytes (1/0). Types are serialized as a list of strings.
     */
    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeByte((byte) (openOnly ? 1 : 0));
        dest.writeByte((byte) (geoOnly ? 1 : 0));
        dest.writeLong(fromDateMs);
        dest.writeLong(toDateMs);
        dest.writeStringList(new ArrayList<>(types));
    }
}



