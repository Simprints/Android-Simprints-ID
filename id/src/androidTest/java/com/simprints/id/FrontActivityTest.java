package com.simprints.id;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.WindowManager;

import com.simprints.id.activities.FrontActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anyOf;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class FrontActivityTest {

    @Rule
    public ActivityTestRule<FrontActivity> frontActivityActivityTestRule = new ActivityTestRule<>(FrontActivity.class);


    @Before
    public void setUp() {
        final FrontActivity activity = frontActivityActivityTestRule.getActivity();
        Runnable wakeUpDevice = new Runnable() {
            public void run() {
                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        };
        activity.runOnUiThread(wakeUpDevice);
    }


    /**
     * Front activity sync button test
     */
    @Test
    public void checkSyncButton() {
        onView(withId(R.id.bt_sync))
                .check(matches(isClickable()))
                .check(matches(anyOf(withText(R.string.sync_data), withText(R.string.not_signed_in))))
                .perform(click())
                .check(matches(anyOf(withText(R.string.sync_data), withText(R.string.not_signed_in))));
    }

    @After
    public void cleanUp() {}
}
