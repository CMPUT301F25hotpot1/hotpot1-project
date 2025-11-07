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
 * MyEventsActivity 的核心交互测试：
 * 1. 默认标签是 Joined Events
 * 2. 能切换到 Created Events
 * 3. 点击 Create Event 跳转 CreateEventActivity
 * 4. 底部导航到 Browse 跳转 BrowseActivity
 * 5. 搜索输入后点击 SEARCH，输入保留且不崩溃
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class MyEventsActivityTest {

    @Before
    public void setUp() { Intents.init(); }

    @After
    public void tearDown() { Intents.release(); }

    /** 启动 MyEventsActivity */
    private ActivityScenario<MyEventsActivity> launch() {
        Intent intent = new Intent(Intent.ACTION_MAIN)
                .setClassName("com.example.lottary", MyEventsActivity.class.getName());
        return ActivityScenario.launch(intent);
    }

    /** 1️⃣ 默认页签应为 Joined Events */
    @Test
    public void defaultTab_isJoinedEvents() {
        launch();
        Espresso.onView(CoreMatchers.allOf(
                        ViewMatchers.withText("Joined Events"),
                        ViewMatchers.isDescendantOfA(ViewMatchers.withId(R.id.tab_layout))
                ))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    /** 2️⃣ 切换到 Created Events 页签 */
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

    /** 3️⃣ 点击 Create Event 按钮应打开 CreateEventActivity */
    @Test
    public void clickingCreate_opensCreateEventActivity() {
        launch();

        // 去掉 scrollTo()，避免 “Animations or transitions are enabled” 异常
        Espresso.onView(ViewMatchers.withId(R.id.btn_create))
                .perform(ViewActions.click());

        Intents.intended(IntentMatchers.hasComponent(CreateEventActivity.class.getName()));
    }


    /** 4️⃣ 底部导航切换到 Browse 应打开 BrowseActivity */
    @Test
    public void bottomNav_browse_opensBrowseActivity() {
        launch();
        Espresso.onView(ViewMatchers.withId(R.id.nav_browse))
                .perform(ViewActions.click());
        Intents.intended(IntentMatchers.hasComponent(BrowseActivity.class.getName()));
    }

    /** 5️⃣ 搜索框输入 + 点击 SEARCH，不崩溃且输入保留 */
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
