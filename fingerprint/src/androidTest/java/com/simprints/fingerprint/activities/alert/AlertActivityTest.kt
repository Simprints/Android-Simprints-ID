package com.simprints.fingerprint.activities.alert

import android.app.Activity
import android.app.Instrumentation
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.any
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.alert.FingerprintAlert.*
import com.simprints.fingerprint.activities.alert.request.AlertActRequest
import com.simprints.fingerprint.activities.alert.response.AlertActResult
import com.simprints.fingerprint.activities.refusal.RefusalActivity
import com.simprints.fingerprint.commontesttools.di.TestFingerprintCoreModule
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.eventData.model.AlertScreenEvent
import com.simprints.fingerprint.testtools.AndroidTestConfig
import com.simprints.id.Application
import com.simprints.testtools.common.di.DependencyRule
import com.simprints.testtools.common.syntax.verifyOnce
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject
import com.simprints.id.R as idR


@RunWith(AndroidJUnit4::class)
class AlertActivityTest {

    @Inject lateinit var sessionEventManagerMock: FingerprintSessionEventsManager

    private val fingerprintModule by lazy {
        TestFingerprintCoreModule(
            fingerprintSessionEventsManagerRule = DependencyRule.MockRule)
    }



    @Before
    fun setUp() {
        AndroidTestConfig(this, fingerprintCoreModule = fingerprintModule).fullSetup()
        Intents.init()
    }

    @Test
    fun noParamForAlertActivity_theRightAlertShouldAppear() {
        launchAlertActivity()
        ensureAlertScreenLaunched(AlertActivityViewModel.UNEXPECTED_ERROR)

        verifyOnce(sessionEventManagerMock) { addEventInBackground(any<AlertScreenEvent>()) }
    }

    @Test
    fun bluetoothNotEnabled_theRightAlertShouldAppear() {
        launchAlertActivity(AlertActRequest(BLUETOOTH_NOT_ENABLED))
        ensureAlertScreenLaunched(AlertActivityViewModel.BLUETOOTH_NOT_ENABLED)
    }

    @Test
    fun bluetoothNotEnabled_userClicksOpenSettings_settingsShouldAppear() {
        val bluetoothSettingsAction = android.provider.Settings.ACTION_BLUETOOTH_SETTINGS
        val bluetoothSettingsIntent = Intent(bluetoothSettingsAction)
        val app = ApplicationProvider.getApplicationContext<Application>()
        val activities = app.packageManager.queryIntentActivities(bluetoothSettingsIntent, 0)

        //In some emulators ACTION_BLUETOOTH_SETTINGS may be missing
        if (activities.size > 0) {
            launchAlertActivity(AlertActRequest(BLUETOOTH_NOT_ENABLED))

            onView(withId(R.id.alertRightButton)).perform(click())

            intended(hasAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS))
        }
    }

    @Test
    fun bluetoothNotEnabled_userClicksTryAgain_alertShouldFinishWithTheRightResult() {
        val scenario = launchAlertActivity(AlertActRequest(BLUETOOTH_NOT_ENABLED))
        ensureAlertScreenLaunched(AlertActivityViewModel.BLUETOOTH_NOT_ENABLED)

        onView(withId(R.id.alertLeftButton)).perform(click())

        verifyIntentReturned(scenario.result,
            BLUETOOTH_NOT_ENABLED, AlertActResult.CloseButtonAction.TRY_AGAIN)
    }

    @Test
    fun unexpectedAlert_userClicksClose_alertShouldFinishWithTheRightResult() {
        val scenario = launchAlertActivity(AlertActRequest(UNEXPECTED_ERROR))
        ensureAlertScreenLaunched(AlertActivityViewModel.UNEXPECTED_ERROR)

        onView(withId(R.id.alertLeftButton)).perform(click())

        verifyIntentReturned(scenario.result,
            UNEXPECTED_ERROR, AlertActResult.CloseButtonAction.CLOSE)
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
        onView(withId(R.id.alertRightButton)).perform(click())

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

    @Test
    fun pressBackButtonOnBluetoothError_shouldStartRefusalActivity() {
        val context: Context = ApplicationProvider.getApplicationContext()
        launchAlertActivity(AlertActRequest(BLUETOOTH_NOT_ENABLED))
        Espresso.pressBackUnconditionally()

        intended(hasComponent(ComponentName(context, RefusalActivity::class.java)))
    }

    @Test
    fun pressBackButtonOnNonBluetoothError_shouldFinish () {
        val scenario = launchAlertActivity(AlertActRequest(GUID_NOT_FOUND_ONLINE))
        Espresso.pressBackUnconditionally()

        verifyIntentReturned(scenario.result, GUID_NOT_FOUND_ONLINE, AlertActResult.CloseButtonAction.BACK)
    }

    private fun launchAlertActivity(request: AlertActRequest? = null): ActivityScenario<AlertActivity> =
        ActivityScenario.launch<AlertActivity>(Intent().apply {
            setClassName(ApplicationProvider.getApplicationContext<Application>().packageName, AlertActivity::class.qualifiedName!!)
            request?.let {
                putExtra(AlertActRequest.BUNDLE_KEY, request)
            }
        })


    private fun ensureAlertScreenLaunched(alertActivityViewModel: AlertActivityViewModel) {
        onView(withId(R.id.alertTitle))
            .check(matches(withText(alertActivityViewModel.title)))

        onView(withId(R.id.message))
            .check(matches(withText(alertActivityViewModel.message)))
    }

    private fun verifyIntentReturned(result: Instrumentation.ActivityResult,
                                     fingerprintAlert: FingerprintAlert,
                                     buttonAction: AlertActResult.CloseButtonAction) {
        Truth.assertThat(result.resultCode).isEqualTo(Activity.RESULT_OK)

        result.resultData.setExtrasClassLoader(AlertActResult::class.java.classLoader)
        val response = result.resultData.getParcelableExtra<AlertActResult>(AlertActResult.BUNDLE_KEY)
        Truth.assertThat(response).isEqualTo(AlertActResult(fingerprintAlert, buttonAction))
    }

    @After
    fun tearDown() {
        Intents.release()
    }
}
