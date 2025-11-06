package com.example.lottary.ui.notifications;

import android.content.Context;

public class NotifyPrefs {
    private static final String P = "notify_prefs";
    private static final String K_ALL = "opt_out_all";

    public static boolean isAllOptedOut(Context c) {
        return c.getSharedPreferences(P, Context.MODE_PRIVATE).getBoolean(K_ALL, false);
    }

    public static void setAllOptedOut(Context c, boolean v) {
        c.getSharedPreferences(P, Context.MODE_PRIVATE).edit().putBoolean(K_ALL, v).apply();
    }

    public static boolean isOrganizerOptedOut(Context c, String orgId) {
        return c.getSharedPreferences(P, Context.MODE_PRIVATE).getBoolean("org_"+orgId, false);
    }

    public static void setOrganizerOptedOut(Context c, String orgId, boolean v) {
        c.getSharedPreferences(P, Context.MODE_PRIVATE).edit().putBoolean("org_"+orgId, v).apply();
    }

    /** 一键清空所有静音设置（调试/误触恢复用） */
    public static void resetAll(Context c) {
        c.getSharedPreferences(P, Context.MODE_PRIVATE).edit().clear().apply();
    }
}


