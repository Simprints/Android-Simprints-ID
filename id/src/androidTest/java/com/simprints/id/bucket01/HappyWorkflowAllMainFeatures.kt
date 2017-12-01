package com.simprints.id.bucket01


import android.content.Intent
import android.os.SystemClock
import android.support.test.InstrumentationRegistry.getInstrumentation
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.util.Log
import android.view.WindowManager
import com.schibsted.spain.barista.BaristaSleepActions.sleep
import com.simprints.id.FirstUseTest
import com.simprints.id.R
import com.simprints.id.RemoteAdminUtils
import com.simprints.id.activities.LaunchActivity
import com.simprints.libdata.models.realm.RealmConfig
import com.simprints.libsimprints.*
import com.simprints.remoteadminclient.ApiException
import io.realm.Realm
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
@LargeTest
class HappyWorkflowAllMainFeatures : FirstUseTest() {

    private val apiKey = "00000000-0000-0000-0000-000000000000"
    private val userId = "the_lone_user"
    private val moduleId = "the_one_and_only_module"

    @Rule @JvmField
    var launchActivityActivityTestRuleEnrol = ActivityTestRule(LaunchActivity::class.java, false, false)

    @Rule @JvmField
    var launchActivityActivityTestRuleIdentify = ActivityTestRule(LaunchActivity::class.java, false, false)

    @Rule @JvmField
    var launchActivityActivityTestRuleVerify = ActivityTestRule(LaunchActivity::class.java, false, false)

    @Before
    @Throws(ApiException::class)
    override fun setUp() {
        Log.d("EndToEndTests", "bucket01.HappyWorkflowAllMainFeatures.setUp()")
        Realm.init(getInstrumentation().targetContext)
        super.setRealmConfiguration(RealmConfig.get(apiKey))
        super.setApiKey(apiKey)
        super.setUp()
    }

    private fun createLaunchActivityIntent(action: String): Intent {
        val targetContext = getInstrumentation().targetContext
        val intent = Intent(targetContext, LaunchActivity::class.java)
        intent.action = action
        intent.putExtra(Constants.SIMPRINTS_API_KEY, apiKey)
        intent.putExtra(Constants.SIMPRINTS_USER_ID, userId)
        intent.putExtra(Constants.SIMPRINTS_MODULE_ID, moduleId)
        return intent
    }

    private fun runActivityOnUiThread(activityTestRule: ActivityTestRule<*>) {
        val activity = activityTestRule.activity
        val wakeUpDevice = Runnable {
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        activity.runOnUiThread(wakeUpDevice)
    }

    /**
     * Go through the full enrol workflow.
     * Go through the full identify workflow.
     * Go through the full verify workflow.
     */
    @Test
    fun happyWorkflowAllMainFeatures() {
        Log.d("EndToEndTests", "bucket01.HappyWorkflowAllMainFeatures.happyWorkflowAllMainFeatures")
        Log.d("EndToEndTests", "bucket01.HappyWorkflowAllMainFeatures.happyWorkflowAllMainFeatures enrol patient start")

        val registerIntent = createLaunchActivityIntent(Constants.SIMPRINTS_REGISTER_INTENT)
        launchActivityActivityTestRuleEnrol.launchActivity(registerIntent)
        runActivityOnUiThread(launchActivityActivityTestRuleEnrol)

        onView(withId(R.id.tv_consent_text))
                .check(matches(isDisplayed()))
                .check(matches(withText(R.string.short_consent)))

        sleep(12, TimeUnit.SECONDS)

        onView(withId(R.id.confirm_consent_text_view))
                .check(matches(isDisplayed()))
                .check(matches(withText(R.string.confirm_consent)))
                .perform(click())

        sleep(2, TimeUnit.SECONDS)

        onView(withId(R.id.scan_button))
                .check(matches(isDisplayed()))
                .check(matches(withText(R.string.scan)))
                .perform(click())

        sleep(8, TimeUnit.SECONDS)

        onView(withId(R.id.scan_button))
                .check(matches(isDisplayed()))
                .check(matches(withText(R.string.scan)))
                .perform(click())

        sleep(8, TimeUnit.SECONDS)

        onView(withId(R.id.action_forward))
                .check(matches(isDisplayed()))
                .perform(click())

        val registration = launchActivityActivityTestRuleEnrol.activityResult
                .resultData.getParcelableExtra<Registration>(Constants.SIMPRINTS_REGISTRATION)
        val guid = registration.guid
        assertNotNull(guid)

        Log.d("EndToEndTests", "bucket01.HappyWorkflowAllMainFeatures.happyWorkflowAllMainFeatures enrol patient end")

        SystemClock.sleep(2000)

        val apiInstance = RemoteAdminUtils.configuredApiInstance
        try {
            // Check to see if the patient made it to the database
            val patientsJson = RemoteAdminUtils.getPatientsNode(apiInstance, apiKey)
            assertNotNull(patientsJson)
            assertEquals(1, patientsJson.size().toLong())
            assertTrue(patientsJson.has(guid))

        } catch (e: ApiException) {
            assertNull("ApiException", e)
        }

        SystemClock.sleep(2000)

        Log.d("EndToEndTests", "bucket01.HappyWorkflowAllMainFeatures.happyWorkflowAllMainFeatures identify patient start")

        val identifyIntent = createLaunchActivityIntent(Constants.SIMPRINTS_IDENTIFY_INTENT)
        launchActivityActivityTestRuleIdentify.launchActivity(identifyIntent)
        runActivityOnUiThread(launchActivityActivityTestRuleIdentify)

        onView(withId(R.id.tv_consent_text))
                .check(matches(isDisplayed()))
                .check(matches(withText(R.string.short_consent)))

        sleep(12, TimeUnit.SECONDS)

        onView(withId(R.id.confirm_consent_text_view))
                .check(matches(isDisplayed()))
                .check(matches(withText(R.string.confirm_consent)))
                .perform(click())

        sleep(2, TimeUnit.SECONDS)

        onView(withId(R.id.scan_button))
                .check(matches(isDisplayed()))
                .check(matches(withText(R.string.scan)))
                .perform(click())

        sleep(8, TimeUnit.SECONDS)

        onView(withId(R.id.scan_button))
                .check(matches(isDisplayed()))
                .check(matches(withText(R.string.scan)))
                .perform(click())

        sleep(8, TimeUnit.SECONDS)

        onView(withId(R.id.action_forward))
                .check(matches(isDisplayed()))
                .perform(click())

        sleep(8, TimeUnit.SECONDS)

        val identifications = launchActivityActivityTestRuleIdentify.activityResult
                .resultData.getParcelableArrayListExtra<Identification>(Constants.SIMPRINTS_IDENTIFICATIONS)
        assertEquals(1, identifications.size.toLong())
        assertEquals(guid, identifications[0].guid)
        assertTrue(identifications[0].confidence > 0)
        assertNotEquals(Tier.TIER_5, identifications[0].tier)

        Log.d("EndToEndTests", "bucket01.HappyWorkflowAllMainFeatures.happyWorkflowAllMainFeatures identify patient end")

        SystemClock.sleep(2000)

        Log.d("EndToEndTests", "bucket01.HappyWorkflowAllMainFeatures.happyWorkflowAllMainFeatures verify patient start")

        val verifyIntent = createLaunchActivityIntent(Constants.SIMPRINTS_VERIFY_INTENT)
        verifyIntent.putExtra(Constants.SIMPRINTS_VERIFY_GUID, guid)
        launchActivityActivityTestRuleVerify.launchActivity(verifyIntent)
        runActivityOnUiThread(launchActivityActivityTestRuleVerify)

        onView(withId(R.id.tv_consent_text))
                .check(matches(isDisplayed()))
                .check(matches(withText(R.string.short_consent)))

        sleep(12, TimeUnit.SECONDS)

        onView(withId(R.id.confirm_consent_text_view))
                .check(matches(isDisplayed()))
                .check(matches(withText(R.string.confirm_consent)))
                .perform(click())

        sleep(2, TimeUnit.SECONDS)

        onView(withId(R.id.scan_button))
                .check(matches(isDisplayed()))
                .check(matches(withText(R.string.scan)))
                .perform(click())

        sleep(8, TimeUnit.SECONDS)

        onView(withId(R.id.scan_button))
                .check(matches(isDisplayed()))
                .check(matches(withText(R.string.scan)))
                .perform(click())

        sleep(8, TimeUnit.SECONDS)

        onView(withId(R.id.action_forward))
                .check(matches(isDisplayed()))
                .perform(click())

        val verification = launchActivityActivityTestRuleVerify.activityResult
                .resultData.getParcelableExtra<Verification>(Constants.SIMPRINTS_VERIFICATION)
        assertEquals(guid, verification.guid)
        assertTrue(verification.confidence > 0)
        assertNotEquals(Tier.TIER_5, verification.tier)

        Log.d("EndToEndTests", "bucket01.HappyWorkflowAllMainFeatures.happyWorkflowAllMainFeatures verify patient end")

        SystemClock.sleep(2000)
    }

    @After
    override fun tearDown() {
        super.tearDown()
        Log.d("EndToEndTests", "bucket01.HappyWorkflowAllMainFeatures.tearDown()")
    }
}
