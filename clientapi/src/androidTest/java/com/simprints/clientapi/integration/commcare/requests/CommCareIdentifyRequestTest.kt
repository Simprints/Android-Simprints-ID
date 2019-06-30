package com.simprints.clientapi.integration.commcare.requests

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
import androidx.test.filters.MediumTest
import com.simprints.clientapi.activities.commcare.CommCareActivity
import com.simprints.clientapi.integration.*
import com.simprints.clientapi.integration.commcare.commCareBaseIntentRequest
import com.simprints.clientapi.integration.commcare.commCareInvalidIntentRequest
import com.simprints.clientapi.integration.commcare.commCareSuspiciousIntentRequest
import com.simprints.clientapi.integration.commcare.commcareIdentifyAction
import com.simprints.moduleapi.app.requests.IAppRequest
import com.simprints.testtools.android.bundleDataMatcherForParcelable
import com.simprints.testtools.common.syntax.value
import org.hamcrest.CoreMatchers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CommCareIdentifyRequestTest : BaseClientApiTest() {

    @Before
    override fun setUp() {
        super.setUp()
        val intentResultOk = Instrumentation.ActivityResult(Activity.RESULT_OK, null)
        Intents.intending(hasAction(appIdentifyAction)).respondWith(intentResultOk)
    }

    @Test
    @MediumTest
    fun callingAppSendsAnIdentifyRequest_shouldLaunchAnAppIdentifyRequest() {
        ActivityScenario.launch<CommCareActivity>(commCareBaseIntentRequest.apply { action = commcareIdentifyAction })

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
        ActivityScenario.launch<CommCareActivity>(commCareSuspiciousIntentRequest.apply { action = commcareIdentifyAction })
        intended(hasAction(appIdentifyAction))
    }

    @Test
    fun callingAppSendsAnInvalidIdentifyRequest_shouldNotLaunchAnAppIdentifyRequest() {
        ActivityScenario.launch<CommCareActivity>(commCareInvalidIntentRequest.apply { action = commcareIdentifyAction })
        intended(CoreMatchers.not(hasAction(appIdentifyAction)), times(2))
    }
}
