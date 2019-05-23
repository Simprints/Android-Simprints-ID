package com.simprints.fingerprint.activities.alert

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.alert.FingerprintAlert.*
import com.simprints.fingerprint.activities.alert.request.AlertActRequest
import com.simprints.fingerprint.activities.alert.response.AlertActResponse
import com.simprints.fingerprint.testtools.AndroidTestConfig
import com.simprints.id.Application
import com.simprints.testtools.android.hasImage
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import com.simprints.id.R as idR

@RunWith(AndroidJUnit4::class)
class AlertActivityTest {

    @Before
    fun setUp() {
        AndroidTestConfig(this).fullSetup()
        Intents.init()
    }

    @Test
    fun noParamForAlertActivity_theRightAlertShouldAppear() {
        launchAlertActivity()
        ensureAlertScreenLaunched(AlertActivityViewModel.UNEXPECTED_ERROR)
    }

    @Test
    fun bluetoothNotEnabled_theRightAlertShouldAppear() {
        launchAlertActivity(AlertActRequest(BLUETOOTH_NOT_ENABLED))
        ensureAlertScreenLaunched(AlertActivityViewModel.BLUETOOTH_NOT_ENABLED)
    }

    @Test
    fun bluetoothNotEnabled_userClicksOpenSettings_settingsShouldAppear() {
        launchAlertActivity(AlertActRequest(BLUETOOTH_NOT_ENABLED))

        onView(withId(R.id.right_button)).perform(click())

        intended(hasAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS))
    }

    @Test
    fun bluetoothNotEnabled_userClicksTryAgain_alertShouldFinishWithTheRightResult() {
        val scenario = launchAlertActivity(AlertActRequest(BLUETOOTH_NOT_ENABLED))
        ensureAlertScreenLaunched(AlertActivityViewModel.BLUETOOTH_NOT_ENABLED)

        onView(withId(R.id.left_button)).perform(click())

        verifyIntentReturned(scenario.result,
            BLUETOOTH_NOT_ENABLED, AlertActResponse.CloseButtonAction.TRY_AGAIN)
    }

    @Test
    @SmallTest
    fun unexpectedAlert_userClicksClose_alertShouldFinishWithTheRightResult() {
        val scenario = launchAlertActivity(AlertActRequest(UNEXPECTED_ERROR))
        ensureAlertScreenLaunched(AlertActivityViewModel.UNEXPECTED_ERROR)

        onView(withId(R.id.left_button)).perform(click())

        verifyIntentReturned(scenario.result,
            UNEXPECTED_ERROR, AlertActResponse.CloseButtonAction.CLOSE)
    }

    @Test
    fun lowBattery_theRightAlertShouldAppear() {
        launchAlertActivity(AlertActRequest(LOW_BATTERY))
        ensureAlertScreenLaunched(AlertActivityViewModel.LOW_BATTERY)
    }

    @Test
    fun guidNotFoundOffline_theRightAlertShouldAppear() {
        launchAlertActivity(AlertActRequest(GUID_NOT_FOUND_OFFLINE))
        ensureAlertScreenLaunched(AlertActivityViewModel.GUID_NOT_FOUND_OFFLINE)
    }

    @Test
    fun guidNotFoundOffline_userClicksOpenSettings_settingsShouldAppear() {
        launchAlertActivity(AlertActRequest(GUID_NOT_FOUND_OFFLINE))
        onView(withId(R.id.right_button)).perform(click())

        intended(hasAction(android.provider.Settings.ACTION_WIFI_SETTINGS))
    }

    @Test
    fun bluetoothNotSupported_theRightAlertShouldAppear() {
        launchAlertActivity(AlertActRequest(BLUETOOTH_NOT_SUPPORTED))
        ensureAlertScreenLaunched(AlertActivityViewModel.BLUETOOTH_NOT_SUPPORTED)
    }

    @Test
    fun disconnected_theRightAlertShouldAppear() {
        launchAlertActivity(AlertActRequest(DISCONNECTED))
        ensureAlertScreenLaunched(AlertActivityViewModel.DISCONNECTED)
    }

    @Test
    fun notPaired_theRightAlertShouldAppear() {
        launchAlertActivity(AlertActRequest(NOT_PAIRED))
        ensureAlertScreenLaunched(AlertActivityViewModel.NOT_PAIRED)
    }

    @Test
    fun multiPairedScanners_theRightAlertShouldAppear() {
        launchAlertActivity(AlertActRequest(MULTIPLE_PAIRED_SCANNERS))
        ensureAlertScreenLaunched(AlertActivityViewModel.MULTIPLE_PAIRED_SCANNERS)
    }

    private fun launchAlertActivity(request: AlertActRequest? = null): ActivityScenario<AlertActivity> =
        ActivityScenario.launch<AlertActivity>(Intent().apply {
            setClassName(ApplicationProvider.getApplicationContext<Application>().packageName, AlertActivity::class.qualifiedName!!)
            request?.let {
                putExtra(AlertActRequest.BUNDLE_KEY, request)
            }
        })


    private fun ensureAlertScreenLaunched(alertActivityViewModel: AlertActivityViewModel) {
        onView(withId(R.id.alert_title))
            .check(matches(withText(alertActivityViewModel.title)))

        onView(withId(R.id.message))
            .check(matches(withText(alertActivityViewModel.message)))

        onView(withId(R.id.alert_image)).check(matches(hasImage(alertActivityViewModel.mainDrawable)))
    }

    private fun verifyIntentReturned(result: Instrumentation.ActivityResult,
                                     fingerprintAlert: FingerprintAlert,
                                     buttonAction: AlertActResponse.CloseButtonAction) {
        Truth.assertThat(result.resultCode).isEqualTo(Activity.RESULT_OK)

        result.resultData.setExtrasClassLoader(AlertActResponse::class.java.classLoader)
        val response = result.resultData.getParcelableExtra<AlertActResponse>(AlertActResponse.BUNDLE_KEY)
        Truth.assertThat(response).isEqualTo(AlertActResponse(fingerprintAlert, buttonAction))
    }

    @After
    fun tearDown() {
        Intents.release()
    }
}
