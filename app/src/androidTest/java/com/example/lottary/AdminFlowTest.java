package com.example.lottary;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;

import android.view.View;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AdminFlowTest {

    @Rule
    public ActivityScenarioRule<MainActivity> rule =
            new ActivityScenarioRule<>(MainActivity.class);

    /** Wait helper */
    public static ViewAction waitFor(long ms) {
        return new ViewAction() {
            @Override public Matcher<View> getConstraints() { return isRoot(); }
            @Override public String getDescription() { return "Wait for " + ms + " ms"; }
            @Override public void perform(UiController uiController, View view) {
                uiController.loopMainThreadForAtLeast(ms);
            }
        };
    }

    /** PerformClick() instead of Espresso click */
    private static ViewAction forceClick() {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isDisplayed();
            }

            @Override
            public String getDescription() {
                return "forceClick bottom navigation item";
            }

            @Override
            public void perform(UiController uiController, View view) {
                view.performClick();
                uiController.loopMainThreadUntilIdle();
            }
        };
    }

    /** Dashboard loads first */
    @Test
    public void testAdminDashboardLoads() {
        onView(withId(R.id.admin_events_list))
                .check(matches(isDisplayed()));
    }

    /** Events tab loads */
    @Test
    public void testAdminEventsLoads() {
        onView(isRoot()).perform(waitFor(800));

        onView(withId(R.id.nav_admin_events))
                .perform(forceClick());

        onView(withId(R.id.admin_events_list))
                .check(matches(isDisplayed()));
    }


    @Test
    public void testMainActivityLaunches() {
        // Simply verify the root view is displayed
        onView(isRoot()).check(matches(isDisplayed()));
    }

    @Test
    public void testEventsListCanScroll() {
        onView(withId(R.id.nav_admin_events)).perform(click());

        onView(withId(R.id.admin_events_list))
                .check(matches(isDisplayed()));

        onView(withId(R.id.admin_events_list))
                .perform(RecyclerViewActions.scrollToPosition(0));
    }


}
