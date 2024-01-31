package com.simprints.clientapi.integration.odk.requests

import android.app.Activity
import android.app.Instrumentation
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.times
import androidx.test.espresso.intent.matcher.BundleMatchers.hasEntry
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtras
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.clientapi.activities.odk.OdkActivity
import com.simprints.clientapi.integration.AppIdentifyRequest
import com.simprints.clientapi.integration.odk.BaseOdkClientApiTest
import com.simprints.clientapi.integration.value
import com.simprints.moduleapi.app.requests.IAppRequest
import org.hamcrest.CoreMatchers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OdkIdentifyRequestTest : BaseOdkClientApiTest() {


    @Before
    override fun setUp() {
        super.setUp()
        val intentResultOk = Instrumentation.ActivityResult(Activity.RESULT_OK, null)
        Intents.intending(hasAction(APP_IDENTIFY_ACTION)).respondWith(intentResultOk)
    }

    @Test
    fun callingAppSendsAnIdentifyRequest_shouldLaunchAnAppIdentifyRequest() {
        ActivityScenario.launch<OdkActivity>(odkBaseFlowIntentRequest.apply { action = ODK_IDENTIFY_ACTION })

        val expectedAppRequest = AppIdentifyRequest(
            projectId = projectIdField.value(),
            userId = userIdField.value(),
            isModuleIdTokenized = false,
            isUserIdTokenized = false,
            moduleId = moduleIdField.value(),
            metadata = metadataField.value()
        )

        intended(hasAction(APP_IDENTIFY_ACTION))
        intended(hasExtras(hasEntry(IAppRequest.BUNDLE_KEY, bundleDataMatcherForParcelable(expectedAppRequest))))
    }

    @Test
    fun callingAppSendsASuspiciousIdentifyRequest_shouldLaunchAnAppIdentifyRequest() {
        ActivityScenario.launch<OdkActivity>(makeIntentRequestSuspicious(odkBaseFlowIntentRequest).apply { action = ODK_IDENTIFY_ACTION })
        intended(hasAction(APP_IDENTIFY_ACTION))
    }

    @Test
    fun callingAppSendsAnInvalidIdentifyRequest_shouldNotLaunchAnAppIdentifyRequest() {
        ActivityScenario.launch<OdkActivity>(makeIntentRequestInvalid(odkBaseFlowIntentRequest).apply { action = ODK_IDENTIFY_ACTION })
        intended(CoreMatchers.not(hasAction(APP_IDENTIFY_ACTION)), times(2))
    }
}