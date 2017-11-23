package com.simprints.id.bucket01;


import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;
import android.view.WindowManager;

import com.simprints.id.FirstUseTest;
import com.simprints.id.R;
import com.simprints.id.activities.LaunchActivity;
import com.simprints.libdata.models.realm.RealmConfig;
import com.simprints.libsimprints.Constants;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import io.realm.Realm;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.schibsted.spain.barista.BaristaSleepActions.sleep;
import static org.hamcrest.Matchers.allOf;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class HappyWorkflow extends FirstUseTest {
    private static final String apiKey = "00000000-0000-0000-0000-000000000000";
    private static final String userId = "the_lone_user";
    private static final String moduleId = "the_one_and_only_module";

    public ActivityTestRule<LaunchActivity> launchActivityActivityTestRuleEnrol = new ActivityTestRule<LaunchActivity>(LaunchActivity.class, false, true) {
        @Override
        protected Intent getActivityIntent() {
            Context targetContext = getInstrumentation().getTargetContext();
            Intent result = new Intent(targetContext, LaunchActivity.class);
            result.setAction(Constants.SIMPRINTS_REGISTER_INTENT);
            result.putExtra(Constants.SIMPRINTS_API_KEY, apiKey);
            result.putExtra(Constants.SIMPRINTS_USER_ID, userId);
            result.putExtra(Constants.SIMPRINTS_MODULE_ID, moduleId);
            return result;
        }
    };

    public ActivityTestRule<LaunchActivity> launchActivityActivityTestRuleIdentify = new ActivityTestRule<LaunchActivity>(LaunchActivity.class, false, false) {
        @Override
        protected Intent getActivityIntent() {
            Context targetContext = getInstrumentation().getTargetContext();
            Intent result = new Intent(targetContext, LaunchActivity.class);
            result.setAction(Constants.SIMPRINTS_IDENTIFY_INTENT);
            result.putExtra(Constants.SIMPRINTS_API_KEY, apiKey);
            result.putExtra(Constants.SIMPRINTS_USER_ID, userId);
            result.putExtra(Constants.SIMPRINTS_MODULE_ID, moduleId);
            return result;
        }
    };

    public ActivityTestRule<LaunchActivity> launchActivityActivityTestRuleVerify = new ActivityTestRule<LaunchActivity>(LaunchActivity.class, false, false) {
        @Override
        protected Intent getActivityIntent() {
            Context targetContext = getInstrumentation().getTargetContext();
            Intent result = new Intent(targetContext, LaunchActivity.class);
            result.setAction(Constants.SIMPRINTS_VERIFY_INTENT);
            result.putExtra(Constants.SIMPRINTS_API_KEY, apiKey);
            result.putExtra(Constants.SIMPRINTS_USER_ID, userId);
            result.putExtra(Constants.SIMPRINTS_MODULE_ID, moduleId);
            return result;
        }
    };

    @Rule
    public RuleChain chain = RuleChain
            .outerRule(launchActivityActivityTestRuleEnrol)
            .around(launchActivityActivityTestRuleIdentify)
            .around(launchActivityActivityTestRuleVerify);

    @Before
    public void setUp() {
        Realm.init(launchActivityActivityTestRuleEnrol.getActivity());
        super.setRealmConfiguration(RealmConfig.get(apiKey));
        super.setUp();
        Log.d("EndToEndTests", "bucket01.HappyWorkflow.setUp()");
        final LaunchActivity activity = launchActivityActivityTestRuleEnrol.getActivity();
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
     * Go through the full enrol workflow.
     * Go through the full identify workflow.
     * Go through the full verify workflow.
     */
    @Test
    public void happyWorkflow() {
        Log.d("EndToEndTests", "bucket01.HappyWorkflow.happyWorkflow");
        Log.d("EndToEndTests", "bucket01.HappyWorkflow.happyWorkflow enrol patient start");
        onView(withId(R.id.tv_consent_text))
                .check(matches(isDisplayed()))
                .check(matches(withText(R.string.short_consent)));

        sleep(12, TimeUnit.SECONDS);

        onView(withId(R.id.confirm_consent_text_view))
                .check(matches(isDisplayed()))
                .check(matches(withText(R.string.confirm_consent)))
                .perform(click());

        sleep(2, TimeUnit.SECONDS);

        onView(withId(R.id.scan_button))
                .check(matches(isDisplayed()))
                .check(matches(withText(R.string.scan)))
                .perform(click());

        sleep(8, TimeUnit.SECONDS);

        onView(withId(R.id.scan_button))
                .check(matches(isDisplayed()))
                .check(matches(withText(R.string.scan)))
                .perform(click());

        sleep(8, TimeUnit.SECONDS);

        onView(withId(R.id.action_forward))
                .check(matches(isDisplayed()))
                .perform(click());

        Log.d("EndToEndTests", "bucket01.HappyWorkflow.happyWorkflow enrol patient start");
        SystemClock.sleep(5000);
        // Check that a patient was indeed saved to the database
    }

    @After
    public void tearDown() {
        super.tearDown();
        Log.d("EndToEndTests", "bucket01.HappyWorkflow.tearDown()");
    }
}
