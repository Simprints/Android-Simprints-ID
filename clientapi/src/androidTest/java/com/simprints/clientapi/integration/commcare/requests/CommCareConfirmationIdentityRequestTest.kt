package com.simprints.clientapi.integration.commcare.requests

import android.app.Activity
import android.app.Instrumentation
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.BundleMatchers
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.simprints.clientapi.activities.commcare.CommCareActivity
import com.simprints.clientapi.integration.AppConfirmIdentityRequest
import com.simprints.clientapi.integration.commcare.BaseCommCareClientApiTest
import com.simprints.clientapi.integration.value
import com.simprints.moduleapi.app.requests.IAppRequest
import org.hamcrest.CoreMatchers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class CommCareConfirmationIdentityRequestTest : BaseCommCareClientApiTest() {

    @Before
    override fun setUp() {
        super.setUp()
        val intentResultOk = Instrumentation.ActivityResult(Activity.RESULT_OK, null)
        Intents.intending(IntentMatchers.hasAction(APP_CONFIRM_ACTION)).respondWith(intentResultOk)
    }

    @Test
    fun callingAppSendsAnConfirmRequest_shouldLaunchAnAppConfirmRequest() {
        ActivityScenario.launch<CommCareActivity>(commCareConfirmIntentRequest.apply { action = COMMCARE_CONFIRM_IDENTITY_ACTION })

        val expectedAppRequest = AppConfirmIdentityRequest(
            projectIdField.value(),
            userIdField.value(),
            sessionIdField.value(),
            selectedGuidField.value())

        Intents.intended(IntentMatchers.hasAction(APP_CONFIRM_ACTION))
        Intents.intended(IntentMatchers.hasExtras(BundleMatchers.hasEntry(IAppRequest.BUNDLE_KEY, bundleDataMatcherForParcelable(expectedAppRequest))))
    }

    @Test
    fun callingAppSendsASuspiciousConfirmRequest_shouldLaunchAnAppConfirmRequest() {
        ActivityScenario.launch<CommCareActivity>(makeIntentRequestSuspicious(commCareConfirmIntentRequest).apply { action = COMMCARE_CONFIRM_IDENTITY_ACTION })
        Intents.intended(IntentMatchers.hasAction(APP_CONFIRM_ACTION))
    }

    @Test
    fun callingAppSendsAnInvalidConfirmRequest_shouldNotLaunchAnAppConfirmRequest() {
        ActivityScenario.launch<CommCareActivity>(makeIntentRequestInvalid(commCareConfirmIntentRequest, sessionIdField).apply { action = COMMCARE_CONFIRM_IDENTITY_ACTION })
        Intents.intended(CoreMatchers.not(IntentMatchers.hasAction(APP_CONFIRM_ACTION)), Intents.times(2))
    }
}
