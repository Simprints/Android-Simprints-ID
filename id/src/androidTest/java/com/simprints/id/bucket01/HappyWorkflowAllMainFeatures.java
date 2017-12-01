package com.simprints.id.bucket01;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;
import android.view.WindowManager;

import com.google.gson.JsonObject;
import com.simprints.id.FirstUseTest;
import com.simprints.id.R;
import com.simprints.id.RemoteAdminUtils;
import com.simprints.id.activities.LaunchActivity;
import com.simprints.libdata.models.realm.RealmConfig;
import com.simprints.libsimprints.Constants;
import com.simprints.libsimprints.Identification;
import com.simprints.libsimprints.Registration;
import com.simprints.libsimprints.Tier;
import com.simprints.libsimprints.Verification;
import com.simprints.remoteadminclient.ApiException;
import com.simprints.remoteadminclient.api.DefaultApi;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.schibsted.spain.barista.BaristaSleepActions.sleep;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class HappyWorkflowAllMainFeatures extends FirstUseTest {
    private static final String apiKey = "00000000-0000-0000-0000-000000000000";
    private static final String userId = "the_lone_user";
    private static final String moduleId = "the_one_and_only_module";

    @Rule
    public ActivityTestRule<LaunchActivity> launchActivityActivityTestRuleEnrol =
            new ActivityTestRule<>(LaunchActivity.class, false, false);

    @Rule
    public ActivityTestRule<LaunchActivity> launchActivityActivityTestRuleIdentify =
            new ActivityTestRule<>(LaunchActivity.class, false, false);

    @Rule
    public ActivityTestRule<LaunchActivity> launchActivityActivityTestRuleVerify =
            new ActivityTestRule<>(LaunchActivity.class, false, false);

    @Before
    public void setUp() throws ApiException {
        Log.d("EndToEndTests", "bucket01.HappyWorkflowAllMainFeatures.setUp()");
        Realm.init(getInstrumentation().getTargetContext());
        super.setRealmConfiguration(RealmConfig.get(apiKey));
        super.setApiKey(apiKey);
        super.setUp();
    }

    private Intent createLaunchActivityIntent(String action) {
        Context targetContext = getInstrumentation().getTargetContext();
        Intent intent = new Intent(targetContext, LaunchActivity.class);
        intent.setAction(action);
        intent.putExtra(Constants.SIMPRINTS_API_KEY, apiKey);
        intent.putExtra(Constants.SIMPRINTS_USER_ID, userId);
        intent.putExtra(Constants.SIMPRINTS_MODULE_ID, moduleId);
        return intent;
    }

    private void runActivityOnUiThread(ActivityTestRule activityTestRule) {
        final Activity activity = activityTestRule.getActivity();
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
    public void happyWorkflowAllMainFeatures() {
        Log.d("EndToEndTests", "bucket01.HappyWorkflowAllMainFeatures.happyWorkflowAllMainFeatures");
        Log.d("EndToEndTests", "bucket01.HappyWorkflowAllMainFeatures.happyWorkflowAllMainFeatures enrol patient start");

        Intent registerIntent = createLaunchActivityIntent(Constants.SIMPRINTS_REGISTER_INTENT);
        launchActivityActivityTestRuleEnrol.launchActivity(registerIntent);
        runActivityOnUiThread(launchActivityActivityTestRuleEnrol);

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

        Registration registration = launchActivityActivityTestRuleEnrol.getActivityResult()
                .getResultData().getParcelableExtra(Constants.SIMPRINTS_REGISTRATION);
        String guid = registration.getGuid();
        assertNotNull(guid);

        Log.d("EndToEndTests", "bucket01.HappyWorkflowAllMainFeatures.happyWorkflowAllMainFeatures enrol patient end");

        SystemClock.sleep(2000);

        DefaultApi apiInstance = RemoteAdminUtils.INSTANCE.getConfiguredApiInstance();
        try {
            // Check to see if the patient made it to the database
            JsonObject patientsJson = RemoteAdminUtils.getPatientsNode(apiInstance, apiKey);
            assertNotNull(patientsJson);
            assertEquals(1, patientsJson.size());
            assertTrue(patientsJson.has(guid));

        } catch (ApiException e) {
            assertNull("ApiException", e);
        }

        SystemClock.sleep(2000);

        Log.d("EndToEndTests", "bucket01.HappyWorkflowAllMainFeatures.happyWorkflowAllMainFeatures identify patient start");

        Intent identifyIntent = createLaunchActivityIntent(Constants.SIMPRINTS_IDENTIFY_INTENT);
        launchActivityActivityTestRuleIdentify.launchActivity(identifyIntent);
        runActivityOnUiThread(launchActivityActivityTestRuleIdentify);

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

        sleep(8, TimeUnit.SECONDS);

        ArrayList<Identification> identifications = launchActivityActivityTestRuleIdentify.getActivityResult()
                .getResultData().getParcelableArrayListExtra(Constants.SIMPRINTS_IDENTIFICATIONS);
        assertEquals(1, identifications.size());
        assertEquals(guid, identifications.get(0).getGuid());
        assertTrue(identifications.get(0).getConfidence() > 0);
        assertNotEquals(Tier.TIER_5, identifications.get(0).getTier());

        Log.d("EndToEndTests", "bucket01.HappyWorkflowAllMainFeatures.happyWorkflowAllMainFeatures identify patient end");

        SystemClock.sleep(2000);

        Log.d("EndToEndTests", "bucket01.HappyWorkflowAllMainFeatures.happyWorkflowAllMainFeatures verify patient start");

        Intent verifyIntent = createLaunchActivityIntent(Constants.SIMPRINTS_VERIFY_INTENT);
        verifyIntent.putExtra(Constants.SIMPRINTS_VERIFY_GUID, guid);
        launchActivityActivityTestRuleVerify.launchActivity(verifyIntent);
        runActivityOnUiThread(launchActivityActivityTestRuleVerify);

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

        Verification verification = launchActivityActivityTestRuleVerify.getActivityResult()
                .getResultData().getParcelableExtra(Constants.SIMPRINTS_VERIFICATION);
        assertEquals(guid, verification.getGuid());
        assertTrue(verification.getConfidence() > 0);
        assertNotEquals(Tier.TIER_5, verification.getTier());

        Log.d("EndToEndTests", "bucket01.HappyWorkflowAllMainFeatures.happyWorkflowAllMainFeatures verify patient end");

        SystemClock.sleep(2000);
    }

    @After
    public void tearDown() {
        super.tearDown();
        Log.d("EndToEndTests", "bucket01.HappyWorkflowAllMainFeatures.tearDown()");
    }
}
