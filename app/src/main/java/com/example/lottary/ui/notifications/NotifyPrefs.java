/*
 * NotifyPrefs.java
 *
 * Utility class for reading and writing notification preference flags
 * Preferences are stored in SharedPreferences and control whether the
 * user wants to receive notifications globally or from specific organizers
 */

package com.example.lottary.ui.notifications;

import android.content.Context;

/**
 * Static helper methods for managing notification opt-out preferences
 */
public class NotifyPrefs {
    private static final String P = "notify_prefs";
    private static final String K_ALL = "opt_out_all";

    /**
     * Returns whether the user has opted out of all notifications
     *
     * @param c context used to access SharedPreferences
     * @return true if all notifications are disabled; false otherwise
     */
    public static boolean isAllOptedOut(Context c) {
        return c.getSharedPreferences(P, Context.MODE_PRIVATE).getBoolean(K_ALL, false);
    }

    /**
     * Sets the global "opt out of all notifications" flag
     *
     * @param c context used to access SharedPreferences
     * @param v true to disable all notifications; false to enable
     */
    public static void setAllOptedOut(Context c, boolean v) {
        c.getSharedPreferences(P, Context.MODE_PRIVATE).edit().putBoolean(K_ALL, v).apply();
    }

    /**
     * Returns whether notifications from the given organizer are muted
     *
     * @param c     context used to access SharedPreferences
     * @param orgId organizer identifier
     * @return true if notifications from this organizer are disabled; false otherwise
     */
    public static boolean isOrganizerOptedOut(Context c, String orgId) {
        return c.getSharedPreferences(P, Context.MODE_PRIVATE).getBoolean("org_"+orgId, false);
    }

    /**
     * Sets the opt-out flag for notifications from a specific organizer
     *
     * @param c     context used to access SharedPreferences
     * @param orgId organizer identifier
     * @param v     true to mute this organizer; false to unmute
     */
    public static void setOrganizerOptedOut(Context c, String orgId, boolean v) {
        c.getSharedPreferences(P, Context.MODE_PRIVATE).edit().putBoolean("org_"+orgId, v).apply();
    }

    /**
     * Clears all notification opt-out preferences
     *
     * @param c context used to access SharedPreferences
     */
    public static void resetAll(Context c) {
        c.getSharedPreferences(P, Context.MODE_PRIVATE).edit().clear().apply();
    }
}

