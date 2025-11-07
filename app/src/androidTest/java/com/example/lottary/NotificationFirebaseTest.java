package com.example.lottary;

import android.content.Context;
import android.os.SystemClock;
import android.provider.Settings;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.lottary.ui.notifications.NotificationsActivity;
import com.example.lottary.ui.notifications.NotifyPrefs;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertTrue;

/**
 * Integration style instrumentation test that verifies NotificationsActivity
 * can display a notification document stored in Firebase Firestore
 *
 * This test assumes:
 *  - Firestore is reachable from the emulator/device.
 *  - Security rules allow reads/writes for the seeded test document
 */
@RunWith(AndroidJUnit4.class)
public class NotificationFirebaseTest {

    private static final String TEST_DOC_ID = "ui_test_notification";
    private static final String TEST_MESSAGE = "UI test notification";

    private FirebaseFirestore db;
    private String deviceId;

    @Before
    public void setUp() throws Exception {
        Context app = ApplicationProvider.getApplicationContext();

        //ensure local preferences do not hide notifications
        NotifyPrefs.resetAll(app);

        //use the same device id logic as NotificationsActivity
        deviceId = Settings.Secure.getString(
                app.getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
        if (deviceId == null || deviceId.isEmpty()) {
            deviceId = "device_demo";
        }

        db = FirebaseFirestore.getInstance();

        //seed a test notification document targeted at this device
        Map<String, Object> data = new HashMap<>();
        data.put("recipientId", deviceId);
        data.put("type", "selected");
        data.put("message", TEST_MESSAGE);
        data.put("eventId", "test_event_id");
        data.put("eventTitle", "Test Event");
        data.put("sentAt", Timestamp.now());

        Tasks.await(
                db.collection("notifications")
                        .document(TEST_DOC_ID)
                        .set(data),
                10, TimeUnit.SECONDS
        );
    }

    @After
    public void tearDown() throws Exception {
        if (db != null) {
            Tasks.await(
                    db.collection("notifications")
                            .document(TEST_DOC_ID)
                            .delete(),
                    10, TimeUnit.SECONDS
            );
        }
        NotifyPrefs.resetAll(ApplicationProvider.getApplicationContext());
    }

    @Test
    public void seededNotification_isVisibleInInbox() {
        try (ActivityScenario<NotificationsActivity> scenario =
                     ActivityScenario.launch(NotificationsActivity.class)) {

            //give Firestore snapshot listener a short window to receive data
            SystemClock.sleep(3000);

            //the seeded message should appear in the notifications list
            onView(withText(TEST_MESSAGE))
                    .check(matches(isDisplayed()));
        }
    }

    /**
     * Simple sanity check: launching the activity after seeding must not crash
     */
    @Test
    public void activityLaunch_withFirestoreSeed_doesNotCrash() {
        try (ActivityScenario<NotificationsActivity> scenario =
                     ActivityScenario.launch(NotificationsActivity.class)) {
            assertTrue(true);
        }
    }
}
