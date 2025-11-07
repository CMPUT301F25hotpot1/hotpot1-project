package com.example.lottary;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.lottary.R;
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

/**
 * Basic instrumentation tests for NotificationsActivity.
 *
 * Verifies that:
 *  - NotificationsActivity can be launched without crashing.
 *  - The toolbar title "Notifications" is visible.
 *  - The notifications RecyclerView is visible.
 *  - The bottom navigation bar is visible.
 */
@RunWith(AndroidJUnit4.class)
public class NotificationTest {

    @Before
    public void clearPrefs() {
        // Ensure mute settings do not interfere with the UI for these tests.
        NotifyPrefs.resetAll(ApplicationProvider.getApplicationContext());
    }

    @Test
    public void openNotifications_showsTitleAndList() {
        try (ActivityScenario<NotificationsActivity> scenario =
                     ActivityScenario.launch(NotificationsActivity.class)) {

            // Assert that the toolbar title "Notifications" is visible.
            // Scope the matcher to the toolbar to avoid ambiguity.
            onView(allOf(
                    withText("Notifications"),
                    isDescendantOfA(withId(R.id.top_app_bar))
            )).check(matches(isDisplayed()));

            // Assert that the RecyclerView for notifications is visible.
            onView(withId(R.id.recycler))
                    .check(matches(isDisplayed()));
        }
    }

    @Test
    public void openNotifications_bottomNavIsVisible() {
        try (ActivityScenario<NotificationsActivity> scenario =
                     ActivityScenario.launch(NotificationsActivity.class)) {

            // Assert that the bottom navigation bar is visible.
            onView(withId(R.id.bottomNav))
                    .check(matches(isDisplayed()));
        }
    }
}
