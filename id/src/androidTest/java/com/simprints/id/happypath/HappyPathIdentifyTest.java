package com.simprints.id.happypath;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.WindowManager;

import com.simprints.id.R;
import com.simprints.id.activities.LaunchActivity;
import com.simprints.libsimprints.Constants;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.schibsted.spain.barista.BaristaSleepActions.sleep;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class HappyPathIdentifyTest {
    private static final String apiKey = "00000000-0000-0000-0000-000000000000";
    private static final String userId = "the_lone_user";
    private static final String moduleId = "the_one_and_only_module";

    @Rule
    public ActivityTestRule<LaunchActivity> launchActivityActivityTestRule = new ActivityTestRule<LaunchActivity>(LaunchActivity.class) {
        @Override
        protected Intent getActivityIntent() {
            Context targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
            Intent result = new Intent(targetContext, LaunchActivity.class);
            result.setAction(Constants.SIMPRINTS_IDENTIFY_INTENT);
            result.putExtra(Constants.SIMPRINTS_API_KEY, apiKey);
            result.putExtra(Constants.SIMPRINTS_USER_ID, userId);
            result.putExtra(Constants.SIMPRINTS_MODULE_ID, moduleId);
            return result;
        }
    };


    @Before
    public void setUp() {
        final LaunchActivity activity = launchActivityActivityTestRule.getActivity();
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
     * SetupActivity:
     * Wait for setup activity to finish loading. Once it has, click through to the next.
     *
     * MainActivity:
     * Click scan. Wait for a good print. Click scan again. Wait for another good print.
     * Click through.
     */
    @Test
    public void successfulEnrol() {

        onView(ViewMatchers.withText(R.string.short_consent))
                .check(matches(isDisplayed()));

        sleep(12, TimeUnit.SECONDS);

        onView(withText(R.string.confirm_consent))
                .check(matches(isDisplayed()))
                .perform(click());

        sleep(2, TimeUnit.SECONDS);

        onView(withText(R.string.scan))
                .check(matches(isDisplayed()))
                .perform(click());

        sleep(8, TimeUnit.SECONDS);

        onView(withText(R.string.scan))
                .check(matches(isDisplayed()))
                .perform(click());

        sleep(8, TimeUnit.SECONDS);

        onView(withId(R.id.action_forward))
                .check(matches(isClickable()))
                .perform(click());

        SystemClock.sleep(10000);
        // Check that the patient was matched correctly
    }

    @After
    public void cleanUp() {}
}
