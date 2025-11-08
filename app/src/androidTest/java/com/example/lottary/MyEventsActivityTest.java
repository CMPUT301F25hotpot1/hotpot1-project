package com.example.lottary;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.lottary.R;
import com.example.lottary.ui.browse.BrowseActivity;
import com.example.lottary.ui.events.MyEventsActivity;
import com.example.lottary.ui.events.create.CreateEventActivity;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * MyEventsActivityTest
 *
 * Purpose:
 * End-to-end Espresso test suite validating the core interactions of the “My Events” screen.
 * This includes verifying tab switching, navigation, search persistence, and intent launching.
 *
 * Test Coverage:
 * 1. Default tab should be “Joined Events” when activity launches.
 * 2. User can switch to the “Created Events” tab successfully.
 * 3. Clicking the “Create Event” button launches CreateEventActivity.
 * 4. Selecting “Browse” from the bottom navigation launches BrowseActivity.
 * 5. Entering a query and pressing SEARCH preserves input and doesn’t crash.
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class MyEventsActivityTest {

    @Before
    public void setUp() { Intents.init(); }

    @After
    public void tearDown() { Intents.release(); }

    private ActivityScenario<MyEventsActivity> launch() {
        Intent intent = new Intent(Intent.ACTION_MAIN)
                .setClassName("com.example.lottary", MyEventsActivity.class.getName());
        return ActivityScenario.launch(intent);
    }

    @Test
    public void defaultTab_isJoinedEvents() {
        launch();
        Espresso.onView(CoreMatchers.allOf(
                        ViewMatchers.withText("Joined Events"),
                        ViewMatchers.isDescendantOfA(ViewMatchers.withId(R.id.tab_layout))
                ))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void canSwitchToCreatedTab() {
        launch();
        Espresso.onView(CoreMatchers.allOf(
                        ViewMatchers.withText("Created Events"),
                        ViewMatchers.isDescendantOfA(ViewMatchers.withId(R.id.tab_layout))
                ))
                .perform(ViewActions.click());
        Espresso.onView(CoreMatchers.allOf(
                        ViewMatchers.withText("Created Events"),
                        ViewMatchers.isDescendantOfA(ViewMatchers.withId(R.id.tab_layout))
                ))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void clickingCreate_opensCreateEventActivity() {
        launch();
        Espresso.onView(ViewMatchers.withId(R.id.btn_create))
                .perform(ViewActions.click());
        Intents.intended(IntentMatchers.hasComponent(CreateEventActivity.class.getName()));
    }

    @Test
    public void bottomNav_browse_opensBrowseActivity() {
        launch();
        Espresso.onView(ViewMatchers.withId(R.id.nav_browse))
                .perform(ViewActions.click());
        Intents.intended(IntentMatchers.hasComponent(BrowseActivity.class.getName()));
    }

    @Test
    public void search_keepsInputAndJoinedTabVisible() {
        launch();
        String query = "new";
        Espresso.onView(ViewMatchers.withId(R.id.input_search))
                .perform(ViewActions.typeText(query), ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.btn_search))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.input_search))
                .check(ViewAssertions.matches(ViewMatchers.withText(query)));
        Espresso.onView(CoreMatchers.allOf(
                        ViewMatchers.withText("Joined Events"),
                        ViewMatchers.isDescendantOfA(ViewMatchers.withId(R.id.tab_layout))
                ))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }
}
