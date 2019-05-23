package com.simprints.id.activities.alert

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.nhaarman.mockito_kotlin.any
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.alert.request.AlertActRequest
import com.simprints.id.activities.alert.response.AlertActResponse
import com.simprints.id.commontesttools.di.TestAppModule
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventdata.models.domain.events.AlertScreenEvent
import com.simprints.id.domain.alert.AlertActivityViewModel
import com.simprints.id.domain.alert.AlertType
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.testtools.android.hasImage
import com.simprints.testtools.common.di.DependencyRule
import com.simprints.testtools.common.syntax.verifyOnce
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
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

    private val module by lazy {
        TestAppModule(
            app,
            sessionEventsManagerRule = DependencyRule.MockRule,
            crashReportManagerRule = DependencyRule.MockRule)
    }

    @Before
    fun setUp() {
        UnitTestConfig(this, module).fullSetup()
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
    fun unexpectedAlert_userClicksClose_alertShouldFinishWithTheRightResult() {
        val scenario = launchAlertActivity(AlertActRequest(AlertType.UNEXPECTED_ERROR))
        ensureAlertScreenLaunched(AlertActivityViewModel.UNEXPECTED_ERROR)

        onView(withId(R.id.left_button)).perform(click())

        verifyIntentReturned(scenario.result, AlertType.UNEXPECTED_ERROR)
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
                                     alert: AlertType) {
        Truth.assertThat(result.resultCode).isEqualTo(Activity.RESULT_OK)

        result.resultData.setExtrasClassLoader(AlertActResponse::class.java.classLoader)
        val response = result.resultData.getParcelableExtra<AlertActResponse>(AlertActResponse.BUNDLE_KEY)
        Truth.assertThat(response).isEqualTo(AlertActResponse(alert))
    }

    @After
    fun tearDown() {
        Intents.release()
    }
}

