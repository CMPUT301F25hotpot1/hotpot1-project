package com.example.lottary.ui.browse;

import android.content.Context;

/**
 * LotteryPolicyPrefs
 *
 * Purpose:
 * - Tiny SharedPreferences helper to remember whether the user has accepted
 *   the lottery policy ("Don't ask again" state).
 *
 * Storage:
 * - Uses a private app-level SharedPreferences file named "lottery_policy".
 * - Stores a single boolean key "accepted".
 *
 * Threading:
 * - Writes use {@link android.content.SharedPreferences.Editor#apply()} which is async and safe
 *   for main-thread usage. No blocking I/O on the UI thread.
 *
 * Usage:
 * - Read with {@link #isAccepted(Context)} to decide whether to show the policy dialog.
 * - Persist with {@link #setAccepted(Context, boolean)} when the user accepts.
 */
public class LotteryPolicyPrefs {
    /** Name of the SharedPreferences file used for policy acceptance state. */
    private static final String P = "lottery_policy";
    /** Key under which the acceptance boolean is stored. */
    private static final String K = "accepted";

    /**
     * Returns whether the user has already accepted the policy.
     * @param c context used to access the app's private SharedPreferences.
     * @return true if accepted; false otherwise (default).
     */
    public static boolean isAccepted(Context c){
        return c.getSharedPreferences(P, Context.MODE_PRIVATE).getBoolean(K, false);
    }

    /**
     * Persists the acceptance flag.
     * @param c context used to access the app's private SharedPreferences.
     * @param v new acceptance value (true = accepted / don't ask again).
     */
    public static void setAccepted(Context c, boolean v){
        c.getSharedPreferences(P, Context.MODE_PRIVATE).edit().putBoolean(K, v).apply();
    }
}

