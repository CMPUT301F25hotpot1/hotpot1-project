package com.example.lottary.ui.browse;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 所有筛选项集中到这里，BottomSheet <-> Fragment 都传这个。
 */
public class FilterOptions implements Parcelable {

    private boolean openOnly;
    private boolean geoOnly;
    private long fromDateMs;
    private long toDateMs;
    private final Set<String> types;

    public FilterOptions() {
        this.openOnly = false;
        this.geoOnly = false;
        this.fromDateMs = 0L;
        this.toDateMs = 0L;
        this.types = new HashSet<>();
    }

    /* ===================== getters ===================== */

    public boolean isOpenOnly() {
        return openOnly;
    }

    public boolean isGeoOnly() {
        return geoOnly;
    }

    public long getFromDateMs() {
        return fromDateMs;
    }

    public long getToDateMs() {
        return toDateMs;
    }

    @NonNull
    public Set<String> getTypes() {
        return new HashSet<>(types);
    }


    public FilterOptions setOpenOnly(boolean openOnly) {
        this.openOnly = openOnly;
        return this;
    }

    public FilterOptions setGeoOnly(boolean geoOnly) {
        this.geoOnly = geoOnly;
        return this;
    }

    public FilterOptions setFromDateMs(long fromDateMs) {
        this.fromDateMs = fromDateMs;
        return this;
    }

    public FilterOptions setToDateMs(long toDateMs) {
        this.toDateMs = toDateMs;
        return this;
    }

    public FilterOptions setTypes(Set<String> types) {
        this.types.clear();
        if (types != null) {
            this.types.addAll(types);
        }
        return this;
    }

    /* ===================== Parcelable ===================== */

    protected FilterOptions(Parcel in) {
        openOnly = in.readByte() != 0;
        geoOnly = in.readByte() != 0;
        fromDateMs = in.readLong();
        toDateMs = in.readLong();
        List<String> list = new ArrayList<>();
        in.readStringList(list);
        types = new HashSet<>(list);
    }

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeByte((byte) (openOnly ? 1 : 0));
        dest.writeByte((byte) (geoOnly ? 1 : 0));
        dest.writeLong(fromDateMs);
        dest.writeLong(toDateMs);
        dest.writeStringList(new ArrayList<>(types));
    }
}


