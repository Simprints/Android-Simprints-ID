package com.simprints.id.activities.alert

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.provider.Settings
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBackUnconditionally
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.alert.request.AlertActRequest
import com.simprints.id.activities.alert.response.AlertActResponse
import com.simprints.id.activities.alert.response.AlertActResponse.ButtonAction
import com.simprints.id.activities.fingerprintexitform.FingerprintExitFormActivity
import com.simprints.id.commontesttools.di.TestAppModule
import com.simprints.id.commontesttools.di.TestPreferencesModule
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventdata.models.domain.events.AlertScreenEvent
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.alert.AlertActivityViewModel
import com.simprints.id.domain.alert.AlertType
import com.simprints.id.domain.modality.Modality
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.testtools.android.hasImage
import com.simprints.testtools.common.di.DependencyRule
import com.simprints.testtools.common.syntax.verifyOnce
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import com.simprints.testtools.unit.robolectric.assertActivityStarted
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class AlertActivityTest {

    private val app = ApplicationProvider.getApplicationContext<Application>()

    @Inject lateinit var sessionEventManagerMock: SessionEventsManager
    @Inject lateinit var preferencesManagerSpy: PreferencesManager

    private val preferencesModule by lazy {
        TestPreferencesModule(
            settingsPreferencesManagerRule = DependencyRule.SpyRule
        )
    }

    private val module by lazy {
        TestAppModule(
            app,
            sessionEventsManagerRule = DependencyRule.MockRule,
            crashReportManagerRule = DependencyRule.MockRule)
    }

    @Before
    fun setUp() {
        UnitTestConfig(this, module, preferencesModule).fullSetup()
        Intents.init()
    }

    @Test
    fun noParamForAlertActivity_theRightAlertShouldAppear() {
        launchAlertActivity()
        ensureAlertScreenLaunched(AlertActivityViewModel.UNEXPECTED_ERROR)

        verifyOnce(sessionEventManagerMock) { addEventInBackground(any<AlertScreenEvent>()) }
    }

    @Test
    fun differentProjectId_theRightAlertShouldAppear() {
        launchAlertActivity(AlertActRequest(AlertType.DIFFERENT_PROJECT_ID_SIGNED_IN))
        ensureAlertScreenLaunched(AlertActivityViewModel.DIFFERENT_PROJECT_ID)
    }

    @Test
    fun differentUserId_theRightAlertShouldAppear() {
        launchAlertActivity(AlertActRequest(AlertType.DIFFERENT_USER_ID_SIGNED_IN))
        ensureAlertScreenLaunched(AlertActivityViewModel.DIFFERENT_USER_ID)
    }

    @Test
    fun differentUnexpected_theRightAlertShouldAppear() {
        launchAlertActivity(AlertActRequest(AlertType.UNEXPECTED_ERROR))
        ensureAlertScreenLaunched(AlertActivityViewModel.UNEXPECTED_ERROR)
    }

    @Test
    fun safetyNetError_theRightAlertShouldAppear() {
        launchAlertActivity(AlertActRequest(AlertType.SAFETYNET_ERROR))
        ensureAlertScreenLaunched(AlertActivityViewModel.SAFETYNET_ERROR)
    }

    @Test
    fun safetyNetDown_userClicksClose_alertShouldFinishWithRightResult() {
        val scenario = launchAlertActivity(AlertActRequest(AlertType.SAFETYNET_ERROR))
        ensureAlertScreenLaunched(AlertActivityViewModel.SAFETYNET_ERROR)

        onView(withId(R.id.alertLeftButton)).perform(click())

        verifyIntentReturned(scenario.result, AlertType.SAFETYNET_ERROR, ButtonAction.CLOSE)
    }


    @Test
    fun unexpectedAlert_userClicksClose_alertShouldFinishWithTheRightResult() {
        val scenario = launchAlertActivity(AlertActRequest(AlertType.UNEXPECTED_ERROR))
        ensureAlertScreenLaunched(AlertActivityViewModel.UNEXPECTED_ERROR)

        onView(withId(R.id.alertLeftButton)).perform(click())

        verifyIntentReturned(scenario.result, AlertType.UNEXPECTED_ERROR, ButtonAction.CLOSE)
    }

    @Test
    fun guidNotFoundOnlineAlert_userClickClose_shouldFinishWithTheRightResult() {
        val scenario = launchAlertActivity(AlertActRequest(AlertType.GUID_NOT_FOUND_ONLINE))
        ensureAlertScreenLaunched(AlertActivityViewModel.GUID_NOT_FOUND_ONLINE)

        onView(withId(R.id.alertLeftButton)).perform(click())

        verifyIntentReturned(scenario.result, AlertType.GUID_NOT_FOUND_ONLINE, ButtonAction.CLOSE)
    }

    @Test
    fun guidNotFoundOffline_userClicksPhoneSettings_shouldOpenWifiSettings() {
        launchAlertActivity(AlertActRequest(AlertType.GUID_NOT_FOUND_OFFLINE))
        ensureAlertScreenLaunched(AlertActivityViewModel.GUID_NOT_FOUND_OFFLINE)

        onView(withId(R.id.alertRightButton)).perform(click())

        Intents.intended(IntentMatchers.hasAction(Settings.ACTION_WIFI_SETTINGS))
    }

    @Test
    fun guidNotFoundOffline_userClickTryAgain_shouldFinishWithRightResult() {
        val scenario = launchAlertActivity(AlertActRequest(AlertType.GUID_NOT_FOUND_OFFLINE))
        ensureAlertScreenLaunched(AlertActivityViewModel.GUID_NOT_FOUND_OFFLINE)

        onView(withId(R.id.alertLeftButton)).perform(click())

        verifyIntentReturned(scenario.result, AlertType.GUID_NOT_FOUND_OFFLINE, ButtonAction.TRY_AGAIN)
    }

    @Test
    fun guidNotFoundOffline_userClicksBack_shouldStartExitForm() {
        whenever(preferencesManagerSpy.modalities).thenReturn(listOf(Modality.FINGER))

        val scenario = launchAlertActivity(AlertActRequest(AlertType.GUID_NOT_FOUND_OFFLINE))
        ensureAlertScreenLaunched(AlertActivityViewModel.GUID_NOT_FOUND_OFFLINE)
        pressBackUnconditionally()

        scenario.onActivity {
            assertActivityStarted(FingerprintExitFormActivity::class.java, it)
        }
    }

    @Test
    fun withRootedDevice_theRightAlertShouldAppear() {
        launchAlertActivity(AlertActRequest(AlertType.ROOTED_DEVICE))
        ensureAlertScreenLaunched(AlertActivityViewModel.ROOTED_DEVICE)
    }

    @Test
    fun withRootedDevice_userClicksClose_shouldFinishWithTheRightResult() {
        val scenario = launchAlertActivity(AlertActRequest(AlertType.ROOTED_DEVICE))
        ensureAlertScreenLaunched(AlertActivityViewModel.ROOTED_DEVICE)

        onView(withId(R.id.alertLeftButton)).perform(click())

        verifyIntentReturned(scenario.result, AlertType.ROOTED_DEVICE, ButtonAction.CLOSE)
    }

    private fun launchAlertActivity(request: AlertActRequest? = null): ActivityScenario<AlertActivity> =
        ActivityScenario.launch(Intent().apply {
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

        onView(withId(R.id.alertImage)).check(matches(hasImage(alertActivityViewModel.mainDrawable)))
    }

    private fun verifyIntentReturned(result: Instrumentation.ActivityResult,
                                     alert: AlertType,
                                     buttonAction: ButtonAction) {
        Truth.assertThat(result.resultCode).isEqualTo(Activity.RESULT_OK)

        result.resultData.setExtrasClassLoader(AlertActResponse::class.java.classLoader)
        val response = result.resultData.getParcelableExtra<AlertActResponse>(AlertActResponse.BUNDLE_KEY)
        Truth.assertThat(response).isEqualTo(AlertActResponse(alert, buttonAction))
    }

    @After
    fun tearDown() {
        Intents.release()
    }
}

