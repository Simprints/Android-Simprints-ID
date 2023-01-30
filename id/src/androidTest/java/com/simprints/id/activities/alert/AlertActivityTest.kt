package com.simprints.id.activities.alert

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.provider.Settings
import android.view.View
import android.widget.ImageView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBackUnconditionally
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.alert.request.AlertActRequest
import com.simprints.id.activities.alert.response.AlertActResponse
import com.simprints.id.activities.alert.response.AlertActResponse.ButtonAction
import com.simprints.id.activities.coreexitform.CoreExitFormActivity
import com.simprints.id.activities.faceexitform.FaceExitFormActivity
import com.simprints.id.activities.fingerprintexitform.FingerprintExitFormActivity
import com.simprints.id.domain.alert.AlertActivityViewModel
import com.simprints.id.domain.alert.AlertType
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.GeneralConfiguration
import com.simprints.infra.config.domain.models.GeneralConfiguration.Modality
import com.simprints.testtools.unit.robolectric.assertActivityStarted
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class AlertActivityTest {

    private val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext

    @Inject
    lateinit var configManager: ConfigManager

    private val generalConfiguration = mockk<GeneralConfiguration>()

    @Before
    fun setUp() {
        Intents.init()
        coEvery { configManager.getProjectConfiguration() } returns mockk {
            every { general } returns generalConfiguration
        }
    }

    @Test
    fun noParamForAlertActivity_theRightAlertShouldAppear() {
        launchAlertActivity()
        ensureAlertScreenLaunched(AlertActivityViewModel.UNEXPECTED_ERROR)
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
    fun integrityServiceError_theRightAlertShouldAppear() {
        launchAlertActivity(AlertActRequest(AlertType.INTEGRITY_SERVICE_ERROR))
        ensureAlertScreenLaunched(AlertActivityViewModel.INTEGRITY_SERVICE_ERROR)
    }

    @Test
    fun fingerprintEnrolLastBiometricsFailed_theRightAlertShouldAppear() {
        every { generalConfiguration.modalities } returns listOf(Modality.FINGERPRINT)
        launchAlertActivity(AlertActRequest(AlertType.ENROLMENT_LAST_BIOMETRICS_FAILED))
        ensureAlertScreenLaunched(
            AlertActivityViewModel.ENROLMENT_LAST_BIOMETRICS_FAILED,
            "Fingerprint"
        )
    }

    @Test
    fun faceEnrolLastBiometricsFailed_theRightAlertShouldAppear() {
        every { generalConfiguration.modalities } returns listOf(Modality.FACE)
        launchAlertActivity(AlertActRequest(AlertType.ENROLMENT_LAST_BIOMETRICS_FAILED))
        ensureAlertScreenLaunched(
            AlertActivityViewModel.ENROLMENT_LAST_BIOMETRICS_FAILED,
            "Face"
        )
    }

    @Test
    fun bothModalitiesEnrolLastBiometricsFailed_theRightAlertShouldAppear() {
        every { generalConfiguration.modalities } returns listOf(
            Modality.FACE,
            Modality.FINGERPRINT
        )
        launchAlertActivity(AlertActRequest(AlertType.ENROLMENT_LAST_BIOMETRICS_FAILED))
        ensureAlertScreenLaunched(
            AlertActivityViewModel.ENROLMENT_LAST_BIOMETRICS_FAILED,
            "Face/Fingerprint"
        )
    }

    @Test
    fun integrityServiceError_userClicksClose_alertShouldFinishWithRightResult() {
        val scenario = launchAlertActivity(AlertActRequest(AlertType.INTEGRITY_SERVICE_ERROR))
        ensureAlertScreenLaunched(AlertActivityViewModel.INTEGRITY_SERVICE_ERROR)

        onView(withId(R.id.alertLeftButton)).perform(click())

        verifyIntentReturned(scenario.result, AlertType.INTEGRITY_SERVICE_ERROR, ButtonAction.CLOSE)
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

        verifyIntentReturned(
            scenario.result,
            AlertType.GUID_NOT_FOUND_OFFLINE,
            ButtonAction.TRY_AGAIN
        )
    }

    @Test
    fun fingerprintGuidNotFoundOffline_userClicksBack_shouldStartExitForm() {
        every { generalConfiguration.modalities } returns listOf(Modality.FINGERPRINT)
        val scenario = launchAlertActivity(AlertActRequest(AlertType.GUID_NOT_FOUND_OFFLINE))
        ensureAlertScreenLaunched(AlertActivityViewModel.GUID_NOT_FOUND_OFFLINE)
        pressBackUnconditionally()

        scenario.onActivity {
            assertActivityStarted(FingerprintExitFormActivity::class.java, it)
        }
    }

    @Test
    fun faceGuidNotFoundOffline_userClicksBack_shouldStartExitForm() {
        every { generalConfiguration.modalities } returns listOf(Modality.FACE)
        val scenario = launchAlertActivity(AlertActRequest(AlertType.GUID_NOT_FOUND_OFFLINE))
        ensureAlertScreenLaunched(AlertActivityViewModel.GUID_NOT_FOUND_OFFLINE)
        pressBackUnconditionally()

        scenario.onActivity {
            assertActivityStarted(FaceExitFormActivity::class.java, it)
        }
    }

    @Test
    fun bothModalitiesGuidNotFoundOffline_userClicksBack_shouldStartExitForm() {
        every { generalConfiguration.modalities } returns listOf(
            Modality.FACE,
            Modality.FINGERPRINT
        )
        val scenario = launchAlertActivity(AlertActRequest(AlertType.GUID_NOT_FOUND_OFFLINE))
        ensureAlertScreenLaunched(AlertActivityViewModel.GUID_NOT_FOUND_OFFLINE)
        pressBackUnconditionally()

        scenario.onActivity {
            assertActivityStarted(CoreExitFormActivity::class.java, it)
        }
    }

    private fun launchAlertActivity(request: AlertActRequest? = null): ActivityScenario<AlertActivity> =
        ActivityScenario.launch(Intent().apply {
            setClassName(
                ApplicationProvider.getApplicationContext<Application>().packageName,
                AlertActivity::class.qualifiedName!!
            )
            request?.let {
                putExtra(AlertActRequest.BUNDLE_KEY, request)
            }
        })


    private fun ensureAlertScreenLaunched(
        alertActivityViewModel: AlertActivityViewModel,
        vararg messageArgs: Any?
    ) {
        onView(withId(R.id.alertTitle))
            .check(matches(withText(alertActivityViewModel.title)))

        onView(withId(R.id.message))
            .check(
                matches(
                    withText(
                        String.format(
                            app.getString(
                                alertActivityViewModel.message,
                                *messageArgs
                            )
                        )
                    )
                )
            )

        onView(withId(R.id.alertImage)).check(matches(hasImage(alertActivityViewModel.mainDrawable)))
    }

    private fun verifyIntentReturned(
        result: Instrumentation.ActivityResult,
        alert: AlertType,
        buttonAction: ButtonAction
    ) {
        Truth.assertThat(result.resultCode).isEqualTo(Activity.RESULT_OK)

        result.resultData.setExtrasClassLoader(AlertActResponse::class.java.classLoader)
        val response =
            result.resultData.getParcelableExtra<AlertActResponse>(AlertActResponse.BUNDLE_KEY)
        Truth.assertThat(response).isEqualTo(AlertActResponse(alert, buttonAction))
    }

    @After
    fun tearDown() {
        Intents.release()
    }
}

fun hasImage(drawableId: Int): Matcher<View> {
    return object : BoundedMatcher<View, ImageView>(ImageView::class.java) {

        override fun describeTo(description: Description) {
            description.appendText("has image with drawable ID: $drawableId")
        }

        override fun matchesSafely(view: ImageView): Boolean {
            return assertDrawable(view.drawable, drawableId, view)
        }
    }
}

private fun assertDrawable(actual: Drawable, expectedId: Int, v: View): Boolean {
    if (actual !is BitmapDrawable) {
        return false
    }

    var expectedBitmap: Bitmap? = null
    return try {
        expectedBitmap = BitmapFactory.decodeResource(v.context.resources, expectedId)
        actual.bitmap.sameAs(expectedBitmap)
    } catch (error: OutOfMemoryError) {
        return false

    } finally {
        expectedBitmap?.recycle()
    }
}
