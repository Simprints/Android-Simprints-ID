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
import com.simprints.clientapi.integration.AppConfirmIdentityRequest
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
class OdkConfirmationIdentityRequestTest : BaseOdkClientApiTest() {

    private  val clientApiSessionEventsManager: ClientApiSessionEventsManager = mockk(relaxed = true){
        coEvery { isSessionHasIdentificationCallback(sessionIdField.value()) } returns true
        coEvery { getCurrentSessionId() } returns sessionIdField.value()
    }
    @Before
    override fun setUp() {
        super.setUp()
        val intentResultOk = Instrumentation.ActivityResult(Activity.RESULT_OK, null)
        Intents.intending(IntentMatchers.hasAction(APP_CONFIRM_ACTION)).respondWith(intentResultOk)
    }

    @Test
    fun callingAppSendsAnConfirmRequest_shouldLaunchAnAppConfirmRequest() {
        ActivityScenario.launch<OdkActivity>(odkConfirmIntentRequest.apply { action = ODK_CONFIRM_IDENTITY_ACTION })

        val expectedAppRequest = AppConfirmIdentityRequest(
            projectId = projectIdField.value(),
            userId = userIdField.value(),
            isUserIdTokenized = false,
            sessionId = sessionIdField.value(),
            selectedGuid = selectedGuidField.value()
        )

        Intents.intended(IntentMatchers.hasAction(APP_CONFIRM_ACTION))
        Intents.intended(IntentMatchers.hasExtras(BundleMatchers.hasEntry(IAppRequest.BUNDLE_KEY, bundleDataMatcherForParcelable(expectedAppRequest))))
    }

    @Test
    fun callingAppSendsASuspiciousConfirmRequest_shouldLaunchAnAppConfirmRequest() {
        ActivityScenario.launch<OdkActivity>(makeIntentRequestSuspicious(odkConfirmIntentRequest).apply { action = ODK_CONFIRM_IDENTITY_ACTION })
        Intents.intended(IntentMatchers.hasAction(APP_CONFIRM_ACTION))
    }

    @Test
    fun callingAppSendsAnInvalidConfirmRequest_shouldNotLaunchAnAppConfirmRequest() {
        ActivityScenario.launch<OdkActivity>(makeIntentRequestInvalid(odkConfirmIntentRequest, sessionIdField).apply { action = ODK_CONFIRM_IDENTITY_ACTION })
        Intents.intended(CoreMatchers.not(IntentMatchers.hasAction(APP_CONFIRM_ACTION)), Intents.times(2))
    }
}
