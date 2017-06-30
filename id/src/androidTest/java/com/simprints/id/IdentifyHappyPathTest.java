package com.simprints.id;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.WindowManager;

import com.simprints.id.activities.LaunchActivity;
import com.simprints.libsimprints.Constants;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class IdentifyHappyPathTest {
    private static final String apiKey = "1b22a8e0-6e86-4d8b-92f7-905bd38ac1d5";
    private static final String userId = "1b22a8e0-6e86-4d8b-92f7-905bd38ac1d5";
    private static final String moduleId = "0";

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
     * LaunchActivity behaves
     */
    @Test
    public void setupActivityBehaves() {
        onView(withText(R.string.short_consent))
                .check(matches(isDisplayed()));
    }

    @After
    public void cleanUp() {}
}
