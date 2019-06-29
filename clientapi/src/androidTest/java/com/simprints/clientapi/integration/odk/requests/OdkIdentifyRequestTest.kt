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
import com.simprints.clientapi.integration.*
import com.simprints.clientapi.integration.odk.odkBaseIntentRequest
import com.simprints.clientapi.integration.odk.odkIdentifyAction
import com.simprints.clientapi.integration.odk.odkInvalidIntentRequest
import com.simprints.clientapi.integration.odk.odkSuspiciousIntentRequest
import com.simprints.moduleapi.app.requests.IAppRequest
import com.simprints.testtools.android.bundleDataMatcherForParcelable
import com.simprints.testtools.common.syntax.value
import org.hamcrest.CoreMatchers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OdkIdentifyRequestTest : BaseClientApiTest() {


    @Before
    override fun setUp() {
        super.setUp()
        val intentResultOk = Instrumentation.ActivityResult(Activity.RESULT_OK, null)
        Intents.intending(hasAction(appEnrolAction)).respondWith(intentResultOk)
    }

    @Test
    fun callingAppSendsAnIdentifyRequest_shouldLaunchAnAppIdentifyRequest() {
        ActivityScenario.launch<OdkActivity>(odkBaseIntentRequest.apply { action = odkIdentifyAction })

        val expectedAppRequest = AppIdentifyRequest(
            projectIdField.value(),
            userIdField.value(),
            moduleIdField.value(),
            metadataField.value())

        intended(hasAction(appIdentifyAction))
        intended(hasExtras(hasEntry(IAppRequest.BUNDLE_KEY, bundleDataMatcherForParcelable(expectedAppRequest))))
    }

    @Test
    fun callingAppSendsASuspiciousIdentifyRequest_shouldLaunchAnAppIdentifyRequest() {
        ActivityScenario.launch<OdkActivity>(odkSuspiciousIntentRequest.apply { action = odkIdentifyAction })
        intended(hasAction(appIdentifyAction))
    }

    @Test
    fun callingAppSendsAnInvalidIdentifyRequest_shouldNotLaunchAnAppIdentifyRequest() {
        ActivityScenario.launch<OdkActivity>(odkInvalidIntentRequest.apply { action = odkIdentifyAction })
        intended(CoreMatchers.not(hasAction(appIdentifyAction)), times(2))
    }
}
