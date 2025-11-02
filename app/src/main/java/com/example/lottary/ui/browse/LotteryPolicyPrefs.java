package com.example.lottary.ui.browse;

import android.content.Context;

public class LotteryPolicyPrefs {
    private static final String P="lottery_policy";
    private static final String K="accepted";
    public static boolean isAccepted(Context c){ return c.getSharedPreferences(P,Context.MODE_PRIVATE).getBoolean(K,false); }
    public static void setAccepted(Context c, boolean v){ c.getSharedPreferences(P,Context.MODE_PRIVATE).edit().putBoolean(K,v).apply(); }
}
