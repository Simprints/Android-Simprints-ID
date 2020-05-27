package com.simprints.clientapi.integration.standard.requests

import android.app.Activity
import android.app.Instrumentation.ActivityResult
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.intent.Intents.*
import androidx.test.espresso.intent.matcher.BundleMatchers.hasEntry
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtras
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.clientapi.activities.libsimprints.LibSimprintsActivity
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.integration.AppEnrolLastBiometricsRequest
import com.simprints.clientapi.integration.key
import com.simprints.clientapi.integration.standard.BaseStandardClientApiTest
import com.simprints.clientapi.integration.value
import com.simprints.moduleapi.app.requests.IAppRequest
import io.mockk.coEvery
import io.mockk.mockk
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.mock.declareModule


@RunWith(AndroidJUnit4::class)
class StandardEnrolLastBiometricsRequestTest : BaseStandardClientApiTest() {

    private lateinit var clientApiSessionEventsManager: ClientApiSessionEventsManager

    @Before
    override fun setUp() {
        super.setUp()
        val intentResultOk = ActivityResult(Activity.RESULT_OK, null)
        intending(hasAction(APP_ENROL_LAST_BIOMETRICS_ACTION)).respondWith(intentResultOk)

        clientApiSessionEventsManager = mockk(relaxed = true)
        coEvery { clientApiSessionEventsManager.isCurrentSessionAnIdentification() } returns true
        coEvery { clientApiSessionEventsManager.getCurrentSessionId() } returns sessionIdField.value()

        declareModule {
            factory { clientApiSessionEventsManager }
        }
    }

    @Test
    fun callingAppSendsAnEnrolLastBiometricsRequest_shouldLaunchAnAppEnrolLastBiometricsRequest() {
        ActivityScenario.launch<LibSimprintsActivity>(standardBaseFlowIntentRequest.apply {
            action = STANDARD_ENROL_LAST_BIOMETRICS_ACTION
            putExtra(sessionIdField.key(), sessionIdField.value())
        })

        val expectedAppRequest = AppEnrolLastBiometricsRequest(
            projectIdField.value(),
            userIdField.value(),
            moduleIdField.value(),
            metadataField.value(),
            sessionIdField.value())

        intended(hasAction(APP_ENROL_LAST_BIOMETRICS_ACTION))
        intended(hasExtras(hasEntry(IAppRequest.BUNDLE_KEY, bundleDataMatcherForParcelable(expectedAppRequest))))
    }

    @Test
    fun callingAppSendsASuspiciousEnrolRequest_shouldLaunchAnAppEnrolRequest() {
        ActivityScenario.launch<LibSimprintsActivity>(makeIntentRequestSuspicious(standardBaseFlowIntentRequest).apply {
            action = STANDARD_ENROL_LAST_BIOMETRICS_ACTION
            putExtra(sessionIdField.key(), sessionIdField.value())
        })
        intended(hasAction(APP_ENROL_LAST_BIOMETRICS_ACTION))
    }

    @Test
    fun callingAppSendsAnInvalidEnrolRequest_shouldNotLaunchAnAppEnrolRequest() {
        ActivityScenario.launch<LibSimprintsActivity>(makeIntentRequestInvalid(standardBaseFlowIntentRequest).apply {
            action = STANDARD_ENROL_LAST_BIOMETRICS_ACTION
            putExtra(sessionIdField.key(), sessionIdField.value())
        })
        intended(not(hasAction(APP_ENROL_LAST_BIOMETRICS_ACTION)), times(2))
    }
}
