package com.example.lottary;

import android.content.Context;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.lottary.R;
import com.example.lottary.ui.notifications.NotificationItem;
import com.example.lottary.ui.notifications.NotificationsActivity;
import com.example.lottary.ui.notifications.NotifyPrefs;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Instrumentation tests for the notifications feature
 *
 * Verifies that:
 *  - NotificationsActivity UI loads without crashing
 *  - The toolbar title "Notifications" is visible
 *  - The notifications RecyclerView is visible
 *  - The bottom navigation bar is visible
 *  - NotificationItem model behaves as expected
 *  - NotifyPrefs correctly stores and clears opt-out flags
 */
@RunWith(AndroidJUnit4.class)
public class NotificationTest {

    @Before
    public void clearPrefs() {
        //ensure mute settings do not interfere with the UI or prefs tests.
        NotifyPrefs.resetAll(ApplicationProvider.getApplicationContext());
    }

    //Activity tests

    @Test
    public void openNotifications_showsTitleAndList() {
        try (ActivityScenario<NotificationsActivity> scenario =
                     ActivityScenario.launch(NotificationsActivity.class)) {

            //assert that the toolbar title "Notifications" is visible
            //scope the matcher to the toolbar to avoid ambiguity
            onView(allOf(
                    withText("Notifications"),
                    isDescendantOfA(withId(R.id.top_app_bar))
            )).check(matches(isDisplayed()));

            //assert that the RecyclerView for notifications is visible
            onView(withId(R.id.recycler))
                    .check(matches(isDisplayed()));
        }
    }

    @Test
    public void openNotifications_bottomNavIsVisible() {
        try (ActivityScenario<NotificationsActivity> scenario =
                     ActivityScenario.launch(NotificationsActivity.class)) {

            //assert that the bottom navigation bar is visible
            onView(withId(R.id.bottomNav))
                    .check(matches(isDisplayed()));
        }
    }

    //tests:NotificationItem

    @Test
    public void notificationItem_constructorNormalizesNulls() {
        NotificationItem item = new NotificationItem(
                null, null, null, null, null, 0L, null, null
        );

        //all nullable string fields should be normalized to empty strings
        assertEquals("", item.id);
        assertEquals("", item.eventId);
        assertEquals("", item.targetGroup);
        assertEquals("", item.type);
        assertEquals("", item.message);
        assertEquals("", item.eventTitle);
        assertEquals("", item.organizerId);
    }

    @Test
    public void notificationItem_fullConstructorSetsFields() {
        NotificationItem item = new NotificationItem(
                "id1", "ev1", "selected",
                "selected", "message", 123L,
                "Event Title", "org1"
        );

        assertEquals("id1", item.id);
        assertEquals("ev1", item.eventId);
        assertEquals("selected", item.targetGroup);
        assertEquals("selected", item.type);
        assertEquals("message", item.message);
        assertEquals(123L, item.sentAtMs);
        assertEquals("Event Title", item.eventTitle);
        assertEquals("org1", item.organizerId);
    }

    //tests:NotifyPrefs

    @Test
    public void notifyPrefs_globalOptOut_roundTrip() {
        Context context = ApplicationProvider.getApplicationContext();
        NotifyPrefs.resetAll(context);

        assertFalse(NotifyPrefs.isAllOptedOut(context));

        NotifyPrefs.setAllOptedOut(context, true);
        assertTrue(NotifyPrefs.isAllOptedOut(context));

        NotifyPrefs.setAllOptedOut(context, false);
        assertFalse(NotifyPrefs.isAllOptedOut(context));
    }

    @Test
    public void notifyPrefs_organizerOptOut_roundTrip() {
        Context context = ApplicationProvider.getApplicationContext();
        NotifyPrefs.resetAll(context);

        String orgId = "org123";

        assertFalse(NotifyPrefs.isOrganizerOptedOut(context, orgId));

        NotifyPrefs.setOrganizerOptedOut(context, orgId, true);
        assertTrue(NotifyPrefs.isOrganizerOptedOut(context, orgId));

        NotifyPrefs.setOrganizerOptedOut(context, orgId, false);
        assertFalse(NotifyPrefs.isOrganizerOptedOut(context, orgId));
    }

    @Test
    public void notifyPrefs_resetAll_clearsAllFlags() {
        Context context = ApplicationProvider.getApplicationContext();

        NotifyPrefs.setAllOptedOut(context, true);
        NotifyPrefs.setOrganizerOptedOut(context, "orgX", true);

        NotifyPrefs.resetAll(context);

        assertFalse(NotifyPrefs.isAllOptedOut(context));
        assertFalse(NotifyPrefs.isOrganizerOptedOut(context, "orgX"));
    }
}
