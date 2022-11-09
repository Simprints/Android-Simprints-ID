package com.simprints.fingerprint.activities.alert

import android.app.Activity.RESULT_OK
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
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.alert.FingerprintAlert.*
import com.simprints.fingerprint.activities.alert.request.AlertTaskRequest
import com.simprints.fingerprint.activities.alert.result.AlertTaskResult
import com.simprints.fingerprint.activities.refusal.RefusalActivity
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.eventData.model.AlertScreenEvent
import com.simprints.fingerprint.orchestrator.domain.ResultCode
import io.mockk.mockk
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AlertActivityTest {

    private val sessionEventManagerMock: FingerprintSessionEventsManager = mockk(relaxed = true)

//    @get:Rule
//    val koinTestRule = KoinTestRule(modules = listOf(module {
//        single { sessionEventManagerMock }
//        single { mockk<FingerprintTimeHelper>(relaxed = true) }
//    }))

    @Before
    fun setUp() {
        Intents.init()
    }

    @Test
    fun noParamForAlertActivity_theRightAlertShouldAppear() {
        launchAlertActivity()
        ensureAlertScreenLaunched(UNEXPECTED_ERROR)

        verify { sessionEventManagerMock.addEventInBackground(any<AlertScreenEvent>()) }
    }

    @Test
    fun bluetoothNotEnabled_theRightAlertShouldAppear() {
        launchAlertActivity(AlertTaskRequest(BLUETOOTH_NOT_ENABLED))
        ensureAlertScreenLaunched(BLUETOOTH_NOT_ENABLED)
    }

    @Test
    fun bluetoothNotEnabled_userClicksOpenSettings_settingsShouldAppear() {
        launchAlertActivity(AlertTaskRequest(BLUETOOTH_NOT_ENABLED))
        mockBluetoothSettingsIntent()

        onView(withId(R.id.alertRightButton)).perform(click())

        intended(hasAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS))
    }

    @Test
    fun bluetoothNotEnabled_userClicksTryAgain_alertShouldFinishWithTheRightResult() {
        val scenario = launchAlertActivity(AlertTaskRequest(BLUETOOTH_NOT_ENABLED))
        ensureAlertScreenLaunched(BLUETOOTH_NOT_ENABLED)

        onView(withId(R.id.alertLeftButton)).perform(click())

        verifyIntentReturned(
            scenario.result,
            BLUETOOTH_NOT_ENABLED, AlertTaskResult.CloseButtonAction.TRY_AGAIN, ResultCode.OK
        )
    }

    @Test
    fun scannerNotPaired_userClicksPairScanner_bluetoothSettingsShouldAppear() {
        launchAlertActivity(AlertTaskRequest(NOT_PAIRED))
        ensureAlertScreenLaunched(NOT_PAIRED)
        mockBluetoothSettingsIntent()

        onView(withId(R.id.alertLeftButton)).perform(click())

        intended(hasAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS))
    }

    @Test
    fun unexpectedAlert_userClicksClose_alertShouldFinishWithTheRightResult() {
        val scenario = launchAlertActivity(AlertTaskRequest(UNEXPECTED_ERROR))
        ensureAlertScreenLaunched(UNEXPECTED_ERROR)

        onView(withId(R.id.alertLeftButton)).perform(click())

        verifyIntentReturned(
            scenario.result,
            UNEXPECTED_ERROR, AlertTaskResult.CloseButtonAction.CLOSE, ResultCode.ALERT
        )
    }

    @Test
    fun lowBattery_theRightAlertShouldAppear() {
        launchAlertActivity(AlertTaskRequest(LOW_BATTERY))
        ensureAlertScreenLaunched(LOW_BATTERY)
    }

    @Test
    fun bluetoothNotSupported_theRightAlertShouldAppear() {
        launchAlertActivity(AlertTaskRequest(BLUETOOTH_NOT_SUPPORTED))
        ensureAlertScreenLaunched(BLUETOOTH_NOT_SUPPORTED)
    }

    @Test
    fun disconnected_theRightAlertShouldAppear() {
        launchAlertActivity(AlertTaskRequest(DISCONNECTED))
        ensureAlertScreenLaunched(DISCONNECTED)
    }

    @Test
    fun notPaired_theRightAlertShouldAppear() {
        launchAlertActivity(AlertTaskRequest(NOT_PAIRED))
        ensureAlertScreenLaunched(NOT_PAIRED)
    }

    @Test
    fun multiPairedScanners_theRightAlertShouldAppear() {
        launchAlertActivity(AlertTaskRequest(MULTIPLE_PAIRED_SCANNERS))
        ensureAlertScreenLaunched(MULTIPLE_PAIRED_SCANNERS)
    }

    @Test
    fun pressBackButtonOnBluetoothError_shouldStartRefusalActivity() {
        val context: Context = ApplicationProvider.getApplicationContext()
        launchAlertActivity(AlertTaskRequest(BLUETOOTH_NOT_ENABLED))
        Espresso.pressBackUnconditionally()

        intended(hasComponent(ComponentName(context, RefusalActivity::class.java)))
    }

    @Test
    fun pressBackButtonOnLowBatteryScannerError_shouldStartRefusalActivity() {
        val context: Context = ApplicationProvider.getApplicationContext()
        launchAlertActivity(AlertTaskRequest(LOW_BATTERY))
        Espresso.pressBackUnconditionally()

        intended(hasComponent(ComponentName(context, RefusalActivity::class.java)))
    }

    @Test
    fun pressBackButtonOnNonBluetoothError_shouldFinish() {
        val scenario = launchAlertActivity(AlertTaskRequest(UNEXPECTED_ERROR))
        Espresso.pressBackUnconditionally()

        verifyIntentReturned(
            scenario.result,
            UNEXPECTED_ERROR,
            AlertTaskResult.CloseButtonAction.BACK,
            ResultCode.ALERT
        )
    }

    private fun launchAlertActivity(request: AlertTaskRequest? = null): ActivityScenario<AlertActivity> =
        ActivityScenario.launch(Intent().apply {
            setClassName(
                InstrumentationRegistry.getInstrumentation().targetContext.applicationContext,
                AlertActivity::class.qualifiedName!!
            )
            request?.let {
                putExtra(AlertTaskRequest.BUNDLE_KEY, request)
            }
        })


    private fun ensureAlertScreenLaunched(alert: FingerprintAlert) {
       val alertActivityViewModel= AlertError.fromAlertToAlertError(alert)

        onView(withId(R.id.message))
            .check(matches(withText(alertActivityViewModel.message)))
    }

    private fun verifyIntentReturned(
        result: Instrumentation.ActivityResult,
        fingerprintAlert: FingerprintAlert,
        buttonAction: AlertTaskResult.CloseButtonAction,
        expectedResultCode: ResultCode
    ) {
        Truth.assertThat(result.resultCode).isEqualTo(expectedResultCode.value)

        result.resultData.setExtrasClassLoader(AlertTaskResult::class.java.classLoader)
        val response =
            result.resultData.getParcelableExtra<AlertTaskResult>(AlertTaskResult.BUNDLE_KEY)
        Truth.assertThat(response).isEqualTo(AlertTaskResult(fingerprintAlert, buttonAction))
    }

    private fun mockBluetoothSettingsIntent() {
        intending(hasAction("android.settings.BLUETOOTH_SETTINGS")).respondWith(
            Instrumentation.ActivityResult(
                RESULT_OK,
                Intent()
            )
        )
    }

    @After
    fun tearDown() {
        Intents.release()
    }
}
