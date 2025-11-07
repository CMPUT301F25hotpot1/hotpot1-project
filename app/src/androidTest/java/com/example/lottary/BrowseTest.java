package com.example.lottary;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.lottary.ui.browse.BrowseActivity;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class BrowseTest {

    @Test
    public void openBrowse_showsHeaderSearchAndList() {
        try (ActivityScenario<BrowseActivity> scenario =
                     ActivityScenario.launch(BrowseActivity.class)) {

            onView(withId(R.id.title)).check(matches(isDisplayed()));

            onView(withId(R.id.input_search)).check(matches(isDisplayed()));
            onView(withId(R.id.btn_search)).check(matches(isDisplayed()));
            
            onView(withId(R.id.recycler)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void openBrowse_bottomNavIsVisible() {
        try (ActivityScenario<BrowseActivity> scenario =
                     ActivityScenario.launch(BrowseActivity.class)) {
            onView(withId(R.id.bottomNav)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void tapFilterButton_opensAndClosesFilterSheet() {
        try (ActivityScenario<BrowseActivity> scenario =
                     ActivityScenario.launch(BrowseActivity.class)) {

            onView(withId(R.id.btn_filter)).perform(click());

            onView(withId(R.id.sw_open_only)).check(matches(isDisplayed()));
            onView(withId(R.id.btn_apply)).check(matches(isDisplayed()));

            onView(withId(R.id.btn_cancel)).perform(click());

            onView(withId(R.id.recycler)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void search_anyKeyword_shouldNotCrashAndListStillVisible() {
        try (ActivityScenario<BrowseActivity> scenario =
                     ActivityScenario.launch(BrowseActivity.class)) {

            onView(withId(R.id.input_search)).perform(replaceText("swim"));
            onView(withId(R.id.btn_search)).perform(click());

            onView(withId(R.id.recycler)).check(matches(isDisplayed()));
        }
    }
}

