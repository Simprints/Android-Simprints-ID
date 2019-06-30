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
import com.simprints.clientapi.integration.*
import com.simprints.clientapi.integration.standard.standardBaseIntentRequest
import com.simprints.clientapi.integration.standard.standardInvalidIntentRequest
import com.simprints.clientapi.integration.standard.standardSuspiciousIntentRequest
import com.simprints.clientapi.integration.standard.standardVerifyAction
import com.simprints.moduleapi.app.requests.IAppRequest
import com.simprints.testtools.android.bundleDataMatcherForParcelable
import com.simprints.testtools.common.syntax.key
import com.simprints.testtools.common.syntax.value
import org.hamcrest.CoreMatchers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StandardVerifyRequestTest : BaseClientApiTest() {

    @Before
    override fun setUp() {
        super.setUp()
        val intentResultOk = Instrumentation.ActivityResult(Activity.RESULT_OK, null)
        Intents.intending(hasAction(appVerifyAction)).respondWith(intentResultOk)
    }

    @Test
    fun callingAppSendsAVerifyRequest_shouldLaunchAnAppVerifyRequest() {
        ActivityScenario.launch<LibSimprintsActivity>(standardBaseIntentRequest.apply {
            action = standardVerifyAction
            putExtra(verifyGuidField.key(), verifyGuidField.value())
        })

        val expectedAppRequest = AppVerifyRequest(
            projectIdField.value(),
            userIdField.value(),
            moduleIdField.value(),
            metadataField.value(),
            verifyGuidField.value())

        intended(hasAction(appVerifyAction))
        intended(hasExtras(hasEntry(IAppRequest.BUNDLE_KEY, bundleDataMatcherForParcelable(expectedAppRequest))))
    }

    @Test
    fun callingAppSendsASuspiciousVerifyRequest_shouldLaunchAnAppVerifyRequest() {
        ActivityScenario.launch<LibSimprintsActivity>(standardSuspiciousIntentRequest.apply {
            action = standardVerifyAction
            putExtra(verifyGuidField.key(), verifyGuidField.value()) 
        })
        
        intended(hasAction(appVerifyAction))
    }

    @Test
    fun callingAppSendsAnInvalidVerifyRequest_shouldNotLaunchAnAppVerifyRequest() {
        ActivityScenario.launch<LibSimprintsActivity>(standardInvalidIntentRequest.apply {
            action = standardVerifyAction
            putExtra(verifyGuidField.key(), verifyGuidField.value())
        })
        
        intended(CoreMatchers.not(hasAction(appVerifyAction)), Intents.times(2))
    }
}
