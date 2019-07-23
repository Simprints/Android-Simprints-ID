package com.simprints.fingerprint.activities.alert
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
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.any
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.alert.FingerprintAlert.*
import com.simprints.fingerprint.activities.alert.request.AlertTaskRequest
import com.simprints.fingerprint.activities.alert.result.AlertTaskResult
import com.simprints.fingerprint.activities.refusal.RefusalActivity
import com.simprints.fingerprint.commontesttools.di.TestFingerprintCoreModule
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.eventData.model.AlertScreenEvent
import com.simprints.fingerprint.orchestrator.task.ResultCode
import com.simprints.fingerprint.testtools.AndroidTestConfig
import com.simprints.id.Application
import com.simprints.testtools.common.di.DependencyRule
import com.simprints.testtools.common.syntax.verifyOnce
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@SmallTest
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
        launchAlertActivity(AlertTaskRequest(BLUETOOTH_NOT_ENABLED))
        ensureAlertScreenLaunched(AlertActivityViewModel.BLUETOOTH_NOT_ENABLED)
    }

    @Test
    fun bluetoothNotEnabled_userClicksOpenSettings_settingsShouldAppear() {
        launchAlertActivity(AlertActRequest(BLUETOOTH_NOT_ENABLED))
        mockBluetoothSettingsIntent()

        //In some emulators ACTION_BLUETOOTH_SETTINGS may be missing
        if (activities.size > 0) {
            launchAlertActivity(AlertActRequest(BLUETOOTH_NOT_ENABLED))

        intended(hasAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS))
    }

    @Test
    fun bluetoothNotEnabled_userClicksTryAgain_alertShouldFinishWithTheRightResult() {
        val scenario = launchAlertActivity(AlertTaskRequest(BLUETOOTH_NOT_ENABLED))
        ensureAlertScreenLaunched(AlertActivityViewModel.BLUETOOTH_NOT_ENABLED)

        onView(withId(R.id.alertLeftButton)).perform(click())

        verifyIntentReturned(scenario.result,
            BLUETOOTH_NOT_ENABLED, AlertTaskResult.CloseButtonAction.TRY_AGAIN, ResultCode.OK)
    }

    @Test
    fun scannerNotPaired_userClicksPairScanner_bluetoothSettingsShouldAppear() {
        launchAlertActivity(AlertTaskRequest(NOT_PAIRED))
        ensureAlertScreenLaunched(AlertActivityViewModel.NOT_PAIRED)
        mockBluetoothSettingsIntent()

        onView(withId(R.id.alertLeftButton)).perform(click())

        intended(hasAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS))
    }

    @Test
    fun unexpectedAlert_userClicksClose_alertShouldFinishWithTheRightResult() {
        val scenario = launchAlertActivity(AlertTaskRequest(UNEXPECTED_ERROR))
        ensureAlertScreenLaunched(AlertActivityViewModel.UNEXPECTED_ERROR)

        onView(withId(R.id.alertLeftButton)).perform(click())

        verifyIntentReturned(scenario.result,
            UNEXPECTED_ERROR, AlertTaskResult.CloseButtonAction.CLOSE, ResultCode.ALERT)
    }

    @Test
    fun lowBattery_theRightAlertShouldAppear() {
        launchAlertActivity(AlertTaskRequest(LOW_BATTERY))
        ensureAlertScreenLaunched(AlertActivityViewModel.LOW_BATTERY)
    }

    @Test
    fun guidNotFoundOffline_theRightAlertShouldAppear() {
        launchAlertActivity(AlertTaskRequest(GUID_NOT_FOUND_OFFLINE))
        ensureAlertScreenLaunched(AlertActivityViewModel.GUID_NOT_FOUND_OFFLINE)
    }

    @Test
    fun guidNotFoundOffline_userClicksOpenSettings_settingsShouldAppear() {
        launchAlertActivity(AlertTaskRequest(GUID_NOT_FOUND_OFFLINE))
        onView(withId(R.id.alertRightButton)).perform(click())

        intended(hasAction(android.provider.Settings.ACTION_WIFI_SETTINGS))
    }

    @Test
    fun bluetoothNotSupported_theRightAlertShouldAppear() {
        launchAlertActivity(AlertTaskRequest(BLUETOOTH_NOT_SUPPORTED))
        ensureAlertScreenLaunched(AlertActivityViewModel.BLUETOOTH_NOT_SUPPORTED)
    }

    @Test
    fun disconnected_theRightAlertShouldAppear() {
        launchAlertActivity(AlertTaskRequest(DISCONNECTED))
        ensureAlertScreenLaunched(AlertActivityViewModel.DISCONNECTED)
    }

    @Test
    fun notPaired_theRightAlertShouldAppear() {
        launchAlertActivity(AlertTaskRequest(NOT_PAIRED))
        ensureAlertScreenLaunched(AlertActivityViewModel.NOT_PAIRED)
    }

    @Test
    fun multiPairedScanners_theRightAlertShouldAppear() {
        launchAlertActivity(AlertTaskRequest(MULTIPLE_PAIRED_SCANNERS))
        ensureAlertScreenLaunched(AlertActivityViewModel.MULTIPLE_PAIRED_SCANNERS)
    }

    @Test
    fun pressBackButtonOnBluetoothError_shouldStartRefusalActivity() {
        val context: Context = ApplicationProvider.getApplicationContext()
        launchAlertActivity(AlertTaskRequest(BLUETOOTH_NOT_ENABLED))
        Espresso.pressBackUnconditionally()

        intended(hasComponent(ComponentName(context, RefusalActivity::class.java)))
    }

    @Test
    fun pressBackButtonOnNonBluetoothError_shouldFinish () {
        val scenario = launchAlertActivity(AlertTaskRequest(GUID_NOT_FOUND_ONLINE))
        Espresso.pressBackUnconditionally()

        verifyIntentReturned(scenario.result, GUID_NOT_FOUND_ONLINE, AlertTaskResult.CloseButtonAction.BACK, ResultCode.ALERT)
    }

    private fun launchAlertActivity(request: AlertTaskRequest? = null): ActivityScenario<AlertActivity> =
        ActivityScenario.launch(Intent().apply {
            setClassName(ApplicationProvider.getApplicationContext<Application>().packageName, AlertActivity::class.qualifiedName!!)
            request?.let {
                putExtra(AlertTaskRequest.BUNDLE_KEY, request)
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
                                     buttonAction: AlertTaskResult.CloseButtonAction,
                                     expectedResultCode: ResultCode) {
        Truth.assertThat(result.resultCode).isEqualTo(expectedResultCode.value)

        result.resultData.setExtrasClassLoader(AlertTaskResult::class.java.classLoader)
        val response = result.resultData.getParcelableExtra<AlertTaskResult>(AlertTaskResult.BUNDLE_KEY)
        Truth.assertThat(response).isEqualTo(AlertTaskResult(fingerprintAlert, buttonAction))
    }

    private fun mockBluetoothSettingsIntent() {
        intending(hasAction("android.settings.BLUETOOTH_SETTINGS")).respondWith(Instrumentation.ActivityResult(RESULT_OK, Intent()))
    }

    @After
    fun tearDown() {
        Intents.release()
    }
}
