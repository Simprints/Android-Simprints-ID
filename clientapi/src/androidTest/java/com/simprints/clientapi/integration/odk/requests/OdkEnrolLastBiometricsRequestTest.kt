package com.simprints.clientapi.integration.odk.requests

import android.app.Activity
import android.app.Instrumentation
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.BundleMatchers
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.clientapi.activities.odk.OdkActivity
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.integration.AppEnrolLastBiometricsRequest
import com.simprints.clientapi.integration.key
import com.simprints.clientapi.integration.odk.BaseOdkClientApiTest
import com.simprints.clientapi.integration.value
import com.simprints.moduleapi.app.requests.IAppRequest
import io.mockk.coEvery
import io.mockk.mockk
import org.hamcrest.CoreMatchers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OdkEnrolLastBiometricsRequestTest : BaseOdkClientApiTest() {

    private lateinit var clientApiSessionEventsManager: ClientApiSessionEventsManager

    @Before
    override fun setUp() {
        super.setUp()
        val intentResultOk = Instrumentation.ActivityResult(Activity.RESULT_OK, null)
        Intents.intending(IntentMatchers.hasAction(APP_ENROL_LAST_BIOMETRICS_ACTION)).respondWith(intentResultOk)

        clientApiSessionEventsManager = mockk(relaxed = true)
        coEvery { clientApiSessionEventsManager.isCurrentSessionAnIdentificationOrEnrolment() } returns true
        coEvery { clientApiSessionEventsManager.getCurrentSessionId() } returns sessionIdField.value()
    }

    @Test
    fun callingAppSendsAnEnrolLastBiometricsRequest_shouldLaunchAnAppEnrolLastBiometricsRequest() {
        ActivityScenario.launch<OdkActivity>(odkBaseFlowIntentRequest.apply {
            action = ODK_ENROL_LAST_BIOMETRICS_ACTION
            putExtra(sessionIdField.key(), sessionIdField.value())
        })

        val expectedAppRequest = AppEnrolLastBiometricsRequest(
            projectId = projectIdField.value(),
            userId = userIdField.value(),
            moduleId = moduleIdField.value(),
            isModuleIdTokenized = false,
            isUserIdTokenized = false,
            metadata = metadataField.value(),
            sessionId = sessionIdField.value()
        )

        Intents.intended(IntentMatchers.hasAction(APP_ENROL_LAST_BIOMETRICS_ACTION))
        Intents.intended(IntentMatchers.hasExtras(BundleMatchers.hasEntry(IAppRequest.BUNDLE_KEY, bundleDataMatcherForParcelable(expectedAppRequest))))
    }

    @Test
    fun callingAppSendsASuspiciousEnrolRequest_shouldLaunchAnAppEnrolRequest() {
        ActivityScenario.launch<OdkActivity>(makeIntentRequestSuspicious(odkBaseFlowIntentRequest).apply {
            action = ODK_ENROL_LAST_BIOMETRICS_ACTION
            putExtra(sessionIdField.key(), sessionIdField.value())
        })
        Intents.intended(IntentMatchers.hasAction(APP_ENROL_LAST_BIOMETRICS_ACTION))
    }

    @Test
    fun callingAppSendsAnInvalidEnrolRequest_shouldNotLaunchAnAppEnrolRequest() {
        ActivityScenario.launch<OdkActivity>(makeIntentRequestInvalid(odkBaseFlowIntentRequest).apply {
            action = ODK_ENROL_LAST_BIOMETRICS_ACTION
            putExtra(sessionIdField.key(), sessionIdField.value())
        })
        Intents.intended(CoreMatchers.not(IntentMatchers.hasAction(APP_ENROL_LAST_BIOMETRICS_ACTION)), Intents.times(2))
    }
}
