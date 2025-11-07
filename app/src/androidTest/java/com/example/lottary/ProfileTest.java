package com.example.lottary;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.is;

import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.lottary.ui.profile.ProfileActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class ProfileTest {
    @Rule
    public ActivityScenarioRule<ProfileActivity> scenario = new
            ActivityScenarioRule<>(ProfileActivity.class);


    @Test
    public void testCreateProfile(){
        // Swap to profile & check if profile tab is displayed
        onView(withId(R.id.nav_profile)).perform(click());
        onView(withId(R.id.activity_profile)).check(matches(isDisplayed()));

        // Check if new profile fragment is displayed
        onView(withText("Welcome to EventLottery!")).check(matches(isDisplayed()));

        // Create profile
        onView(withId(R.id.btn_create_profile)).perform(click());
        onView(withId(R.id.activity_create_profile)).check(matches(isDisplayed()));

        onView(withId(R.id.et_name)).perform(ViewActions.replaceText("Han Ney"));
        onView(withId(R.id.et_email)).perform(ViewActions.replaceText("hanney@gmail.com"));
        onView(withId(R.id.et_phone_number)).perform(ViewActions.replaceText(""));

        // Click on Confirm
        onView(withId(R.id.btn_create_profile)).perform(click());

        // Check if profile info is displayed correctly
        onView(withId(R.id.activity_profile)).check(matches(isDisplayed()));
        onView(withText("Han Ney")).check(matches(isDisplayed()));
        onView(withText("hanney@gmail.com")).check(matches(isDisplayed()));
        onView(withText("Not provided")).check(matches(isDisplayed()));
    }

    @Test
    public void testEditProfile(){
        // Swap to profile & check if profile tab is displayed
        onView(withId(R.id.nav_profile)).perform(click());
        onView(withId(R.id.activity_profile)).check(matches(isDisplayed()));

        // Check if profile info is displayed correctly
        onView(withText("Han Ney")).check(matches(isDisplayed()));
        onView(withText("hanney@gmail.com")).check(matches(isDisplayed()));
        onView(withText("Not provided")).check(matches(isDisplayed()));

        // Edit profile
        onView(withId(R.id.btn_edit_profile)).perform(click());
        onView(withId(R.id.activity_edit_profile)).check(matches(isDisplayed()));

        onView(withId(R.id.et_name)).perform(ViewActions.typeText("Han Nguyen"));
        onView(withId(R.id.et_email)).perform(ViewActions.typeText("hann@gmail.com"));
        onView(withId(R.id.et_phone_number)).perform(ViewActions.typeText("8877447232"));

        // Click on Confirm
        onView(withId(R.id.btn_edit_profile)).perform(click());

        // Check if profile is updated correctly
        onView(withId(R.id.activity_profile)).check(matches(isDisplayed()));
        onView(withText("Han Nguyen")).check(matches(isDisplayed()));
        onView(withText("hann@gmail.com")).check(matches(isDisplayed()));
        onView(withText("8877447232")).check(matches(isDisplayed()));
    }

    public void testDeleteProfile(){
        // Swap to profile & check if profile tab is displayed
        onView(withId(R.id.nav_profile)).perform(click());
        onView(withId(R.id.activity_profile)).check(matches(isDisplayed()));

        // Check if profile info is displayed correctly
        onView(withText("Han Nguyen")).check(matches(isDisplayed()));
        onView(withText("hann@gmail.com")).check(matches(isDisplayed()));
        onView(withText("8877447232")).check(matches(isDisplayed()));

        // Check if delete profile dialog appears
        onView(withId(R.id.btn_delete_profile)).perform(click());
        onView(withId(R.id.dialog_user_delete_profile)).check(matches(isDisplayed()));

        // Don't delete
        onView(withId(R.id.btn_no)).perform(click());

        // Check if profile info is re_displayed again
        onView(withText("Han Nguyen")).check(matches(isDisplayed()));
        onView(withText("hann@gmail.com")).check(matches(isDisplayed()));
        onView(withText("8877447232")).check(matches(isDisplayed()));

        // Delete profile
        onView(withId(R.id.btn_delete_profile)).perform(click());
        onView(withId(R.id.btn_yes)).perform(click());

        // Check if new profile fragment is displayed
        onView(withText("Welcome to EventLottery!")).check(matches(isDisplayed()));
    }
}
