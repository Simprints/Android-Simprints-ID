package com.simprints.clientapi.integration.odk.requests

import android.app.Activity
import android.app.Instrumentation.ActivityResult
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.intent.Intents.*
import androidx.test.espresso.intent.matcher.BundleMatchers.hasEntry
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtras
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.clientapi.activities.odk.OdkActivity
import com.simprints.clientapi.integration.*
import com.simprints.clientapi.integration.odk.odkBaseIntentRequest
import com.simprints.clientapi.integration.odk.odkEnrolAction
import com.simprints.clientapi.integration.odk.odkInvalidIntentRequest
import com.simprints.clientapi.integration.odk.odkSuspiciousIntentRequest
import com.simprints.moduleapi.app.requests.IAppRequest
import com.simprints.testtools.android.bundleDataMatcherForParcelable
import com.simprints.testtools.common.syntax.value
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class OdkEnrolRequestTest : BaseClientApiTest() {

    @Before
    override fun setUp() {
        super.setUp()
        val intentResultOk = ActivityResult(Activity.RESULT_OK, null)
        intending(hasAction(appEnrolAction)).respondWith(intentResultOk)
    }

    @Test
    fun callingAppSendsAnEnrolRequest_shouldLaunchAnAppEnrolRequest() {
        ActivityScenario.launch<OdkActivity>(odkBaseIntentRequest.apply { action = odkEnrolAction })

        val expectedAppRequest = AppEnrollRequest(
            projectIdField.value(),
            userIdField.value(),
            moduleIdField.value(),
            metadataField.value())

        intended(hasAction(appEnrolAction))
        intended(hasExtras(hasEntry(IAppRequest.BUNDLE_KEY, bundleDataMatcherForParcelable(expectedAppRequest))))
    }

    @Test
    fun callingAppSendsASuspiciousEnrolRequest_shouldLaunchAnAppEnrolRequest() {
        ActivityScenario.launch<OdkActivity>(odkSuspiciousIntentRequest.apply { action = odkEnrolAction })
        intended(hasAction(appEnrolAction))
    }

    @Test
    fun callingAppSendsAnInvalidEnrolRequest_shouldNotLaunchAnAppEnrolRequest() {
        ActivityScenario.launch<OdkActivity>(odkInvalidIntentRequest.apply { action = odkEnrolAction })
        intended(not(hasAction(appEnrolAction)), times(2))
    }
}
