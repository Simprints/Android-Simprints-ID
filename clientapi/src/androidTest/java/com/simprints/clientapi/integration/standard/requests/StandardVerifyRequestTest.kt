package com.simprints.clientapi.integration.standard.requests

import android.app.Activity
import android.app.Instrumentation
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.BundleMatchers.hasEntry
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtras
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.clientapi.activities.libsimprints.LibSimprintsActivity
import com.simprints.clientapi.integration.AppVerifyRequest
import com.simprints.clientapi.integration.standard.BaseStandardClientApiTest
import com.simprints.moduleapi.app.requests.IAppRequest
import com.simprints.testtools.android.bundleDataMatcherForParcelable
import com.simprints.testtools.common.syntax.key
import com.simprints.testtools.common.syntax.value
import org.hamcrest.CoreMatchers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StandardVerifyRequestTest : BaseStandardClientApiTest() {

    @Before
    override fun setUp() {
        super.setUp()
        val intentResultOk = Instrumentation.ActivityResult(Activity.RESULT_OK, null)
        Intents.intending(hasAction(APP_VERIFICATION_ACTION)).respondWith(intentResultOk)
    }

    @Test
    fun callingAppSendsAVerifyRequest_shouldLaunchAnAppVerifyRequest() {
        ActivityScenario.launch<LibSimprintsActivity>(standardBaseIntentRequest.apply {
            action = STANDARD_VERIFY_ACTION
            putExtra(verifyGuidField.key(), verifyGuidField.value())
        })

        val expectedAppRequest = AppVerifyRequest(
            projectIdField.value(),
            userIdField.value(),
            moduleIdField.value(),
            metadataField.value(),
            verifyGuidField.value())

        intended(hasAction(APP_VERIFICATION_ACTION))
        intended(hasExtras(hasEntry(IAppRequest.BUNDLE_KEY, bundleDataMatcherForParcelable(expectedAppRequest))))
    }

    @Test
    fun callingAppSendsASuspiciousVerifyRequest_shouldLaunchAnAppVerifyRequest() {
        ActivityScenario.launch<LibSimprintsActivity>(makeIntentRequestSuspicious(standardBaseIntentRequest).apply {
            action = STANDARD_VERIFY_ACTION
            putExtra(verifyGuidField.key(), verifyGuidField.value()) 
        })
        
        intended(hasAction(APP_VERIFICATION_ACTION))
    }

    @Test
    fun callingAppSendsAnInvalidVerifyRequest_shouldNotLaunchAnAppVerifyRequest() {
        ActivityScenario.launch<LibSimprintsActivity>(makeIntentRequestInvalid(standardBaseIntentRequest).apply {
            action = STANDARD_VERIFY_ACTION
            putExtra(verifyGuidField.key(), verifyGuidField.value())
        })
        
        intended(CoreMatchers.not(hasAction(APP_VERIFICATION_ACTION)), Intents.times(2))
    }
}
