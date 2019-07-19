package com.simprints.clientapi.integration.standard.requests

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
import com.simprints.clientapi.activities.libsimprints.LibSimprintsActivity
import com.simprints.clientapi.integration.*
import com.simprints.clientapi.integration.standard.standardBaseIntentRequest
import com.simprints.clientapi.integration.standard.standardIdentifyAction
import com.simprints.clientapi.integration.standard.standardInvalidIntentRequest
import com.simprints.clientapi.integration.standard.standardSuspiciousIntentRequest
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
        Intents.intending(hasAction(APP_IDENTIFY_ACTION)).respondWith(intentResultOk)
    }

    @Test
    fun callingAppSendsAnIdentifyRequest_shouldLaunchAnAppIdentifyRequest() {
        ActivityScenario.launch<LibSimprintsActivity>(standardBaseIntentRequest.apply { action = standardIdentifyAction })

        val expectedAppRequest = AppIdentifyRequest(
            projectIdField.value(),
            userIdField.value(),
            moduleIdField.value(),
            metadataField.value())

        intended(hasAction(APP_IDENTIFY_ACTION))
        intended(hasExtras(hasEntry(IAppRequest.BUNDLE_KEY, bundleDataMatcherForParcelable(expectedAppRequest))))
    }

    @Test
    fun callingAppSendsASuspiciousIdentifyRequest_shouldLaunchAnAppIdentifyRequest() {
        ActivityScenario.launch<LibSimprintsActivity>(standardSuspiciousIntentRequest.apply { action = standardIdentifyAction })
        intended(hasAction(APP_IDENTIFY_ACTION))
    }

    @Test
    fun callingAppSendsAnInvalidIdentifyRequest_shouldNotLaunchAnAppIdentifyRequest() {
        ActivityScenario.launch<LibSimprintsActivity>(standardInvalidIntentRequest.apply { action = standardIdentifyAction })
        intended(CoreMatchers.not(hasAction(APP_IDENTIFY_ACTION)), times(2))
    }
}
