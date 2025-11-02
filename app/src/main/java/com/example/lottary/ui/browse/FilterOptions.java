package com.example.lottary.ui.browse;

import android.os.Parcel;
import android.os.Parcelable;

public class FilterOptions implements Parcelable {
    public boolean openOnly;

    public FilterOptions() {}

    protected FilterOptions(Parcel in) { openOnly = in.readByte() != 0; }

    public static final Creator<FilterOptions> CREATOR = new Creator<FilterOptions>() {
        @Override public FilterOptions createFromParcel(Parcel in) { return new FilterOptions(in); }
        @Override public FilterOptions[] newArray(int size) { return new FilterOptions[size]; }
    };

    @Override public int describeContents() { return 0; }
    @Override public void writeToParcel(Parcel dest, int flags) { dest.writeByte((byte) (openOnly ? 1 : 0)); }
}
